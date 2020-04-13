(ns gomoku.event-loop
  (:require
   [gomoku.board :refer [handle-connect-event handle-disconnect-event handle-read-event]]
   [clojure.core.async :refer [chan put! <! put! go go-loop]])
  (:gen-class))

(def event-queue (chan))

(defn put-event! [event]
  (put! event-queue event))

(defn start-event-loop []
  (go-loop [event (<! event-queue)]
    (cond
      (= (:event event) 'connect) (handle-connect-event (:channel event))
      (= (:event event) 'disconnect) (handle-disconnect-event (:channel event))
      (= (:event event) 'read) (handle-read-event (:channel event) (:msg event)))
    (recur (<! event-queue))))
