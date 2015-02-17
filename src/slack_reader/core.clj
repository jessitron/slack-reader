(ns slack-reader.core
  (:require [schema.core :as s]
            [clj-http.client :as client]
           [slack-reader.schema :as t]))

(def token "xoxp-2185251080-2332259617-3744086818-6cb514")
(defn slack-url [endpoint]
  (str "https://slack.com/api/" endpoint))

(s/defn call-slack :- t/SlackResponse
  ([endpoint :- s/Str]
     (call-slack endpoint {}))
  ( [endpoint :- s/Str
     parameters :- {s/Keyword s/Str}]
      (:body (client/get (slack-url endpoint) {:query-params (merge parameters {:token token})
                                               :as :json}))))

(s/defn channel-exists? :- s/Bool [name :- s/Str]
  true)

(s/defn ok :- s/Bool [response :- t/SlackResponse]
  (:ok response))
