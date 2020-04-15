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
      (is (= (-> remove-1 :players keys set) #{2})))))
