(ns gomoku.board
  (:require [clojure.set])
  (:gen-class))

(defn new-game [notify-player] {:notify notify-player :board {} :players {}})

(defn notify! [game channel msg]
  ((:notify game) channel msg))

(defn update-player [game channel]
  (if (some #{channel} (-> game :players keys))
    {:status 'nok :error 'already-present :next game}
    (let [current-players (-> game :players vals set)
          player (first (apply disj #{:x :o} current-players))]
      (if player
        {:status 'ok :next (assoc-in game [:players channel] player)}
        {:status 'nok :error 'too-many-players :next game}))))

(defn add-player [game channel]
  (update-player game channel))

(defn update-or-error [m k v error]
  (if (get m k)
    (do (reset! error 'already-exists) m)
    (assoc m k v)))

(defn channel-to-player [game channel]
  (get (:players game) channel))

(defn move! [game channel coords]
  (if (get (:board game) coords)
    {:status 'nok :error 'already-exists :next game}
    (let [color (channel-to-player game channel)]
      {:status 'ok :next (update game :board
                                 #(assoc %1 coords color))})))

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

(defn handle-connect-event [game channel]
  (let [result (add-player game channel)
        {status :status error :error next-game :next} result]
    (let [msg (if (= status 'ok)
                {:event 'message :message "Successfully joined! Waiting for other players ..."}
                {:event 'message :message error})
          next-game (or (when (everyone-arrived? next-game)
                          (init-next-player next-game channel)) next-game)]
      (notify! game channel msg)
      (if (everyone-arrived? next-game)
        (send-display next-game))
      next-game)))

(defn handle-disconnect-event [game channel]
  (remove-player game channel))

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
    (when (winning-move? updated-game channel (:point msg))
      (notify-clients updated-game {:event 'message :message "GG"}))
      updated-game))

(defn handle-move-command [game channel msg]
  (let [moved (move! game channel (:point msg))
        next-game (:next moved)]
    (if (= 'nok (:status moved))
        (do (notify! next-game channel {:event 'message :message (:error moved)})
            next-game)
        (execute-move-command next-game channel msg))))

(defn handle-read-event [game channel msg]
  (if (= (:next-player game) (channel-to-player game channel))
    (handle-move-command game channel msg)
    (do
      (notify! game channel {:event 'message :message 'not-your-turn})
      game)))
