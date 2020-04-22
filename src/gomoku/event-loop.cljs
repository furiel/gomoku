(ns gomoku.event-loop
  (:require
   [reagent.dom :as rd]
   [reagent.core :as r]
   [gomoku.board :refer [board set-player! set-next-player! player]]
   [cljs.core.async :refer [chan put! <! go go-loop close!]]
   [gomoku.websockets :refer [send-msg!]]
   [gomoku.board :refer [draw-me!]]))

(def event-queue (chan))

(defn set-message! [message]
  (rd/render message (.getElementById js/document "message")))

(defn click-event [x y]
  (send-msg!
   {:event 'click :point [x y]}))

(defn handle-click-event [event]
  (let [{[x y] :point who :player next-player :next-player} event]
    (set-next-player! next-player)
    (draw-me! x y who)))

(defn get-board-element []
  (.getElementById js/document "board"))

(defn handle-display-event [event]
  (let [{[x y] :dimension player :player next-player :next-player} event]
    (set-player! player)
    (set-next-player! next-player)
    (rd/render
     [(board {:dimension [x y] :on-click click-event})]
     (get-board-element))))

(defn handle-game-finished [event event-queue]
  (set-message! (str "Game finished. " (if (= @player (:winner event)) "You" "The other player") " won!"))
  (set-next-player! nil)
  (close! event-queue))

(defn start-event-loop []
  (go-loop [event (<! event-queue)]
    (cond
      (= (:event event) 'click) (handle-click-event event)
      (= (:event event) 'display) (handle-display-event event)
      (= (:event event) 'message) nil
      (= (:event event) 'game-finished) (handle-game-finished event event-queue)
      :else (js/alert (str "Unknown event: " (:event event))))
    (when (not= (:event event) 'game-finished)
      (set-message! (:message event))
      (recur (<! event-queue)))))

(defn handle-message! [msg]
  (put! event-queue msg))
