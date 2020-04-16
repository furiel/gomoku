(ns gomoku.board
  (:require
   [reagent.core :as r]))

(def player-to-color {:x "green" :o "red"})
(def table (r/atom {}))
(def player (r/atom nil))
(def next-player (r/atom nil))

(defn set-player! [p]
  (reset! player p))

(defn set-next-player! [p]
  (reset! next-player p))

(defn draw-me! [x y who]
  (swap! table assoc [x y] who))

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
  (fn []
    [:div
     [:div
      [:label (when @player "You are: ")]
      [:label#player {:style {:color (player-to-color @player)}} (when @player (player-to-color @player))]]
     [:div [:label (if (= @next-player @player) "Your turn!" "Waiting for other player to move!")]]
     [:table
      (into [:tbody]
            (for [x0 (range x)]
              (row x0 y on-click)))]]))
