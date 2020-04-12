(ns gomoku.event-loop
  (:require
   [cljs.core.async :refer [chan put! <! go go-loop]]
   [gomoku.websockets :refer [send-msg!]]
   [gomoku.board :refer [draw-me]]))

(def event-queue (chan))

(defn handle-click-event [event]
  (let [{{[x y] :point who :player} :click} event]
    (draw-me x y who)))

(defn start-event-loop []
  (go-loop [event (<! event-queue)]
    (cond
      (contains? event :click) (handle-click-event event))
      (recur (<! event-queue))))

(defn click-event [x y who]
  (send-msg!
   {:click {:player who
            :point [x y]}}))

(defn handle-message [msg]
  (put! event-queue msg))
