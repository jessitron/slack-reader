(ns slack-reader.core-test
  (:require [clojure.test :refer :all]
            [slack-reader.core :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.string :as str]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(def channel-gen (gen/no-shrink (gen/such-that #(not (str/blank? %)) gen/string)))

(defspec test-api 10
  (prop/for-all [parameters (gen/map gen/keyword gen/string-alphanumeric)]
                (let [response (call-slack "api.test" parameters)
                      parameter-echo (-> (:args response)
                                         (dissoc :token))]
                  (and (is (ok response))
                       (is (= parameters parameter-echo)))
                  )))

(deftest whoami
  (testing "can retrieve all users"
    (let [users-by-name (list-users)]
      (is (not (nil? (get users-by-name "jess")))))))

;; ha ha, need this for cleanup
(def my-user {:id "U029S7MJ5"})
(defn archive-my-lonely-channels [user]
  (let [
        me (:id user)
        lonely-channels (->> (list-channels)
                             (filter #(= me (:creator %)))
                             (filter #(= 1 (:num_members %))))]
    (doseq [c lonely-channels]
      (println "archiving" (:name c))
      (archive-channel c))))

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
    (is (not (channel-exists? "no-more-clojure"))))
  (testing "archived channels don't exist"
    (is (not (channel-exists? "guest-support-rocks")))))

;; TODO: work from archived channels instead of creating a million new ones
;; should be fair to un-archive, use it, re-archive
(defspec create-and-delete-channel 2
  (prop/for-all [name (gen/such-that #(not (channel-exists? %)) channel-gen)]
                (let [channel-info (create-channel name)]
                  (and (is (channel-exists? name))
                       (do
                         (archive-channel channel-info)
                         (is (not (channel-exists? name))))))))

(deftest can-hit-it
  (testing "connectivity"
    (is (ok (call-slack "api.test")))))
