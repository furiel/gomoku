(ns gomoku.websockets
  (:require
   [gomoku.board :refer [put-event!]]
   [cognitect.transit :as transit]
   [org.httpkit.server :refer [send! with-channel on-close on-receive open?]])
  (:gen-class))

(import [java.io ByteArrayInputStream ByteArrayOutputStream])

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

(defn ws-handler [game request]
  (with-channel request channel
    (connect! game channel)
    (on-close channel (partial disconnect! game channel))
    (on-receive channel #(read-event game channel (read-msg %)))))

(defn wrap-ws [game handler]
  (fn [req]
    (if (clojure.string/starts-with? (:uri req) "/ws")
      (ws-handler game req)
      (handler req))))
