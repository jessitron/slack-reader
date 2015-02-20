(ns slack-reader.core
  (:require [schema.core :as s]
            [clj-http.client :as client]
            [clojure.edn]
           [slack-reader.schema :as t]))

(def token (delay (let [token (clojure.edn/read-string (slurp "token.edn"))]
                    (if (nil? token)
                      (throw (Exception. "No token found.
1. Go to https://api.slack.com/web and get yourself a token
2. Put it in a string (quoted) in a file token.edn in the project root
3. Try again")))
                    (println "Token:" token)
                    token)))
(defn authorization [] {:token @token})
(defn slack-url [endpoint]
  (str "https://slack.com/api/" endpoint))

(s/defn ok? :- s/Bool [response :- t/SlackResponse]
  (:ok response))

(s/defn add-orig-msg-to-errors :- t/SlackResponse [response :- t/SlackResponse
                              url
                                                   params]
  (cond (ok? response)
        response
        :else
        (merge {:requested-url url :request-params params} response)))

(s/defn call-slack :- t/SlackResponse
  ([endpoint :- s/Str]
     (call-slack endpoint {}))
  ( [endpoint :- s/Str
     parameters :- {s/Keyword (s/either s/Str s/Num)}]
      (let [url (slack-url endpoint)
            params (merge parameters (authorization))
            response (client/get url {:query-params params
                                      :as :json})]
        (println (:body response))
        (add-orig-msg-to-errors (:body response) url params))))



(s/defn with-ok-checking [f :- (s/=> t/SlackResponse)]
  (when-not (fn? f)
    (throw (Exception. "Not a function")))
  (let [response (f)]
    (if (ok? response)
      response
      (throw (Exception. (str "Failure from slack API " response))))))

(s/defn list-channels :- [t/ChannelInfo] []
  (:channels (with-ok-checking (fn []
                                 (call-slack "channels.list" {:exclude_archived 1})))))

(s/defn channel-exists? :- s/Bool [name :- s/Str]
  (let [existing-channels (map :name (list-channels))]
    (contains? (set existing-channels) name)))

(s/defn create-channel :- t/ChannelInfo [name :- s/Str]
  (:channel (with-ok-checking #(call-slack "channels.create" {:name name}))))

(s/defn archive-channel [info :- t/ChannelInfo]
  (let [response (call-slack "channels.archive" {:channel (:id info)})]
    (when-not (or (ok? response) (= "already_archived" (:error response)))
      (throw (Exception. (str "Unable to archive channel " info " -- " response))))))

(s/defn list-users :- {s/Str t/UserInfo} []
  (let [members (:members (with-ok-checking #(call-slack "users.list")))]
    (into {} (for [m members]
               [(:name m) m]))))

;; ChannelName, ChannelId schemas
(s/defn post [channel text]
  (call-slack "chat.postMessage" {:channel channel :text text}))

;; lazy-seq schema please
(defn read-messages [channel-id]
  (map :text (filter (comp (partial = "message") :type) ( :messages
                                                          (with-ok-checking #(call-slack "channels.history" { :channel channel-id }))))))
