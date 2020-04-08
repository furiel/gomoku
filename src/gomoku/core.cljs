(ns gomoku.core
  (:require
   [reagent.dom :as rd]
   [reagent.core :as r]
   [gomoku.board :as board]))

(defn page []
  [:div
   [:div "Board"]
   [:div (board/board 10 10)]])

(defn get-app-element []
  (.getElementById js/document "app"))

(defn mount [el]
  (rd/render [page] el))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount el)))

(mount-app-element)

(defn ^:after-load on-reload []
  (mount-app-element)
)
