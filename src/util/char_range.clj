(ns util.char-range)

(defn- to-and-back [to from f]
                          (fn [& in]
                            (from (apply f (map to in)))))
(defn- inclusive-range [a b] (range a (inc b)))
(def char-range (to-and-back int (partial map char) inclusive-range))

