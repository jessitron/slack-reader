(ns slack-reader.schema
  (:require [schema.core :as s]))

(def ChannelInfo {
                  :id s/Str
                  :name s/Str
                  s/Keyword s/Any})

(def UserInfo {:id s/Str
               :name s/Str
               s/Keyword s/Any})

(def HappySlackResponse {:ok (s/eq true)
                         (s/optional-key :args) {s/Keyword s/Str}
                         (s/optional-key :channels) [ChannelInfo]})

(def AngrySlackResponse {:ok (s/eq false)
                         :error s/Str
                         (s/optional-key :req_method) s/Str
                         (s/optional-key :requested-url) s/Str
                         (s/optional-key :request-params) {s/Keyword s/Any}})

(def SlackResponse (s/either HappySlackResponse AngrySlackResponse))
