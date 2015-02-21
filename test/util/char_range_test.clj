(ns util.char-range-test
  (:require [clojure.test :refer :all]
            [util.char-range :refer :all]))

(deftest char-range-test
  (testing "simple range"
    (is (= [\a \b \c] (char-range \a \c)))))
