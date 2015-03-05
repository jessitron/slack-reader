(ns slack-reader.schema
  (:require [util.char-range :refer [char-range]]
            [schema.core :as s]
            [var-schema.core :refer [fmap]]))

;;
;; Channels
;;
;; lowercase letters, underscore, dash

(def max-channel-name-len 21)
(def min-channel-name-len 1)
(def channel-name-allowed-chars (set (concat (char-range \a \z) (char-range \0 \9) [\_ \-])))
(defn all-underscores? [s] (= #{\_} (set s)))
(defn all-dashes? [s] (= #{\-} (set s)))
(def ChannelNameChar (s/pred channel-name-allowed-chars "lowercase, digit, dash, underscore"))

(def ChannelName (s/named (s/both s/Str

                                  (s/pred (complement all-underscores?) "All underscores is not OK")
                                  (s/pred (complement all-dashes?) "All dashes is not OK")
                                  (fmap seq [ChannelNameChar])
                                  (s/pred
                                   #(<= min-channel-name-len (count %) max-channel-name-len)
                                   (str "Channel name length: " min-channel-name-len
                                        "-" max-channel-name-len)))  ;; this could be prettier
                          "Channel Name"))

(def ChannelInfo {
                  :id s/Str
                  :name s/Str
                  s/Keyword s/Any})

;;
;; Users
;;
(s/defschema UserId (s/named s/Str "UserId"))
(s/defschema UserName (s/named s/Str "UserName"))
(def UserInfo {:id UserId
               :name UserName
               s/Keyword s/Any})

;;
;; Messages
;;
(s/defschema SlackTimestamp (s/named s/Str "Slack Timestamp"))
(s/defschema Message {(s/optional-key :user) UserId    ;; not populated for bot messages
                      (s/optional-key :username) UserName ;; not populated for channel join
                      :type (s/eq "message")
                      :subtype s/Str
                      :ts SlackTimestamp
                      :text s/Str})

;;
;; Full responses
;;
(s/defschema HappySlackResponse {:ok (s/eq true)
                                 (s/optional-key :args) {s/Keyword s/Str}
                                 (s/optional-key :channel) ChannelInfo
                         (s/optional-key :channels) [ChannelInfo]
                                 (s/optional-key :messages) [Message]
                                 s/Keyword s/Any})

(s/defschema AngrySlackResponse {:ok (s/eq false)
                         :error s/Str
                         (s/optional-key :req_method) s/Str
                         (s/optional-key :requested-url) s/Str
                         (s/optional-key :request-params) {s/Keyword s/Any}})

(s/defschema SlackResponse s/Any)
