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
(def UserInfo {:id s/Str
               :name s/Str
               s/Keyword s/Any})

;;
;; Full responses
;;
(def HappySlackResponse {:ok (s/eq true)
                         (s/optional-key :args) {s/Keyword s/Str}
                         (s/optional-key :channels) [ChannelInfo]})

(def AngrySlackResponse {:ok (s/eq false)
                         :error s/Str
                         (s/optional-key :req_method) s/Str
                         (s/optional-key :requested-url) s/Str
                         (s/optional-key :request-params) {s/Keyword s/Any}})

(def SlackResponse (s/either HappySlackResponse AngrySlackResponse))
