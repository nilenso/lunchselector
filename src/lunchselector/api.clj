(ns lunchselector.api
  (:require [lunchselector.model :as model]
            [cheshire.core :as json]
            [lunchselector.utils :as utils]))

(defn restaurants
  "Return a list of map of restaurant id and name"
  [request]
  (let [restaurants (model/fetch-all-restaurants)]
    (utils/build-json-response restaurants)))

(defn popular-restaurant
  "Returns the popular restaurant for the day of week"
  [request]
  (utils/build-json-response (model/popular-restaurant-for-day-of-week)))

(defn add-votes
  "Adds the user votes to the list of restaurants in params"
  [request]
  (let [email (get-in request [:unsigned-cookie :useremail])
        restaurant-ids (get-in request [:body "votes"])]
    (if (every? integer? restaurant-ids)
      (do
        (model/submit-votes-api email restaurant-ids)
        (utils/build-json-response {:success true}))
      (utils/build-json-response {:success false}))))

(defn get-user-votes
  "Get the user's votes for the day"
  [request]
  (let [email (get-in request [:unsigned-cookie :useremail])]
    (utils/build-json-response (model/user-votes-today email))))

(defn remove-user-votes
  "Removes some of the user's vote for the day"
  [request]
  (let [email (get-in request [:unsigned-cookie :useremail])
        restaurant-ids (get-in request [:body "votes"])]
    (if (every? integer? restaurant-ids)
      (do (model/remove-user-votes email restaurant-ids)
          (utils/build-json-response {:success true}))
      (utils/build-json-response {:success false}))))

(defn current-votes
  "Fetches all the votes for the day"
  [request]
  (utils/build-json-response (model/current-votes)))

(defn location
  "Returns current set location coordinates"
  [request]
  (utils/build-json-response {:lat (Float. (utils/get-config :org-latitude))
                              :long (Float. (utils/get-config :org-longitude))}))


(defn update-online-restaurants [request]
  (model/update-restaurants-list)
  (utils/build-json-response {:success true}))

(defn add-offline-restaurant [request]
  (let [restaurant  (get-in request [:body "restaurant"])
        email  (get-in request [:unsigned-cookie :useremail])]
    (model/add-restaurant restaurant email)
    (utils/build-json-response {:success true})))
