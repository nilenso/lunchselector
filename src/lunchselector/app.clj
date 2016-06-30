(ns lunchselector.app
  (:gen-class)
  (:require [org.httpkit.server :as server]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.middleware.reload :refer [wrap-reload]]
            [lunchselector.oauth :refer [wrap-authentication wrap-unsign-cookie]]
            [bidi.ring :as bidi]
            [lunchselector.utils :as utils]
            [lunchselector.core :as core]
            [clojure.tools.nrepl.server :as nrepl-server]
            [cider.nrepl :refer (cider-nrepl-handler)]))

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
  (nrepl-server/start-server :port 3001 :handler cider-nrepl-handler) ;; start an nrepl server
  (server/run-server (wrap-reload #'lunch-app)
                     {:port (utils/get-config :server-port)}))
