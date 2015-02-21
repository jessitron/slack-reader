(ns slack-reader.play
  (:require [slack-reader.core :refer :all]))

(def beginning-emoji (partial re-find #"^:[a-z0-9-_]*:"))
(defn emoji-to-keyword [e] (keyword (.replace e ":" "")))

(def coe-channel-id (channel-id-from-name "core-offer-engine"))

(def our-messages (read-messages coe-channel-id))

(def our-beginning-emoji (->> our-messages
                              (map beginning-emoji)
                              (filter (complement nil?))
                              (map emoji-to-keyword)))

(def emoji-classification
  {:hurt :sad
   :weight :sad
   :lift :happy
   :eyes :neutral
   :action :neutral
   :simple_smile :happy
   :neutral_face :neutral
   :beer :happy
   :disappointed :sad
   :observation :neutral
   :heavy :sad
   :sadtrombone :sad
   :thumbsup :happy
   :smile :happy
   :tada :happy
   :rocket :happy
   :balloon :happy})

(def emoji-freq (frequencies our-beginning-emoji))

(def classified-freqs
  (apply merge-with merge
         (for [entry emoji-freq]
           (let [cat (get emoji-classification (first entry) :other)]
             {cat (apply hash-map entry)}))))

(def classified-totals ( into {}
                              (for [[cat emoji-counts] classified-freqs]
                                [cat (reduce + (vals emoji-counts))])))

(defn keyword-to-emoji [kw] (str ":" (name kw) ":"))

(defn cat-emoji [cat-of-interest] (for [[kw cat] emoji-classification
                         :when (= cat-of-interest cat)]
                     (keyword-to-emoji kw)))

(defn starts-with-fn [prefix] (fn [s] (.startsWith s prefix)))

(defn cat-message? [cat msg] (some (fn [f] (f msg)) (map starts-with-fn (cat-emoji cat)) ))

(def happy-messages (filter (partial cat-message? :happy) our-messages))

(defn remove-beginning-emoji [msg]
  )
