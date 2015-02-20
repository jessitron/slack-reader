(ns slack-reader.core-test
  (:require [clojure.test :refer :all]
            [slack-reader.core :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.string :as str]
            [schema.core :as s]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(defn- to-and-back [to from f]
                          (fn [& in]
                            (from (apply f (map to in)))))
(def char-range (to-and-back int (partial map char) range))
;; lowercase letters, underscore, dash

(def channel-name-allowed-chars (set (concat (char-range \a \z) (char-range \0 \9) [\_ \-])))
(def ChannelNameChar (s/pred channel-name-allowed-chars "lowercase, digit, dash, underscore"))

(def channel-char-gen (gen/elements channel-name-allowed-chars))
(def disallowed? (partial contains? #{"_" "-"}))

;; Can't be all underscores, or all dashes. Can be a combo of the two
;; (by observation)
(s/defn disallowed? [channel-chars :- [ChannelNameChar]]
  (or (every? (partial = \_) channel-chars)
      (every? (partial = \-) channel-chars)))

(def channel-name-gen (gen/no-shrink
                  (gen/such-that (complement disallowed?)
                                 (gen/fmap (partial apply str)
                                           (gen/vector channel-char-gen 1 21)))))

(defspec test-api 10
  (prop/for-all [parameters (gen/map gen/keyword gen/string-alphanumeric)]
                (let [response (call-slack "api.test" parameters)
                      parameter-echo (-> (:args response)
                                         (dissoc :token))]
                  (and (is (ok response))
                       (is (= parameters parameter-echo)))
                  )))

;; this could be in config with token
(def my-username "jess")
(deftest whoami
  (testing "can retrieve all users"
    (let [users-by-name (list-users)]
      (is (not (nil? (get users-by-name my-username)))))))

;; ha ha, need this for cleanup
(defn my-user [] (:id (get (list-users) my-username )))
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
      (is (channel-ids "general"))
      (is (channel-ids "random")))))

(deftest channel-existence
  (testing "channel that must exist"
    (is (channel-exists? "general")))
  (testing "channel that does not exist"
    (is (not (channel-exists? "do-not-create-this-channel"))))
  (testing "archived channels don't exist"
    (is (not (channel-exists? "this-channel-is-archived")))))

;; TODO: work from archived channels instead of creating a million new ones
;; should be fair to un-archive, use it, re-archive
(defspec create-and-delete-channel 2
  (prop/for-all [name (gen/such-that #(not (channel-exists? %)) channel-name-gen)]
                (println (str "creating channel<" name) ">")
                (let [channel-info (create-channel name)]
                  (and (is (channel-exists? name))
                       (do
                         (archive-channel channel-info)
                         (is (not (channel-exists? name))))))))

(deftest can-hit-it
  (testing "connectivity"
    (is (ok (call-slack "api.test")))))

(defn sample-one [g]  (last (gen/sample g)))
;; this is not going to work when it generates an archived one
(defn new-channel []
  (create-channel (sample-one channel-name-gen)))

(defn with-channel [messages f]
  (let [channel-info (new-channel)]
    (doall (map (partial post (:id channel-info)) messages))
    (f channel-info)
    (archive-channel channel-info)))

(deftest read-from-channel
  (let [message "Spank Me"]
    (with-channel [message]
      (fn [channel-info]
        (let [results (read-messages (:id channel-info))]
           (is (some (partial = message) results)))))))
