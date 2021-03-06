(ns gomoku.routes
  (:require
   [gomoku.websockets :refer [wrap-ws]]
   [taoensso.timbre :refer [debug]]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.util.response :refer [resource-response content-type]])
  (:gen-class))

(defn- root-handler [req]
  (or
   (when (= "/" (:uri req))
     (some-> (resource-response "index.html" {:root "public"})
             (content-type "text/html; charset=utf-8")))
   {:status 404
    :headers {"Content-Type" "text/html"}
    :body "Not found"}))

(defn- default-handler []
  (wrap-defaults root-handler site-defaults))

(defn- wrap-log [handler]
  (fn [req]
    (debug req)
    (handler req)))

(defn route []
  (-> (default-handler)
      wrap-ws
      wrap-log
      wrap-params))
