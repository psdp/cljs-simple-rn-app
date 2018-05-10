(require '[cljs.build.api :as api]
         '[clojure.string :as str]
         '[clojure.edn :as edn]
         '[clojure.java.io :as io])

;; clj build.clj help # Prints details about tasks

;;; Load config file

(def main-config
  (edn/read-string (slurp "build-cfg.edn")))

(def cli-tasks-info
  {:compile  {:desc  "Compile ClojureScript"
              :usage ["Usage: clj build.clj compile [env] [build-id] [type]"
                      ""
                      "[env] (required): Pre-defined build environment. Allowed values: \"dev\", \"prod\", \"test\""
                      "[build-id] (optional): Build ID. When omitted, this task will compile all builds from the specified [env]."
                      "[type] (optional): Build type - value could be \"once\" or \"watch\". Default: \"once\"."]}
   :figwheel {:desc  "Start figwheel + CLJS REPL / nREPL"
              :usage ["Usage: clj -R:repl build.clj figwheel [options]"
                      ""
                      "[-h|--help] to see all available options"]}
   :test     {:desc  "Run tests"
              :usage ["Usage: clj -R:test build.clj test [build-id]"
                      ""
                      "Compile and run the tests once."]}
   :help     {:desc "Show this help"}})

;;; Helper functions.

(def reset-color "\u001b[0m")
(def red-color "\u001b[31m")
(def green-color "\u001b[32m")
(def yellow-color "\u001b[33m")

(defn- colorizer [c]
  (fn [& args]
    (str c (apply str args) reset-color)))

(defn- println-colorized [message color]
  (println ((colorizer color) message)))

(defn- elapsed [started-at]
  (let [elapsed-us (- (System/currentTimeMillis) started-at)]
    (with-precision 2
      (str (/ (double elapsed-us) 1000) " seconds"))))

(defn- try-require [ns-sym]
  (try
    (require ns-sym)
    true
    (catch Exception e
      false)))

(defmacro with-namespaces [namespaces & body]
  (if (every? try-require namespaces)
    `(do ~@body)
    `(do (println-colorized "task not available - required dependencies not found" red-color)
         (System/exit 1))))

(defn- get-cljsbuild-config [name-env & [name-build-id]]
  (try
    (let [env    (keyword name-env)
          config (:cljsbuild main-config)]
      (when-not (contains? config env)
        (throw (Exception. (str "ENV " (pr-str name-env) " does not exist"))))
      (let [env-config (get config env)]
        (if name-build-id
          (let [build-id (keyword name-build-id)]
            (when-not (contains? env-config build-id)
              (throw (Exception. (str "Build ID " (pr-str name-build-id) " does not exist"))))
            (get env-config build-id))
          env-config)))
    (catch Exception e
      (println-colorized (.getMessage e) red-color)
      (System/exit 1))))

(defn- get-output-files [compiler-options]
  (if-let [output-file (:output-to compiler-options)]
    [output-file]
    (into [] (map :output-to (->> compiler-options :modules vals)))))

(defn- compile-cljs-with-build-config [build-config build-fn env build-id]
  (let [{:keys [source-paths compiler]} build-config
        output-files                    (get-output-files compiler)
        started-at                      (System/currentTimeMillis)]
    (println (str "Compiling " (pr-str build-id) " for " (pr-str env) "..."))
    (flush)
    (build-fn (apply api/inputs source-paths) compiler)
    (println-colorized (str "Successfully compiled " (pr-str output-files) " in " (elapsed started-at) ".") green-color)))

(defn- compile-cljs [env & [build-id watch?]]
  (let [build-fn (if watch? api/watch api/build)]
    (if build-id
      (compile-cljs-with-build-config (get-cljsbuild-config env build-id) build-fn env build-id)
      (doseq [[build-id build-config] (get-cljsbuild-config env)]
        (compile-cljs-with-build-config (get-cljsbuild-config env build-id) build-fn env build-id)))))

(defn- show-help []
  (doseq [[task {:keys [desc usage]}] cli-tasks-info]
    (println (format (str yellow-color "%-12s" reset-color green-color "%s" reset-color)
                     (name task) desc))
    (when usage
      (println)
      (->> usage
           (map #(str "  " %))
           (str/join "\n")
           println)
      (println))))

(defn enable-source-maps
  []
  (println "Source maps enabled.")
  (let [path "node_modules/metro/src/Server/index.js"]
    (spit path
          (str/replace (slurp path) "/\\.map$/" "/main.map$/"))))

(defn get-lan-ip
  "If .lan-ip file exists, it fetches the ip from the file."
  []
  (if-let [ip (try (slurp ".lan-ip") (catch Exception e nil))]
    (str/trim-newline ip)
    (cond
      (some #{(System/getProperty "os.name")} ["Mac OS X" "Windows 10"])
      (.getHostAddress (java.net.InetAddress/getLocalHost))

      :else
      (->> (java.net.NetworkInterface/getNetworkInterfaces)
           (enumeration-seq)
           (filter #(not (or (str/starts-with? (.getName %) "docker")
                             (str/starts-with? (.getName %) "br-"))))
           (map #(.getInterfaceAddresses %))
           (map
             (fn [ip]
               (seq (filter #(instance?
                               java.net.Inet4Address
                               (.getAddress %))
                            ip))))
           (remove nil?)
           (first)
           (filter #(instance?
                      java.net.Inet4Address
                      (.getAddress %)))
           (first)
           (.getAddress)
           (.getHostAddress)))))

