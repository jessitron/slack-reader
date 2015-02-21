(ns slack-reader.gen
  (:require [clojure.test.check.generators :as gen]
            [slack-reader.schema :refer :all]))


(def channel-char-gen (gen/elements channel-name-allowed-chars))
(def disallowed? (partial contains? #{"_" "-"}))

;; Can't be all underscores, or all dashes. Can be a combo of the two
;; (by observation)
(defn disallowed? [channel-name]
  (or all-underscores? all-dashes?))

(def channel-name-gen (gen/no-shrink
                  (gen/such-that (complement disallowed?)
                                 (gen/fmap (partial apply str)
                                           (gen/vector channel-char-gen 1 21)))))
