(ns simple-app.ui.routes
  (:require [simple-app.js-deps :as js-deps]
            [simple-app.ui.screens.about :as about]
            [simple-app.ui.screens.home :as home]
            [reagent.core :as r]))

(def routing
  (.createStackNavigator js-deps/react-navigation
                         #js {:home #js {:screen (r/reactify-component home/home)}
                              :about #js {:screen (r/reactify-component about/about)}}
                         #js {:headerMode "none"}))
