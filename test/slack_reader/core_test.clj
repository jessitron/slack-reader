(ns slack-reader.core-test
  (:require [clojure.test :refer :all]
            [slack-reader.core :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)


(defspec i-like-orange 100
  (prop/for-all [n gen/int]
                (is (= :orange (favorite-color)))))

(deftest can-hit-it
  (testing "connectivity"
    (is (ok (call-slack "api.test")))))
