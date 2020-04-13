(ns gomoku.board
  (:gen-class))

(defonce game (atom {:board {} :players {}}))

(defn reset-game! [] (reset! game {:board {} :players {}}))

(defn update-player-or-error [game channel error]
  (if (some #{channel} (-> game :players keys))
    (do (reset! error 'already-present)
        game)
    (let [current-players (-> game :players vals set)
          player (first (apply disj #{:x :o} current-players))]
      (if player
        (assoc-in game [:players channel] player)
        (do (reset! error 'too-many-players)
            game)))))

(defn add-player! [channel]
  (let [error (atom nil)
        new (swap! game #(update-player-or-error %1 channel error))]
    (if @error
      {:status 'nok :data @error}
      {:status 'ok :data new})))

(defn update-or-error [m k v error]
  (if (get m k)
    (do (reset! error 'already-exists) m)
    (assoc m k v)))

(defn move! [channel coords]
  (let [error (atom nil)
        color (get-in @game [:players channel])
        new (swap! game update :board
                   #(update-or-error %1 coords color error))]
    (if @error
      {:status 'nok :data @error}
      {:status 'ok :data new})))

(defn remove-player! [channel]
  (swap! game update :players (fn [orig] (dissoc orig channel))))

(defn get-channels []
  (-> @game :players keys))

(defn channel-to-player [channel]
  (get (:players @game) channel))

(defn display-message [channel]
  {:display {:dimension [10 10] :player (channel-to-player channel) :next-player :o}})
