(ns gomoku.core
  (:require [immutant.web :refer :all]
            [immutant.web.async :as async])
  (:gen-class))

(def callbacks
  {:on-message (fn [ch msg]
                 (async/send! ch (.toUpperCase msg)))})

(defn app [request]
  (async/as-channel request callbacks))

(defn -main []
  (run app))
