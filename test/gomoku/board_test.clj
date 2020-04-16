(ns gomoku.board-test
  (:require [clojure.test :refer :all]
            [gomoku.board :refer :all]))

(deftest test-game
  (testing "add-player"
    (let [game (new-game (constantly nil))
          with-player1 (add-player game 1)
          do-not-readd-player1 (add-player (:next with-player1) 1)
          with-player2 (add-player (:next do-not-readd-player1) 2)]
      (is (= 'ok (:status with-player1)))
      (is (= 'nok (:status do-not-readd-player1)))
      (is (= 'already-present (:error do-not-readd-player1)))
      (is (not= (channel-to-player (:next with-player2) 1) (channel-to-player (:next with-player2) 2)))))

  (testing "remove-player"
    (let [game (new-game (constantly nil))
          everyone (:next (add-player (:next (add-player game 1)) 2))
          remove-1 (remove-player everyone 1)]
      (is (= (-> remove-1 :players keys set) #{2}))))

  (testing "move"
    (let [game (new-game (constantly nil))
          everyone (:next (add-player (:next (add-player game 1)) 2))]
      (let [first-move (move! everyone 1 [1 1])
            already-occupied-1 (move! (:next first-move) 1 [1 1])
            already-occupied-2 (move! (:next already-occupied-1) 2 [1 1])
            ok-1 (move! (:next already-occupied-2) 1 [2 2])
            ok-2 (move! (:next ok-1) 2 [3 3])]
        (is (= 'ok (:status first-move)))
        (is (= 'ok (:status ok-1)))
        (is (= 'ok (:status ok-2)))
        (is (= 'nok (:status already-occupied-1)))
        (is (= 'nok (:status already-occupied-2)))
        (is (= 'already-exists (:error already-occupied-1)))
        (is (= 'already-exists (:error already-occupied-2)))))))
