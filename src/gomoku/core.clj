(ns gomoku.core
  (:require
   [taoensso.timbre :refer [info]]
   [gomoku.routes :refer [route]]
   [gomoku.board :refer [start-new-game stop-game]]
   [org.httpkit.server :refer [run-server]])
  (:gen-class))

(defonce server (atom nil))

(defn stop-server []
  (when-not (nil? @server)
    (info "stopping server")
    (@server :timeout 100)
    (reset! server nil)))

(defn -main []
  (reset! server
          (run-server
           (route)
           {:port 3000}))
  (info "gomoku server started"))

(defn restart-server []
  (info "restarting server")
  (stop-server)
  (-main))
