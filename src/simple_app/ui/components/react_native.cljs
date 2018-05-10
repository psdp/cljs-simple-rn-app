(ns simple-app.ui.components.react-native
  (:require [reagent.core :as r]))

(def ReactNative (js/require "react-native"))

(def Alert (.-Alert ReactNative))

;; React Components

(def app-registry (.-AppRegistry ReactNative))

(def text (r/adapt-react-class (.-Text ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))
(def status-bar (r/adapt-react-class (.-StatusBar ReactNative)))
