(ns gomoku.websockets
  (:require
   [cognitect.transit :as transit]
   [gomoku.board :refer [add-player! remove-player! get-channels
                         channel-to-player display-message everyone-arrived?
                         get-channels-from-data]]
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

(defn send-display [data]
  (doseq [channel (get-channels-from-data data)]
    (send-channel channel (display-message channel))))

(defn connect! [channel]
 (let [{status :status data :data} (add-player! channel)]
   (let [msg (if (= status 'ok)
               {:message "Successfully joined! Waiting for other players ..."}
               {:message data})]
     (send-channel channel msg)
     (if (everyone-arrived? data)
       (send-display data)))))

(defn disconnect! [channel status]
  (remove-player! channel))

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
