(ns simple-app.ui.utils.navigation
  (:refer-clojure :exclude [pop replace]))

; API

(defonce ReactNavigation (js/require "react-navigation"))
(defonce NavigationActions (.-NavigationActions ReactNavigation))
(defonce StackActions (.-StackActions ReactNavigation))
(defonce createStackNavigator (.-createStackNavigator ReactNavigation))
(defonce createSwitchNavigator (.-createSwitchNavigator ReactNavigation))

; Utils

(def navigator nil)

(defn init [n]
  (set! navigator n))

(defn navigate
  [{:keys [routeName params]}]
  (.dispatch navigator (.navigate NavigationActions #js {:routeName routeName :params params})))

(defn replace
  [{:keys [key newKey routeName params]}]
  (.dispatch navigator (.replace StackActions #js {:key key :newKey newKey :routeName routeName :params params})))

(defn back [] (.dispatch navigator (.back NavigationActions)))

(defn push
  [{:keys [routeName params]}]
  (.dispatch navigator (.push StackActions #js {:routeName routeName :params params})))

(defn pop [] (.dispatch navigator (.pop StackActions)))

(defn state [] (.-state navigator))
