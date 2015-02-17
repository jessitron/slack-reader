(ns slack-reader.core-test
  (:require [clojure.test :refer :all]
            [slack-reader.core :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)


(defspec test-api 10
  (prop/for-all [parameters (gen/map gen/keyword gen/string-alphanumeric)]
                (let [response (call-slack "api.test" parameters)
                      parameter-echo (-> (:args response)
                                         (dissoc :token))]
                  (and (is (ok response))
                       (is (= parameters parameter-echo)))
                  )))

(deftest can-hit-it
  (testing "connectivity"
    (is (ok (call-slack "api.test")))))
