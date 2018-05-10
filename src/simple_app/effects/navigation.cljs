(ns simple-app.effects.navigation
  (:require [re-frame.core :as re-frame]
            [simple-app.ui.utils.navigation :as utils]))

(re-frame/reg-fx :nav/navigate utils/navigate)
(re-frame/reg-fx :nav/back utils/back)
(re-frame/reg-fx :nav/push utils/push)
(re-frame/reg-fx :nav/pop utils/pop)
