(ns gomoku.websockets
  (:require
   [org.httpkit.server :refer [send! with-channel on-close on-receive]])
  (:gen-class))

(defonce channels (atom #{}))

(defn connect! [channel]
 (swap! channels conj channel))

(defn disconnect! [channel status]
 (swap! channels #(remove #{channel} %)))

(defn notify-clients [msg]
 (doseq [channel @channels]
     (send! channel msg)))

(defn ws-handler [request]
  (with-channel request channel
    (connect! channel)
    (on-close channel (partial disconnect! channel))
    (on-receive channel #(notify-clients %))))

(defn wrap-ws [handler]
  (fn [req]
    (if (clojure.string/starts-with? (:uri req) "/ws")
      (ws-handler req)
      (handler req))))
