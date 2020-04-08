(ns gomoku.board
  (:require
   [reagent.core :as r]))

(def player-to-color {:x "green" :o "red"})
(def table (r/atom {}))

(defn cell [x y]
  [:td
   {:style {:cursor "pointer"
            :text-align "center"
            :border-style "solid"
            :border-width 1
            :width 20
            :height 20
            :background-color (let [player (get @table [x y])] (and player (player-to-color player)))
            }
     :on-click #(swap! table assoc [x y] :x)
    }
   ])

(defn row [n size]
  (into [:tr]
        (for [y0 (range size)]
          (cell n y0))))

(defn board [x y]
  [:table
   (into [:tbody]
         (for [x0 (range x)]
           (row x0 y)))])
