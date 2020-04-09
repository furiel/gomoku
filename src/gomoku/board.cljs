(ns gomoku.board
  (:require
   [reagent.core :as r]))

(def player-to-color {:x "green" :o "red"})
(def table (r/atom {}))

(defn draw-me [x y]
  (swap! table assoc [x y] :x))

(defn cell [x y on-click]
  [:td
   {:style {:cursor "pointer"
            :text-align "center"
            :border-style "solid"
            :border-width 1
            :width 20
            :height 20
            :background-color (let [player (get @table [x y])] (and player (player-to-color player)))
            }
     :on-click #(on-click x y)
    }
   ])

(defn row [n size on-click]
  (into [:tr]
        (for [y0 (range size)]
          (cell n y0 on-click))))

(defn board [{[x y] :dimension on-click :on-click}]
  [:table
   (into [:tbody]
         (for [x0 (range x)]
           (row x0 y on-click)))])
