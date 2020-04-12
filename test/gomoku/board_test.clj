(ns gomoku.board-test
  (:require [clojure.test :refer :all]
            [gomoku.board :refer :all]))

(defn setup [f]
  (reset-game!)
  (f))

(clojure.test/use-fixtures :each setup)

(deftest board-test
  (testing "board-test"
    (is (= 'ok (:status (add-player! 1))))
    (is (= {:status 'nok :data 'already-present} (add-player! 1)))
    (is (= 'ok (:status (add-player! 2))))

    (is (= 'ok (:status (move! 1 [1 1]))))
    (is (= {:status 'nok :data 'already-exists} (move! 1 [1 1])))
    (is (= {:status 'nok :data 'already-exists} (move! 2 [1 1])))
    (is (= 'ok (:status (move! 2 [2 2]))))))

(deftest test-remove-player
  (testing "remove-player"
    (is (= 'ok (:status (add-player! 1))))
    (is (= 'ok (:status (add-player! 2))))
    (remove-player! 1)
    (is (= (-> @game :players keys set) #{2}))))
