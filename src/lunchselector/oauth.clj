(ns lunchselector.oauth
  (:require [ring.util.codec :as codec]
            [ring.util.response :as res]
            [lunchselector.utils :as utils]
            [buddy.sign.jwt :as jwt]
            [clj-time.core :as time]
            [clj-time.coerce :as time-coerce]))

(defn google-oauth-redirect-uri
  "Creates the URI from which to request the OAuth code from google"
  []
  (str (utils/get-config :google-oauth-code-uri)
       "scope="            (codec/url-encode (utils/get-config :google-oauth-scope))
       "&redirect_uri="    (codec/url-encode (utils/get-config :google-oauth-redirect-uri))
       "&client_id="       (codec/url-encode (utils/get-config :google-oauth-client-id))
       "&response_type="   (codec/url-encode (utils/get-config :google-oauth-response-type))
       "&approval_prompt=" (codec/url-encode (utils/get-config :google-oauth-approval-prompt))))

(defn google-oauth-token-uri
  "Creates the URI from to request Google's OAuth API for authentication"
  []
  (str (utils/get-config :google-oauth-token-uri)))

(defn google-oauth-token-params
  "Creates a map of the parameters to be passed for Google OAuth"
  [code]
  {:form-params
   {:code          code
    :client_id     (utils/get-config :google-oauth-client-id)
    :client_secret (utils/get-config :google-oauth-client-secret)
    :redirect_uri  (utils/get-config :google-oauth-redirect-uri)
    :grant_type    (utils/get-config :google-oauth-grant-type)}})


(defn google-user-details-uri
  "Creates the URI to fetch the google account details"
  [token]
  (str (utils/get-config :google-oauth-user-info-uri)
       "access_token=" token))

(defn validate-token
  [access-token]
  (let [user-response (utils/get-request (google-user-details-uri access-token))
        user-details (utils/parse-response-body-map user-response)]
    (if (or (contains? user-details :error) (nil? (re-find #"@nilenso.com$" (:email user-details))))
      false
      true)))

(defn unsign-cookie
  [value]
  (try
    (jwt/unsign value (utils/get-config :app-secret-key))
    (catch Exception e nil)))

(defn authenticated?
  [request]
  (let [cookie-val (unsign-cookie (get-in request [:cookies "auth" :value]))]
    (if (or (nil? cookie-val)
            (time/after? (time/now) (time-coerce/from-long (:cookie-expiry cookie-val))))
      false
      true)))

(defn wrap-authentication
  [handler]
  (fn
    [request]
    (if (or (contains? #{"/login" "/unauthorized"} (:uri request))
            (authenticated? request))
      (handler request)
      (res/redirect (google-oauth-redirect-uri)))))

(defn wrap-unsign-cookie
  [handler]
  (fn
    [request]
    (handler (assoc request :unsigned-cookie (unsign-cookie (get-in request [:cookies "auth" :value]))))))
