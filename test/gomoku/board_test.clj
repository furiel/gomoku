(ns gomoku.board-test
  (:require [clojure.test :refer :all]
            [gomoku.lib :refer [error?]]
            [gomoku.board :refer :all]))

(defn- with-all-players []
  (let [game (new-game (constantly nil))
        with-everyone (add-player (add-player game 1) 2)]
    with-everyone))

(deftest test-game
  (testing "add-player-simple"
    (let [game (new-game (constantly nil))
          with-player1 (add-player game 1)]
      (is (not (error? with-player1)))))

  (testing "do-not-readd-player"
    (let [game (new-game (constantly nil))
          with-player1 (add-player game 1)
          do-not-readd-player1 (add-player with-player1 1)]
      (is (= 'already-present do-not-readd-player1))))

  (testing "add-two-players"
    (let [game (new-game (constantly nil))
          with-player1 (add-player game 1)
          with-player2 (add-player with-player1 2)]
      (is (not (error? with-player2)))
      (is (not= (channel-to-player with-player2 1) (channel-to-player with-player2 2)))))

  (testing "remove-player"
    (let [game (new-game (constantly nil))
          everyone (add-player (add-player game 1) 2)
          remove-1 (remove-player everyone 1)]
      (is (= (-> remove-1 :players keys set) #{2}))))

  (testing "move-simple"
    (let [game (with-all-players)
          first-move (move game 1 [1 1])
          second-move (move first-move 2 [2 2])]
      (is (not (error? first-move)))
      (is (contains? (:board first-move [1 1]) [1 1]))
      (is (not (error? second-move)))
      (is (contains? (:board second-move [1 1]) [1 1]))
      (is (contains? (:board second-move [2 2]) [1 1]))))

  (testing "already-occupied"
    (let [game (with-all-players)
          first-move (move game 1 [1 1])
          already-occupied-1 (move first-move 1 [1 1])
          already-occupied-2 (move first-move 2 [1 1])]
      (is (= 'already-exists already-occupied-1))
      (is (= 'already-exists already-occupied-2)))))
