(ns simple-app.ui.components.material-ui
  (:require [simple-app.js-deps :as js-deps]
            [reagent.core :as r]))

(def theme-provider (r/adapt-react-class (.-ThemeProvider js-deps/react-native-material-ui)))
(def button (r/adapt-react-class (.-Button js-deps/react-native-material-ui)))
(def toolbar (r/adapt-react-class (.-Toolbar js-deps/react-native-material-ui)))

(def colors
  (js->clj (.-COLOR js-deps/react-native-material-ui) :keywordize-keys true))
