(ns slack-reader.schema
  (:require [schema.core :as s]))

(def ChannelInfo {
                  :id s/Str
                  :name s/Str
                  s/Keyword s/Any})

(def SlackResponse {:ok s/Bool
                    (s/optional-key :args) {s/Keyword s/Str}
                    (s/optional-key :channels) [ChannelInfo]
                    s/Keyword (s/either s/Str [s/Any])})
