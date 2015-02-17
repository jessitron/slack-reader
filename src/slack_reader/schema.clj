(ns slack-reader.schema
  (:require [schema.core :as s]))

(def SlackResponse {:ok s/Bool
                    :args {s/Keyword s/Str}
                    s/Keyword s/Str})
