(ns gomoku.websockets)

(defonce ws-chan (atom nil))

(defn log [& args] (apply (.-log js/console) args))

(defn send-msg! [msg]
  (if @ws-chan
    (.send @ws-chan msg)
    (throw (js/Error. "Websocket is not available!"))))

(defn make-websocket! [url start-game handle-message]
  (if-let [chan (js/WebSocket. url)]
    (do
      (set! (.-onerror chan) (fn [error-event] (throw error-event)))
      (set! (.-onopen chan) (fn [event] (start-game)))
      (set! (.-onmessage chan) handle-message)
      (reset! ws-chan chan)
      (log "Websocket connection established with: " url))
    (throw (js/Error. "Websocket creation failed"))))
