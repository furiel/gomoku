(ns gomoku.core
  (:require
   [reagent.dom :as rd]
   [reagent.core :as r]
   [cljs.core.async :refer [chan put! <! go go-loop timeout]]
   [gomoku.board :as board]))

(def event-queue (chan))

(defn start-event-loop []
  (go-loop [event (<! event-queue)]
    (let [{{[x y] :point} :click} event]
      (board/draw-me x y))
    (recur (<! event-queue))))

(defn click-event [x y]
  (put! event-queue
        {:click {:player :x
                 :point [x y]}}))
(defn page []
  [:div
   [:div "Board"]
   [:div (board/board {:dimension [10 10]
                       :on-click click-event})]])

(defn get-app-element []
  (.getElementById js/document "app"))

(defn mount [el]
  (rd/render [page] el))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount el)
    (start-event-loop)))

(mount-app-element)

(defn ^:after-load on-reload []
  (mount-app-element)
)
