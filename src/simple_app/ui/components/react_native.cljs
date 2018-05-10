(ns simple-app.ui.components.react-native
  (:require [simple-app.js-deps :as js-deps]
            [reagent.core :as r]))

;; React Components

(def app-registry (.-AppRegistry js-deps/react-native))

(def text (r/adapt-react-class (.-Text js-deps/react-native)))
(def view (r/adapt-react-class (.-View js-deps/react-native)))
(def image (r/adapt-react-class (.-Image js-deps/react-native)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight js-deps/react-native)))
(def status-bar (r/adapt-react-class (.-StatusBar js-deps/react-native)))
