(ns gomoku.websockets
  (:require
   [gomoku.event-loop :refer [put-event!]]
   [cognitect.transit :as transit]
   [org.httpkit.server :refer [send! with-channel on-close on-receive]])
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

(defn connect! [channel]
  (put-event! {:event 'connect :channel channel}))

(defn disconnect! [channel status]
  (put-event! {:event 'disconnect! :channel channel}))

(defn read-msg [transit-msg]
  (let [in (ByteArrayInputStream. (.getBytes transit-msg))
        reader (transit/reader in :json)
        msg (transit/read reader)]
    msg))

(defn notify-clients [msg]
  (doseq [channel (get-channels)]
    (send! channel (write-msg msg))))

(defn ws-handler [request]
  (with-channel request channel
    (connect! channel)
    (on-close channel (partial disconnect! channel))
    (on-receive channel #(notify-clients (read-msg %)))))

(defn wrap-ws [handler]
  (fn [req]
    (if (clojure.string/starts-with? (:uri req) "/ws")
      (ws-handler req)
      (handler req))))
