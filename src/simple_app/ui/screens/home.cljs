(ns simple-app.ui.screens.home
  (:require [re-frame.core :as re-frame]
            [simple-app.ui.components.material-ui :as ui]
            [simple-app.ui.components.react-native :as rn]))

(def logo-img (js/require "./resources/images/cljs.png"))

(defn alert [title]
      (.alert rn/Alert title))

(defn home [props]
  (let [greeting (re-frame/subscribe [:get-greeting])]
    [rn/view
     [ui/toolbar {:center-element "Home" :left-element "menu"}]
     [rn/view {:style {:flex-direction "column" :margin 40 :align-items "center"}}
      [rn/text {:style {:font-size 30 :font-weight "100" :margin-bottom 20 :text-align "center"}} @greeting]
      [rn/image {:source logo-img
                 :style  {:width 80 :height 80 :margin-bottom 30}}]
      [rn/view {:style {:flex-direction "row" :justify-content "center"}}
       [rn/view {:style {:margin-horizontal 8}}
        [ui/button {:text "Alert!" :raised true :accent true :on-press #(alert "Hello World")}]]
       [rn/view {:style {:margin-horizontal 8}}
        [ui/button {:text "About" :raised true :on-press #(re-frame/dispatch [:nav/navigate-to "about"])}]]]]]))
