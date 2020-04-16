(ns gomoku.event-loop
  (:require
   [gomoku.board :refer [handle-connect-event handle-disconnect-event handle-read-event new-game]]
   [clojure.core.async :refer [chan put! <! close! go go-loop]])
  (:gen-class))

(def event-queue (atom nil))

(defn put-event! [event]
  (put! @event-queue event))

(defn inspect! []
  (put! @event-queue {:event 'inspect}))

(defn handle-inspect-event [game]
  (println game)
  game)

(defn dispatch-on-event [game event]
  (cond
    (= (:event event) 'connect) (handle-connect-event game (:channel event))
    (= (:event event) 'inspect) (handle-inspect-event game)
    (= (:event event) 'disconnect) (handle-disconnect-event game (:channel event))
    (= (:event event) 'read) (handle-read-event game (:channel event) (:msg event))
    :else (throw (Exception. (str "Unknown event:" event " game: " game)))))

(defn stop-game []
  (when @event-queue
    (close! @event-queue)
    (reset! event-queue nil)))

(defn start-new-game [callbacks]
  (when @event-queue
    (stop-game))
  (reset! event-queue (chan))
  (go-loop [event (<! @event-queue) game (new-game (:notify callbacks))]
    (when event
      (let [next-game (dispatch-on-event game event)]
        (recur (<! @event-queue) next-game)))))
