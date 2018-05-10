(ns simple-app.ui.screens.views
  (:require [simple-app.js-deps :as js-deps]
            [simple-app.ui.components.material-ui :as ui]
            [simple-app.ui.components.react-native :as rn]
            [simple-app.ui.routes :as routes]
            [re-frame.core :as re-frame]
            [reagent.core :as r]))

(def ui-theme
  {:palette {:primaryColor (:green500 ui/colors)
             :accentColor  (:pink500 ui/colors)}
   :toolbar {:container {:height     70
                         :paddingTop 20}}})

(defn main []
  [ui/theme-provider {:ui-theme ui-theme}
   [rn/view {:flex 1}
    [rn/status-bar {:background-color "rgba(0, 0, 0, 0.2)" :translucent true}]
    [(r/adapt-react-class routes/routing)]]])
