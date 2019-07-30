(ns spec-migration-fun.core
  (:require [spec-tools.data-spec :as ds]
            [clj-time.core :as t]
            [clojure.spec.alpha :as s])
  (:import (java.util UUID)))

(def loan
  {::id uuid?
   ::amount integer?
   ::value-date string?
   ::recovery (ds/maybe integer?)})

(def loan-meta
  {::amount {:default 0
             :added (t/date-time 2019 03 03)}})

(def old-event
  {:id (UUID/randomUUID)
   :value-date "2019-07-30"
   :created-at (t/date-time 2019 01 01)})

(def new-event
  {:id (UUID/randomUUID)
   :value-date "2019-07-30"
   :created-at (t/date-time 2019 04 04)
   :amount 1000})

(defn update-loan-spec
  [instant]
  (for [k (keys loan)]
    (if (contains? loan-meta k)
      (when (t/after? instant (-> loan-meta k :added))
        {k (k loan)})
      {k (k loan)})))

(defn validate-loan
  [event]
  (let [new-map (update-loan-spec
                 (:created-at event))
        generated-spec (ds/spec
                        (update-loan-spec
                         (:created-at event)))]
    (println " New Map = Spec = " new-map generated-spec)
    (s/valid? generated-spec event)))

(validate-loan old-event)
;; => true

(validate-loan new-event)
;; => true
