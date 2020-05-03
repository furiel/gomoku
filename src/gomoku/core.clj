(ns gomoku.core
  (:require
   [taoensso.timbre :refer [info error set-level! spy]]
   [clojure.tools.cli :refer [parse-opts]]
   [gomoku.routes :refer [route]]
   [gomoku.board :refer [start-new-game stop-game]]
   [org.httpkit.server :refer [run-server]])
  (:gen-class))

(defonce server (atom {}))

(def log-levels ["trace" "debug" "info" "warn" "error"])
(def cli-options
  [["-p" "--port PORT" "Port number"
    :default 3000
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   [nil "--log-level LOGLEVEL" (format "Sets the log level. Possible values: %s" (clojure.string/join ", " log-levels))
    :default :info
    :default-desc "info"
    :parse-fn keyword
    :validate [#(contains? (set (map keyword log-levels)) %) (format "Must be in %s" log-levels)]]
   ["-h" "--help"]])

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
  (let [cli-arguments (parse-opts args cli-options)]
    (if-let [errors (:errors cli-arguments)]
      (doseq [err errors]
        (println err))
        (let [options (:options cli-arguments)]
          (if (:help options)
            (println (format "Options:\n%s" (:summary cli-arguments)))
            (do
              (set-level! (:log-level options))
              (start-server (:port options))
              (info "gomoku server started")))))))
