(ns simple-app.android.core
  (:require [simple-app.core :as core]
            [simple-app.ui.container :as container]))

(def app-root container/main)

(defn init []
  (core/init app-root))
