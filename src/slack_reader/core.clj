(ns slack-reader.core
  (:require [schema.core :as s]
           [slack-reader.schema :as t]))

(s/defn call-slack :- t/SlackResponse [endpoint :- s/Str
                                       parameters :- {s/Keyword s/Str}]
  {:ok true})

(s/defn ok :- s/Bool [response :- t/SlackResponse]
  (:ok response))
