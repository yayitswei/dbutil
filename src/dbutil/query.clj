(ns dbutil.query
  (:require [datomic.api :as d]))

(defn only
  "Return the only item from a query result"
  [query-result]
  (assert (= 1 (count query-result)))
  (assert (= 1 (count (first query-result))))
  (ffirst query-result))

(defn entity
  "Returns the entity if passed an id that is not false-y."
  [db id]
  (when id
    (d/entity db id)))

(defprotocol Eid
  (e [_]))

(extend-protocol Eid
  java.lang.Long
  (e [n] n)

  datomic.db.DbId
  (e [dbid] dbid)

  datomic.Entity
  (e [ent] (:db/id ent)))

(defn qe
  "Returns the single entity returned by a query."
  [query db & args]
  (let [res (apply d/q query db args)]
    (d/entity db (only res))))

(defn qe-or-nil
  "Returns the single entity returned by a query, or nil"
  [query db & args]
  (let [res (apply d/q query db args)]
    (entity db (ffirst res))))

(defn find-by
  "Returns the unique entity identified by attr and val."
  [db attr val]
  (qe-or-nil '[:find ?e
               :in $ ?attr ?val
               :where [?e ?attr ?val]]
             db attr val))

(defn qes
  "Returns the entities returned by a query, assuming that
   all :find results are entity ids."
  [query db & args]
  (->> (apply d/q query db args)
       (mapv (fn [items]
               (mapv (partial d/entity db) items)))))

(defn find-all-by
  "Returns all entities possessing attr."
  [db attr]
  (qes '[:find ?e
         :in $ ?attr
         :where [?e ?attr]]
       db attr))

(defn qfs
  "Returns the first of each query result."
  [query db & args]
  (->> (apply d/q query db args)
       (mapv first)))

(defn qfes
  "Returns the first entity of each query result."
  [query db & args]
  (->> (apply d/q query db args)
       (mapv (fn [items] (d/entity db (first items))))))

(comment
  (defn maybe
    "Returns the value of attr for e, or if-not if e does not possess
   any values for attr. Cardinality-many attributes will be
   returned as a set"
    [db e attr if-not]
    (let [result (d/q '[:find ?v
                      :in $ ?e ?a
                      :where [?e ?a ?v]]
                    db e attr)]
      (if (seq result)
        (case (schema/cardinality db attr)
          :db.cardinality/one (ffirst result)
          :db.cardinality/many (into #{} (map first result)))
        if-not))))

(defn qol
  "Query-ordered-list. Expects a query of pattern [?eid ?ts],
   returns a list of enities sorted by timestamp."
  [query db & args]
  (->> (apply d/q query db args)
       (sort-by second #(compare %2 %1))
       (map #(update-in % [0] (partial d/entity db)))))
