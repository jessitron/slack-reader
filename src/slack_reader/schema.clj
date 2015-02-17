(ns slack-reader.schema
  (:require [schema.core :as s]))

(def SlackResponse {:ok s/Bool s/Keyword s/Str})
