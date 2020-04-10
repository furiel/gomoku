(ns gomoku.core
  (:require
   [org.httpkit.server :refer [run-server send! with-channel on-close on-receive]]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [ring.util.response :refer [resource-response content-type]])
  (:gen-class))

(defn handler [req]
  (or
   (when (= "/" (:uri req))
     (some-> (resource-response "index.html" {:root "public"})
             (content-type "text/html; charset=utf-8")))
   {:status 404
    :headers {"Content-Type" "text/html"}
    :body "Not found"}))

(defn -main []
  (run-server
   (wrap-defaults handler site-defaults)
   {:port 3000}))
