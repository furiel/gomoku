(ns gomoku.websockets
  (:require [cognitect.transit :as transit]))

(defonce ws-chan (atom nil))

(defn log [& args] (apply (.-log js/console) args))
(def json-reader (transit/reader :json))
(def json-writer (transit/writer :json))

(defn send-msg! [msg]
  (if @ws-chan
    (.send @ws-chan (transit/write json-writer msg))
    (throw (js/Error. "Websocket is not available!"))))

(defn wrap-json [message-handler]
  (fn [msg]
    (let [parsed (->> msg .-data (transit/read json-reader))]
      (message-handler parsed))))

(defn make-websocket! [url start-game message-handler]
  (if-let [chan (js/WebSocket. url)]
    (do
      (set! (.-onerror chan) (fn [error-event] (throw error-event)))
      (set! (.-onopen chan) (fn [event] (start-game)))
      (set! (.-onmessage chan) (wrap-json message-handler))
      (reset! ws-chan chan)
      (log "Websocket connection established with: " url))
    (throw (js/Error. "Websocket creation failed"))))
