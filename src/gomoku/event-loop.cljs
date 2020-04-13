(ns gomoku.event-loop
  (:require
   [reagent.dom :as rd]
   [reagent.core :as r]
   [gomoku.board :refer [board set-player!]]
   [cljs.core.async :refer [chan put! <! go go-loop]]
   [gomoku.websockets :refer [send-msg!]]
   [gomoku.board :refer [draw-me!]]))

(def event-queue (chan))

(defn set-message! [message]
  (rd/render message (.getElementById js/document "message")))

(defn click-event [x y who]
  (send-msg!
   {:click {:player who
            :point [x y]}}))

(defn handle-click-event [event]
  (let [{{[x y] :point who :player} :click} event]
    (draw-me! x y who)))

(defn get-board-element []
  (.getElementById js/document "board"))

(defn handle-display-event [event]
  (let [{{[x y] :dimension player :player next-player :next-player} :display} event]
    (set-player! player)
    (rd/render
     [(board {:dimension [x y] :on-click click-event})]

     (get-board-element))))

(defn start-event-loop []
  (go-loop [event (<! event-queue)]
    (cond
      (contains? event :click) (handle-click-event event)
      (contains? event :display) (handle-display-event event)
      (contains? event :message) nil
      :else (js/alert (str "Unknown event: " (-> event keys first))))
    (set-message! (:message event))
    (recur (<! event-queue))))

(defn handle-message! [msg]
  (put! event-queue msg))
