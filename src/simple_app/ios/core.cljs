(ns simple-app.ios.core
  (:require [simple-app.core :as core]
            [simple-app.ui.screens.views :as views]))

(def app-root views/main)

(defn init []
  (core/init app-root))
