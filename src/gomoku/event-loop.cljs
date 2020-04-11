(ns gomoku.event-loop
  (:require
   [cljs.core.async :refer [chan put! <! go go-loop]]
   [gomoku.board :refer [draw-me]]))

(def event-queue (chan))

(defn start-event-loop []
  (go-loop [event (<! event-queue)]
    (let [{{[x y] :point} :click} event]
      (draw-me x y))
    (recur (<! event-queue))))

(defn click-event [x y]
  (put! event-queue
        {:click {:player :x
                 :point [x y]}}))

(defn handle-message [msg])