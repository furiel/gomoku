(ns gomoku.websockets-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :refer [chan put! alts!! close! timeout]]
            [gomoku.websockets :refer :all]
            [gomoku.board :refer :all]))

(defn pop-message! [msg-queue]
  (let [[msg t] (alts!! [msg-queue (timeout 1000)])]
    (is (not (nil? msg)))
    msg))

(deftest test-websockets
  (testing "test-websockets"
    (let [q1 (chan 10)
          q2 (chan 10)
          notify! (fn [channel msg] (put! (if (= channel 1) q1 q2) msg))
          game (start-new-game {:notify notify!})]
      (connect! game 1)
      (is (= (:message (pop-message! q1)) "Successfully joined! Waiting for other players ..."))
      (connect! game 2)
      (is (= (:message (pop-message! q2)) "Successfully joined! Waiting for other players ..."))

      (is (= (:event (pop-message! q1)) 'display))
      (is (= (:event (pop-message! q2)) 'display))

      (dotimes [n 4]
        (read-event game 1 {:event 'click :point [0 n]})
        (is (= (:event (pop-message! q1)) 'click))
        (is (= (:event (pop-message! q2)) 'click))

        (read-event game 2 {:event 'click :point [1 n]})
        (is (= (:event (pop-message! q1)) 'click))
        (is (= (:event (pop-message! q2)) 'click)))

      (read-event game 1 {:event 'click :point [0 4]})
      (is (= (:event (pop-message! q1)) 'click))
      (is (= (:event (pop-message! q2)) 'click))

      (is (= (:event (pop-message! q1)) 'game-finished))
      (is (= (:event (pop-message! q2)) 'game-finished))

      (stop-game game))))
