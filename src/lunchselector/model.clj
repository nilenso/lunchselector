(ns lunchselector.model
  (:require [cheshire.core :as cheshire]
            [lunchselector.db :as db]
            [lunchselector.utils :as utils]))

;; Used as an annotation while adding restaurants discovered via Zomato
(def zomato "Zomato")

;; API to fetch data from Zomato
(defn get-categories
  "Fetches the list of categories from Zomato"
  []
  (utils/get-request (str (utils/get-config :zomato-api-uri)
                          "categories")
                     {:headers {"user_key" (utils/get-config :zomato-user-key)}
                      :save-request? true}))

(defn- fetch-restaurants
  "Fetch the list of restaurants closest to our location"
  [keyword]
  (utils/get-request (str (utils/get-config :zomato-api-uri)
                          "search")
                     {:accept :json
                      :headers {"user_key" (utils/get-config :zomato-user-key)}
                      :query-params {"q" keyword
                                     "lat" (utils/get-config :org-latitude)
                                     "lon" (utils/get-config :org-longitude)
                                     "entity_id" (utils/get-config :zomato-entity-id)
                                     "entity_type" "city"
                                     "sort" "real_distance"
                                     "order" "desc"
                                     "start" 0
                                     "count" 40}}))

(defn- restaurant-name
  "Get the name of the restaurant"
  [restaurant]
  (get-in restaurant [:restaurant :name]))

(defn- restaurant-rating
  "Get the rating of the restaurant"
  [restaurant]
  (get-in restaurant [:user_rating :aggregate_rating]))

(defn- zomato-id
  "Get the zomato id of the retaurant"
  [restaurant]
  (get-in restaurant [:restaurant :id]))

(defn online-restaurants
  "Gets the list of online restaurants from Zomato"
  ([] (online-restaurants ""))
  ([keyword]
   (let [online-restaurants (fetch-restaurants keyword)
         response-body (utils/parse-response-body-map online-restaurants)]
     (loop [restaurant-list (:restaurants response-body)
            resta ()]
       (if (empty? restaurant-list)
         resta
         (recur (rest restaurant-list)
                (conj resta (restaurant-name (first restaurant-list)))))))))

(defn submit-votes
  "Submits the votes for restaurants for a particular user."
  [email votes]
  (if (coll? votes)
    (doseq [x votes]
      (db/cast-vote-safe {:email email :restaurant x}))
    (db/cast-vote-safe {:email email :restaurant votes})))

(defn submit-votes-api
  "Add the votes to the db with given email and list of restaurant ids"
  [email ids]
  (doseq [rest-id ids]
    (db/cast-votes email rest-id)))


(defn submit-users
  "Submits the user and email to the db"
  [user email]
  (db/add-user-safe {:name user :email email}))

(defn submit-restaurants
  "Adds restaurants safely to the list. Can be used for adding
  online and offline restaurants to the database"
  [restaurant added-by]
  (if (coll? restaurant)
    (doseq [x restaurant] (db/add-restaurant-safe x added-by))
    (db/add-restaurant-safe restaurant added-by)))

(defn submit-online-restaurants
  [rest]
  (submit-restaurants rest zomato))

(defn offline-restaurants
  "Fetches the list of restaurants that were added offline"
  []
  (let [offline-list (db/fetch-offline-restaurants)]
    (map :restaurant offline-list)))

(defn my-votes
  "Fetches the voting history of the user"
  [email]
  (db/fetch-my-votes email))

(defn user-votes-today
  "Fetches the user's vote for today"
  [email]
  (db/fetch-user-votes email))

(defn votes-today
  "Fetches the votes submitted today"
  []
  (db/fetch-votes-for-today))

(defn top-restaurants
  "Fetches the restaurants that were voted the most"
  []
  (db/fetch-popular-restaurants))

(defn popular-restaurant-for-day-of-week
  "Fetches the most popular restaurant for the day of the week"
  []
  (db/get-most-popular-restaurant-for-day-of-week))


(defn remove-user-votes
  "Removes some of the user's votes for the day"
  [email restaurant-ids]
  (doseq [id restaurant-ids]
    (db/remove-user-votes email id)))

(defn current-votes
  "Fetches all the votes submitte today"
  []
  (db/current-votes))

(defn update-restaurants-list
  "Update the restaurants table with data from zomato"
  []
  (let [zomato-response (fetch-restaurants "")
        zomato-restaurants (utils/parse-response-body-map zomato-response)]
    (doseq [restaurant (:restaurants zomato-restaurants)]
      (db/upsert-restaurant {:name (restaurant-name restaurant)
                             :zomato-id (zomato-id restaurant)
                             :rest-type "online"
                             :added-by "Zomato"}))))

(defn add-restaurant
  "Add an offline restaurant"
  [restaurant email]
  (db/upsert-restaurant {:name restaurant
                         :zomato-id nil
                         :rest-type "offline"
                         :added-by email}))

(defn fetch-all-restaurants
  []
  (db/fetch-restaurants))
