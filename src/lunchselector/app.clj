(ns lunchselector.app
  (:gen-class)
  (:require [org.httpkit.server :as server]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.json :refer [wrap-json-body]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.resource :refer [wrap-resource]]
            [lunchselector.oauth :refer [wrap-authentication wrap-unsign-cookie]]
            [bidi.ring :as bidi :refer [->Resources ->Files]]
            [lunchselector.utils :as utils]
            [lunchselector.core :as core]
            [clojure.tools.nrepl.server :as nrepl-server]
            [cider.nrepl :refer (cider-nrepl-handler)]
            [lunchselector.api :as api]))

(def handler
  (bidi/make-handler
   ["/" { "" core/index
         "restaurants" core/restaurants
         "login" core/login
         "vote" core/vote
         "result" core/result
         "add-offline-restaurants" core/add-offline-restaurants
         "slack" core/slack
         "unauthorized" core/unauthorized
         "api/" {"restaurants" api/restaurants
                 "popular-restaurant" api/popular-restaurant
                 "vote" {:get api/get-user-votes
                         :post api/add-votes
                         :delete api/remove-user-votes}
                 "current-votes" api/current-votes
                 "location" api/location
                 "update-restaurants"{:get api/update-online-restaurants
                                      :post api/add-offline-restaurant}}}]))

(def lunch-app
  (-> handler
      (wrap-resource "public")
      wrap-json-body
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
