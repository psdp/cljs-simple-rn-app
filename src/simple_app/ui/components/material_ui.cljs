(ns simple-app.ui.components.material-ui
  (:require [reagent.core :as r]))

(def ReactNativeMaterialUI (js/require "react-native-material-ui"))

(def theme-provider (r/adapt-react-class (.-ThemeProvider ReactNativeMaterialUI)))
(def button (r/adapt-react-class (.-Button ReactNativeMaterialUI)))
(def toolbar (r/adapt-react-class (.-Toolbar ReactNativeMaterialUI)))

(def colors
  (js->clj (.-COLOR ReactNativeMaterialUI) :keywordize-keys true))
