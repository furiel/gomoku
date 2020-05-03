(ns gomoku.core
  (:require
   [taoensso.timbre :refer [info]]
   [gomoku.routes :refer [route]]
   [gomoku.board :refer [start-new-game stop-game]]
   [org.httpkit.server :refer [run-server]])
  (:gen-class))

(defonce server (atom {}))

(defn start-server [port]
  (reset! server
          {:server (run-server
                    (route)
                    {:port port})
           :port port}))

(defn stop-server []
  (when (:server @server)
    (info "stopping server")
    ((:server @server) :timeout 100)
    (swap! server assoc :server nil)))

(defn restart-server []
  (info "restarting server")
  (stop-server)
  (start-server (:port @server)))

(defn -main [& args]
  (start-server 3000)
  (info "gomoku server started"))
