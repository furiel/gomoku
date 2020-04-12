(ns gomoku.core
  (:require
   [gomoku.websockets :refer [wrap-ws]]
   [org.httpkit.server :refer [run-server]]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [ring.util.response :refer [resource-response content-type]])
  (:gen-class))

(defonce server (atom nil))

(defn root-handler [req]
  (or
   (when (= "/" (:uri req))
     (some-> (resource-response "index.html" {:root "public"})
             (content-type "text/html; charset=utf-8")))
   {:status 404
    :headers {"Content-Type" "text/html"}
    :body "Not found"}))

(defn default-handler []
  (wrap-defaults root-handler site-defaults))

(defn stop-server []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn -main []
  (reset! server
          (run-server
           (wrap-ws
            (default-handler))
           {:port 3000})))
