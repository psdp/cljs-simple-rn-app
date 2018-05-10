(ns ^:figwheel-no-load env.ios.main
  (:require [reagent.core :as r]
            [re-frisk-remote.core :as rr]
            [re-frame.core :refer [clear-subscription-cache!]]
            [simple-app.ios.core :as core]
            [figwheel.client :as fw]
            [env.dev :as env]))

(enable-console-print!)

(assert (exists? core/init) "Fatal Error - Your core.cljs file doesn't define an 'init' function!!! - Perhaps there was a compilation failure?")
(assert (exists? core/app-root) "Fatal Error - Your core.cljs file doesn't define an 'app-root' function!!! - Perhaps there was a compilation failure?")

(def cnt (r/atom 0))
(defn reloader [] @cnt [core/app-root])

;; Do not delete, root-el is used by the figwheel-bridge.js
(def root-el (r/as-element [reloader]))

(defn force-reload! []
      (clear-subscription-cache!)
      (swap! cnt inc))

(def host (:android env/hosts))

(fw/start {:websocket-url    (str "ws://" host ":3449/figwheel-ws")
           :heads-up-display false
           :jsload-callback  force-reload!})

(rr/enable-re-frisk-remote! {:host (str host ":4567")
                             :on-init core/init})
