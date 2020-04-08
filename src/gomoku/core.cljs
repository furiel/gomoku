(ns gomoku.core
  (:require
   [reagent.dom :as rd]
   [reagent.core :as r]
   [gomoku.board :as board]))

(defn page []
  [:div
   [:div "Board"]
   [:div (board/board 10 10)]])

(rd/render [page] (.getElementById js/document "app"))
