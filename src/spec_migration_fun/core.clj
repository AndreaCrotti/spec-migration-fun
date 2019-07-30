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
             :added #inst "2019-07-30T10:03:39.782Z"}})

(def old-event
  {:id (UUID/randomUUID)
   :value-date "2019-07-30"
   :created-at #inst "2019-07-30T10:03:39.782Z"})

(def new-event
  {:id (UUID/randomUUID)
   :value-date "2019-07-30"
   :created-at #inst "2019-07-30T10:03:39.782Z"
   :amount 1000})

(defn update-loan-spec
  [instant]
  (for [k (keys loan)]
    (if (contains? loan-meta k)
      (when (t/after? (-> loan-meta k :added) instant)
        {k (k loan)})
      {k (get loan k)})))

(update-loan-spec  #inst "2019-07-30T10:03:39.782Z")

(defn validate-loan
  [event]
  (let [generated-spec (update-loan-spec (t/now))]
    (s/valid? generated-spec event)))

(validate-loan old-event)

(validate-loan new-event)
