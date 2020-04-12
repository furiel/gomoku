(ns gomoku.board-test
  (:require [clojure.test :refer :all]
            [gomoku.board :refer :all]))

(defn setup [f]
  (reset-game!)
  (f))

(clojure.test/use-fixtures :each setup)

(deftest board-test
  (testing "board-test"
    (is (= 'ok (first (add-player! 1 :x))))
    (is (= 'nok (first (add-player! 1 :x))))
    (is (= 'nok (first (add-player! 2 :x))))
    (is (= 'ok (first (add-player! 2 :o))))

    (is (= 'ok (first (move! 1 [1 1]))))
    (is (= 'nok (first (move! 1 [1 1]))))
    (is (= 'nok (first (move! 2 [1 1]))))
    (is (= 'ok (first (move! 2 [2 2]))))))