(defmulti resolve-dev-host (fn [platform _] platform))

(defmethod resolve-dev-host :default [_ device-type]
  (case device-type
    :real       "localhost"
    :avd        "10.0.2.2"
    :genymotion "10.0.3.2"
    (get-lan-ip)))

(defmethod resolve-dev-host :ios [_ device-type]
  (if (= device-type :simulator)
    "localhost"
    (get-lan-ip)))

(defn write-env-dev [hosts-map]
  (-> "(ns env.dev)\n\n(def hosts %s)"
      (format (with-out-str (clojure.pprint/pprint hosts-map)))
      ((partial spit "env/dev/env/dev.cljs"))))

(defn rebuild-index-js [platform {:keys [app-name host-ip js-modules resource-dirs]}]
  (let [modules     (->> (for [dir resource-dirs]
                           (->> (file-seq (io/file dir))
                                (filter #(and (not (re-find #"DS_Store" (str %)))
                                              (.isFile %)))
                                (map (fn [file] (when-let [unix-path (->> file .toPath .iterator iterator-seq (str/join "/"))]
                                                  (-> (str "./" unix-path)
                                                      (str/replace "\\" "/")
                                                      (str/replace "@2x" "")
                                                      (str/replace "@3x" "")))))))
                         flatten
                         (concat js-modules ["react" "react-native" "create-react-class"])
                         distinct)
        modules-map (zipmap modules modules)
        target-file (str "index." (if (= :ios platform) "ios" "android")  ".js")]
    (try
      (-> "var modules={};%s;\nrequire('figwheel-bridge').withModules(modules).start('%s','%s','%s');"
          (format
           (->> modules-map
                (map (fn [[module path]]
                       (str "modules['" module "']=require('" path "')")))
                (str/join ";"))
           app-name
           (name platform)
           host-ip)
          ((partial spit target-file)))
      (println-colorized (str target-file " was regenerated") green-color)
      (catch Exception e
        (println "Error: " e)))))

;;; Task dispatching

(defmulti task first)

(defmethod task :default [args]
  (println (format "Unknown or missing task. Choose one of: %s\n"
                   (->> cli-tasks-info
                        keys
                        (map name)
                        (interpose ", ")
                        (apply str))))
  (show-help)
  (System/exit 1))

;;; Compiling task

(defmethod task "compile" [[_ env build-id type]]
  (case type
    (nil "once") (compile-cljs env build-id)
    "watch"      (compile-cljs env build-id true)
    (do (println "Unknown argument to compile task:" type)
        (System/exit 1))))

