(ns gomoku.board
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
  {:event 'display :dimension [10 10] :player (channel-to-player game channel) :next-player :o})

(defn everyone-arrived? [game]
  (= 2 (count (get-channels game))))

(defn send-display [game]
  (doseq [channel (get-channels game)]
    (notify! game channel (display-message game channel))))

(defn handle-connect-event [game channel]
  (let [result (add-player game channel)
        {status :status error :error next-game :next} result]
    (let [msg (if (= status 'ok)
                {:event 'message :message "Successfully joined! Waiting for other players ..."}
                {:event 'message :message error})]
      (notify! game channel msg)
      (if (everyone-arrived? next-game)
        (send-display next-game)))
    next-game))

(defn handle-disconnect-event [game channel]
  (remove-player game channel))

(defn notify-clients [game msg]
  (doseq [channel (get-channels game)]
    (notify! game channel msg)))

(defn handle-read-event [game channel msg]
  (let [player (channel-to-player game channel)
        moved (move! game channel (:point msg))
        next-game (:next moved)]
    (if (= 'nok (:status moved))
      (notify! next-game channel {:event 'message :message (:error moved)})
      (notify-clients next-game (assoc msg :player player)))
  next-game))
