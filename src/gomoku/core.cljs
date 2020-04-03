(ns gomoku.core
  (:require
   [reagent.dom :as r]))

(defn page []
  [:div "Hello world"])

(r/render (page) (.getElementById js/document "app"))
