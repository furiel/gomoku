(ns gomoku.core
  (:require
   [reagent.dom :as rd]
   [reagent.core :as r]
   [gomoku.event-loop :refer [start-event-loop click-event handle-message]]
   [gomoku.websockets :refer [make-websocket!]]
   [gomoku.board :refer [board]]))

(def websocket-url (str "ws://" (.-host js/location) "/ws"))

(defn page []
  [:div
   [:div "Board"]
   [:div (board {:dimension [10 10]
                 :on-click click-event})]])

(defn get-app-element []
  (.getElementById js/document "app"))

(defn mount [el]
  (rd/render [page] el))

(defn start-game [el]
  (mount el)
  (start-event-loop))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (make-websocket! websocket-url (partial start-game el) handle-message)))

(mount-app-element)

(defn ^:after-load on-reload []
  (mount-app-element)
)
