(ns simple-app.ui.container
  (:require [reagent.core :as r]
            [simple-app.ui.components.material-ui :as ui]
            [simple-app.ui.components.react-native :as rn]
            [simple-app.ui.utils.navigation :as nav.utils]
            [simple-app.ui.screens.about :refer [about]]
            [simple-app.ui.screens.home :refer [home]]))

(def ui-theme
  {:palette {:primaryColor (:green500 ui/colors)
             :accentColor  (:pink500 ui/colors)}
   :toolbar {:container {:height     70
                         :paddingTop 20}}})

(def Router
  (nav.utils/createStackNavigator
   #js {:home #js {:screen (r/reactify-component home)}
        :about #js {:screen (r/reactify-component about)}}
   #js {:headerMode "none"}))

(defn main []
  [ui/theme-provider {:ui-theme ui-theme}
   [rn/view {:flex 1}
    [rn/status-bar {:background-color "rgba(0, 0, 0, 0.2)" :translucent true}]
    [:> Router {:ref (fn [navigator] (nav.utils/init navigator))}]]])
