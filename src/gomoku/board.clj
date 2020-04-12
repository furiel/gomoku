(ns gomoku.board
  (:gen-class))

(defonce game (atom {:board {} :players {}}))

(defn reset-game! [] (reset! game {:board {} :players {}}))

(defn update-player-or-error [game channel player error]
  (if (get-in game [:players channel])
    (do (reset! error "already exists")
        game)
    (if (some #{player} (vals (:players game)))
      (do (reset! error "player exists with another channel")
          game)
      (assoc-in game [:players channel] player))))

(defn add-player! [channel player]
  (let [error (atom nil)
        new (swap! game #(update-player-or-error %1 channel player error))]
    (if @error
      ['nok "another player already exists"]
      ['ok new])))

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
      ['nok "already exists"]
      ['ok new])))
