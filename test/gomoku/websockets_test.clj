(ns gomoku.websockets-test
  (:require [clojure.test :refer :all]
            [gomoku.websockets :refer :all]
            [gomoku.board :refer :all]))

(deftest test-websockets
  (testing "test-websockets"
    (let [msg-queue-1 (atom nil)
          msg-queue-2 (atom nil)
          notify! (fn [channel msg] (reset! (if (= channel 1)
                                                  msg-queue-1 msg-queue-2) msg))
          game (start-new-game {:notify notify!})]
      (connect! game 1)
      (is (= 'done (sync-game game)))
      (is (= (:message @msg-queue-1) "Successfully joined! Waiting for other players ..."))
      (stop-game game))))
