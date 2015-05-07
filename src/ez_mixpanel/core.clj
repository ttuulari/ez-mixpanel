(ns ez-mixpanel.core
  (:require [cheshire.core :as json]
            [org.httpkit.client :as client])
  (:import [org.apache.commons.codec.binary Base64]
           [java.util Date Calendar TimeZone])
  (:refer-clojure :exclude [set]))

(def track-url "http://api.mixpanel.com/track")
(def engage-url "http://api.mixpanel.com/engage")

(defn timestamp
  "Factors in timezone but not DST"
  []
  (let [calendar (Calendar/getInstance (TimeZone/getDefault))]
    (apply str (drop-last 3 (str (.. calendar getTime getTime))))))

(defn encode
  ([^String str]
     (encode str false false))
  ([^String str chunked url-safe]
     (String. (Base64/encodeBase64 (.getBytes str) chunked url-safe))))

(defn track
  "Track event. Set :time in data to false to let mixpanel set the time of the event"
  ([token distinct-id event opts data]
     (track token distinct-id event opts data nil))
  ([token distinct-id event {:keys [time] :as opts} data cb]
     {:pre [(not (nil? token))
            (not (nil? event))
            (not (nil? distinct-id))]}
     (let [distinct-id (str distinct-id)
           time? (not (false? time))
           opts {:user-agent "ez mixpanel"
                 :query-params {:data
                                (->> (merge
                                      {:distinct_id distinct-id
                                       :token token
                                       :event event
                                       :properties data}
                                      (dissoc opts :time)
                                      (if time?
                                        {:time (or time (timestamp))}))
                                     json/generate-string
                                     encode)}}]
       (client/post track-url opts cb))))


(defn engage
  ([token distinct-id opts operation value]
     (engage token distinct-id opts nil))
  ([token distinct-id opts operation value cb]
     (let [opts {:user-agent "ez mixpanel"
                 :query-params {:data (->>
                                       (merge
                                        {:$token token
                                         :$distinct_id distinct-id}
                                        (reduce (fn [out [k v]]
                                                  (assoc out (str "$" (name k)) v))
                                                {} opts)
                                        {(str "$" (name operation)) value})
                                       json/generate-string
                                       encode)}}]
      (client/post engage-url opts cb))))

(defn set
  "Set properties for a user profile"
  ([token distinct-id opts data]
     (set token distinct-id opts data nil))
  ([token distinct-id opts data cb]
     (engage token distinct-id opts :set data cb)))

(defn set-once
  "Set properties for a user profile once (non-destructive)"
  ([token distinct-id opts data]
     (set-once token distinct-id opts data nil))
  ([token distinct-id opts data cb]
     (engage token distinct-id opts :set_once data cb)))

(defn add
  "Add numerical values to a user profile. Data must be a hashmap with keys and numerical values. Added to the profile. Substraction can be done by adding negative values"
  ([token distinct-id opts data]
     (add token distinct-id opts data nil))
  ([token distinct-id opts data cb]
     (engage token distinct-id opts :add data cb)))

(defn append
  "Append values to a user profile. Data must be a hashmap with keys and values"
  ([token distinct-id opts data]
     (append token distinct-id opts data nil))
  ([token distinct-id opts data cb]
     (engage token distinct-id opts :append data cb)))

(defn union
  "Perform a union on the user profile and a hashmap of keys and a list of corresponding values"
  ([token distinct-id opts data]
     (union token distinct-id opts data nil))
  ([token distinct-id opts data cb]
     (engage token distinct-id opts :union data cb)))

(defn unset
  "Permanently remove "
  ([token distinct-id opts data]
     (unset token distinct-id opts data nil))
  ([token distinct-id opts data cb]
     (engage token distinct-id opts :unset data cb)))

(defn delete
  ([token distinct-id opts]
     (delete token distinct-id opts nil))
  ([token distinct-id opts cb]
     (engage token distinct-id opts :delete "" cb)))
