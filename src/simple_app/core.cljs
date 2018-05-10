(ns simple-app.core
  (:require simple-app.events
            simple-app.subs
            simple-app.effects
            [simple-app.ui.components.react-native :as rn]
            [re-frame.core :as re-frame]
            [reagent.core :as r]))

(when js/goog.DEBUG
  (aset js/console "ignoredYellowBox" #js ["re-frame: overwriting"]))

(defn init [app-root]
  (re-frame/dispatch-sync [:initialize-db])
  (.registerComponent rn/app-registry "SimpleApp" #(r/reactify-component app-root)))
