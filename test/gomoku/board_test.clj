(ns gomoku.board-test
  (:require [clojure.test :refer :all]
            [gomoku.board :refer :all]))

(defn setup [f]
  (reset-game!)
  (f))

(clojure.test/use-fixtures :each setup)

(deftest board-test
  (testing "board-test"
    (is (= 'ok (:status (add-player! 1 :x))))
    (is (= {:status 'nok :data 'already-exists} (add-player! 1 :x)))
    (is (= {:status 'nok :data 'already-taken} (add-player! 2 :x)))
    (is (= 'ok (:status (add-player! 2 :o))))

    (is (= 'ok (:status (move! 1 [1 1]))))
    (is (= {:status 'nok :data 'already-exists} (move! 1 [1 1])))
    (is (= {:status 'nok :data 'already-exists} (move! 2 [1 1])))
    (is (= 'ok (:status (move! 2 [2 2]))))))
