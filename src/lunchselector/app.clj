(ns lunchselector.app
  (:gen-class)
  (:require [org.httpkit.server :as server]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [lunchselector.oauth :refer [wrap-authentication wrap-unsign-cookie]]
            [bidi.ring :as bidi]
            [lunchselector.utils :as utils]
            [lunchselector.core :as core]))

(def handler
  (bidi/make-handler
   ["/" { "" core/home
          "restaurants" core/restaurants
          "login" core/login
          "vote" core/vote
          "result" core/result
          "add-offline-restaurants" core/add-offline-restaurants
          "slack" core/slack
          "unauthorized" core/unauthorized}]))

(def lunch-app
  (-> handler
      wrap-authentication
      wrap-unsign-cookie
      wrap-cookies
      wrap-params
      wrap-session))

(defn -main []
  (utils/initialize-app-configuration)
  (println "starting server @ localhost:3000")
  (server/run-server lunch-app {:port (utils/get-config :server-port)}))