;;; Testing task

(defmethod task "test" [[_ build-id]]
  (with-namespaces [[doo.core :as doo]]
    (compile-cljs :test build-id)
    (doo/run-script :node (->> build-id (get-cljsbuild-config :test) :compiler))))

;;; Figwheeling task

(def figwheel-cli-opts
  [["-p" "--platform BUILD-IDS" "Platform Build IDs - Android / iOS"
    :id       :build-ids
    :default  [:android]
    :parse-fn #(->> (.split % ",")
                    (map (comp keyword str/lower-case str/trim))
                    vec)
    :validate [(fn [build-ids] (every? #(some? (#{:android :ios} %)) build-ids)) "Allowed \"android\", and/or \"ios\""]]
   ["-n" "--nrepl-port PORT" "nREPL Port"
    :id       :port
    :parse-fn #(if (string? %) (Integer/parseInt %) %)
    :validate [#(or (true? %) (< 0 % 0x10000)) "Must be a number between 0 and 65536"]]
   ["-a" "--android-device TYPE" "Android Device Type"
    :id       :android-device
    :parse-fn #(keyword (str/lower-case %))
    :validate [#(some? (#{:avd :genymotion :real} %)) "Must be \"avd\", \"genymotion\", or \"real\""]]
   ["-i" "--ios-device TYPE" "iOS Device Type"
    :id       :ios-device
    :parse-fn #(keyword (str/lower-case %))
    :validate [#(some? (#{:simulator :real} %)) "Must be \"simulator\", or \"real\""]]
   ["-h" "--help"]])

(defn print-and-exit [msg]
  (println msg)
  (System/exit 1))

(defn parse-figwheel-cli-opts [args]
  (with-namespaces [[clojure.tools.cli :as cli]]
    (let [{:keys [options errors summary]} (cli/parse-opts args figwheel-cli-opts)]
      (cond
        (:help options)     (print-and-exit summary)
        (not (nil? errors)) (print-and-exit errors)
        :else               options))))

(defmethod task "figwheel" [[_ & args]]
  (with-namespaces [[figwheel-sidecar.repl-api :as ra]
                    [hawk.core :as hawk]
                    [re-frisk-sidecar.core :as rfs]]
    (let [{:keys [build-ids
                  port
                  android-device
                  ios-device]} (parse-figwheel-cli-opts args)
          hosts-map            {:android (resolve-dev-host :android android-device)
                                :ios     (resolve-dev-host :ios ios-device)}]
      (enable-source-maps)
      (write-env-dev hosts-map)
      (doseq [build-id build-ids
              :let     [host-ip                 (get hosts-map build-id)
                        platform-name           (if (= build-id :ios) "iOS" "Android")
                        {:keys [js-modules
                                app-name
                                resource-dirs]} main-config]]
        (rebuild-index-js build-id {:app-name      app-name
                                    :host-ip       host-ip
                                    :js-modules    js-modules
                                    :resource-dirs resource-dirs})
        (println-colorized (format "Dev server host for %s: %s" platform-name host-ip) green-color))
      (ra/start-figwheel!
       {:figwheel-options (cond-> {:builds-to-start build-ids}
                            port (merge {:nrepl-port       port
                                         :nrepl-middleware ["cider.nrepl/cider-middleware"
                                                            "refactor-nrepl.middleware/wrap-refactor"
                                                            "cemerick.piggieback/wrap-cljs-repl"]}))
        :all-builds       (into [] (for [[build-id {:keys [source-paths compiler warning-handlers]}]
                                         (get-cljsbuild-config :dev)]
                                     {:id           build-id
                                      :source-paths (conj source-paths "env/dev")
                                      :compiler     compiler
                                      :figwheel     true}))})
      (rfs/-main)
      (if-not port
        (ra/cljs-repl)
        (spit ".nrepl-port" port)))))

;;; Help

(defmethod task "help" [_]
  (show-help)
  (System/exit 1))

;;; Build script entrypoint.

(task *command-line-args*)
