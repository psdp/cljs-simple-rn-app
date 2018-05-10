(ns simple-app.events.navigation
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-event-fx
 :nav/navigate-to
 (fn [_ [_ route-name route-params]]
   {:nav/navigate {:routeName route-name :params route-params}}))

(re-frame/reg-event-fx
 :nav/back
 (fn [_ _]
   {:nav/back nil}))
