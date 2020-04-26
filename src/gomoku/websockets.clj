(ns gomoku.websockets
  (:require
   [gomoku.board :refer [put-event! start-new-game]]
   [gomoku.lib :refer [error?]]
   [cognitect.transit :as transit]
   [org.httpkit.server :refer [send! with-channel on-close on-receive open?]])
  (:gen-class))

(import [java.io ByteArrayInputStream ByteArrayOutputStream])

(def games (atom {}))

(defn write-msg [msg]
  (let [out (ByteArrayOutputStream. 4096)
        writer (transit/writer out :json)]
    (transit/write writer msg)
    (let [transit-msg (.toString out)]
      transit-msg)))

(defn send-channel [channel msg]
  (send! channel (write-msg msg)))

(defn connect! [game channel]
  (put-event! game {:event 'connect :channel channel}))

(defn disconnect! [game channel status]
  (put-event! game {:event 'disconnect :channel channel}))

(defn read-msg [transit-msg]
  (let [in (ByteArrayInputStream. (.getBytes transit-msg))
        reader (transit/reader in :json)
        msg (transit/read reader)]
    msg))

(defn read-event [game channel msg]
  (put-event! game {:event 'read :channel channel :msg msg}))

(defn- get-game [games id]
  (or (get games id) {:game (start-new-game {:notify send-channel}) :channels #{}}))

(defn- add-player [game channel]
  (if (= (count (:channels game)) 2)
    'too-many-players
    (update game :channels conj channel)))

(defn update-games-or-set-error [id channel error]
  (swap! games
          (fn [games]
            (let [game (get-game games id)
                  updated-game (add-player game channel)]
              (if (error? updated-game)
                (do (reset! error updated-game) games)
                (assoc games id updated-game))))))

(defn update-games! [id channel]
  (let [error (atom nil)
        updated-games (update-games-or-set-error id channel error)]
    (if @error
      @error
      (get updated-games id))))

(defn ws-handler [id request]
  (with-channel request channel
    (let [new-game (update-games! id channel)]
      (assert new-game)
      (when-not (error? new-game)
        (let [game (:game new-game)]
          (connect! game channel)
          (on-close channel (partial disconnect! game channel))
          (on-receive channel #(read-event game channel (read-msg %))))))))

(defn wrap-ws [handler]
  (fn [req]
    (if (clojure.string/starts-with? (:uri req) "/ws")
      (ws-handler 1 req)
      (handler req))))
