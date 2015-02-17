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

(deftest list-channels-test
  (let [response (call-slack "channels.list")]
    (is (ok response))
    (let [channel-ids (set (map :name (:channels response)))]
      (is (channel-ids "cuteness"))
      (is (channel-ids "pravda")))))

(deftest channel-existence
  (testing "channel that must exist"
    (is (channel-exists? "cuteness")))
  (testing "channel that does not exist"
    (is (not (channel-exists? "no-more-clojure")))))

(deftest can-hit-it
  (testing "connectivity"
    (is (ok (call-slack "api.test")))))
