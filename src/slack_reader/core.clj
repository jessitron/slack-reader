(ns slack-reader.core
  (:require [schema.core :as s]
           [slack-reader.schema :as t]))

(s/defn favorite-color :- s/Keyword
  "What is my favorite color?"
  []
  (first (shuffle [:orange :yellow :purple :green])))

(s/defn call-slack :- t/SlackResponse [endpoint :- s/Str]
  {:ok true})

(s/defn ok :- s/Bool [response :- t/SlackResponse]
  (:ok response))
