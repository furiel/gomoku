(ns gomoku.event-loop
  (:require
   [clojure.core.async :refer [chan put! <! close! go go-loop]])
  (:gen-class))

(defn put-event! [event-loop event]
  (put! event-loop event))

(defn stop-event-loop [event-loop]
    (close! event-loop))

(defn start-event-loop [initial-state event-handlers]
  (let [event-loop (chan)]
    (go-loop [event (<! event-loop) state initial-state]
      (when event
        (when (not (contains? event :event))
          (throw (Exception. (str "Invalid event:" event " state: " state))))
        (when (not (contains? event-handlers (:event event)))
          (throw (Exception. (str "Unknown event:" event " state: " state))))
        (let [event-handler (get event-handlers (:event event))
              next-state (event-handler state event)]
          (recur (<! event-loop) next-state))))
    event-loop))
