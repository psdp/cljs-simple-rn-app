(ns simple-app.ui.screens.about
  (:require [simple-app.js-deps :as js-deps]
            [simple-app.ui.components.material-ui :as ui]
            [simple-app.ui.components.react-native :as rn]
            [re-frame.core :as re-frame]
            [reagent.core :as r]))

(def logo-img (js/require "./resources/images/cljs.png"))

(defn about []
  (fn [{:keys [screenProps navigation] :as props}]
    (let [greeting (re-frame/subscribe [:get-greeting])]
      [rn/view
       [ui/toolbar {:center-element "About" :left-element "arrow-back" :on-left-element-press #(.goBack navigation)}]
       [rn/view {:style {:flex-direction "column" :margin 40 :align-items "center"}}
        [rn/text {:style {:font-size 30 :font-weight "100" :margin-bottom 20 :text-align "center"}} @greeting]
        [rn/image {:source logo-img
                      :style  {:width 80 :height 80 :margin-bottom 30}}]
        [ui/button {:text "Back to Home" :ancent true :on-press #(.goBack navigation)}]]])))
