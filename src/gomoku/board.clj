(ns gomoku.board
  (:require [clojure.set]
            [gomoku.event-loop :refer [start-event-loop stop-event-loop] :as event-loop])
  (:gen-class))

(defn new-game [notify-player] {:notify notify-player :board {} :players {}})

(def error? symbol?)

(defn notify! [game channel msg]
  ((:notify game) channel msg))

(defn update-player [game channel]
  (if (some #{channel} (-> game :players keys))
    'already-present
    (let [current-players (-> game :players vals set)
          player (first (apply disj #{:x :o} current-players))]
      (if player
        (assoc-in game [:players channel] player)
        'too-many-players))))

(defn add-player [game channel]
  (update-player game channel))

(defn channel-to-player [game channel]
  (get (:players game) channel))

(defn move [game channel coords]
  (if (get (:board game) coords)
    'already-exists
    (let [color (channel-to-player game channel)]
      (update game :board
              #(assoc %1 coords color)))))

(defn remove-player [game channel]
  (update game :players (fn [orig] (dissoc orig channel))))

(defn get-channels [game]
  (-> game :players keys))

(defn display-message [game channel]
  {:event 'display :dimension [10 10] :player (channel-to-player game channel) :next-player (:next-player game)})

(defn everyone-arrived? [game]
  (= 2 (count (get-channels game))))

(defn send-display [game]
  (doseq [channel (get-channels game)]
    (notify! game channel (display-message game channel))))

(defn other-player [game channel]
  (first (vals (dissoc (:players game) channel))))

(defn init-next-player [game channel]
  (let [player (other-player game channel)]
    (assoc game :next-player player)))

(defn handle-connect-event [game event]
  (let [channel (:channel event)
        player-added (add-player game channel)]
    (let [msg (if (error? player-added)
                {:event 'message :message player-added}
                {:event 'message :message "Successfully joined! Waiting for other players ..."})
          game (if (error? player-added) game player-added)
          game (or (when (everyone-arrived? game)
                     (init-next-player game channel)) game)]
      (notify! game channel msg)
      (if (everyone-arrived? game)
        (send-display game))
      game)))

(defn handle-disconnect-event [game event]
  (remove-player game (:channel event)))

(defn notify-clients [game msg]
  (doseq [channel (get-channels game)]
    (notify! game channel msg)))

(defn direction [x0 y0 dx dy]
  (lazy-seq (cons [x0 y0] (direction (+ x0 dx) (+ y0 dy) dx dy))))

(defn inverse [x0 y0 dx dy]
  [x0 y0 (- dx) (- dy)])

(defn get-line [game color line]
  (letfn [(same-color [coord] (= color (get (:board game) coord)))]
    (clojure.set/union
     (set (take-while same-color (apply direction line)))
     (set (take-while same-color (apply direction (apply inverse line)))))))

(defn winning-move? [game channel coords]
  (let [color (channel-to-player game channel)
        lines (map #(into coords %1) '([0 1] [1 0] [1 1] [-1 1]))
        candidates (map (partial get-line game color) lines)
        largest (apply max-key count candidates)]
    (if (>= (count largest) 5)
      largest
      nil)))

(defn execute-move-command [game channel msg]
  (let [player (channel-to-player game channel)
        updated-game (assoc game :next-player (other-player game channel))]
    (notify-clients updated-game (assoc msg :player player :next-player (:next-player updated-game)))
    (if (winning-move? updated-game channel (:point msg))
      (do (notify-clients updated-game {:event 'game-finished :winner player})
          'imminent-shutdown)
      updated-game)))

(defn handle-move-command [game channel msg]
  (let [moved (move game channel (:point msg))]
    (if (error? moved)
      (do (notify! game channel {:event 'message :message moved})
          game)
      (execute-move-command moved channel msg))))

(defn handle-read-event [game event]
  (let [{channel :channel msg :msg} event]
    (if (= (:next-player game) (channel-to-player game channel))
      (handle-move-command game channel msg)
      (do
        (notify! game channel {:event 'message :message 'not-your-turn})
        game))))

(defn start-new-game [callbacks]
  (start-event-loop
   (new-game (:notify callbacks))
   {'connect handle-connect-event
    'disconnect handle-disconnect-event
    'read handle-read-event}))

(defn stop-game [event-loop]
  (when event-loop
    (stop-event-loop event-loop)))

(defn put-event! [game event]
  (event-loop/put-event! game event))
