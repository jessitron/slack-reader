(ns slack-reader.schema-test
  (:require [slack-reader.schema :refer :all]
            [clojure.test :refer :all]
            [schema.core :as s]))

(deftest channel-name-test
  (testing "the canonical channel"
    (is (nil? (s/check ChannelName "general"))))
  (testing "lowercase and digits are cool"
    (is (nil? (s/check ChannelName "abcd01234567890xyz"))))
  (testing "underscores and dashes are fine"
    (is (nil? (s/check ChannelName "_yo-yo_yo-"))))
  (testing "Polish lowercase, not accepted"
    (is (not (nil? (s/check ChannelName "cześć")))))
  (testing "empty is bad"
    (is (not (nil? (s/check ChannelName "")))))
  (testing "21 chars is OK"
    (is (nil? (s/check ChannelName (apply str (repeat max-channel-name-len "x" ))))))
  (testing "22 chars is not OK"
    (is (not (nil? (s/check ChannelName (apply str (repeat (inc max-channel-name-len) "x")))))))
  (testing "Combination of underscores and dashes, OK"
    (is (nil? (s/check ChannelName "_-"))))
  (testing "all underscores is not OK"
    (is (not (nil? (s/check ChannelName "____")))))
  (testing "all dashes is not OK"
    (is (not (nil? (s/check ChannelName "----"))))))
