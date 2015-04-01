(ns dbutil.core
  (:require [datomic.api :as d]
            [taoensso.timbre :as timbre]
            [environ.core :refer [env]]))

(timbre/refer-timbre)

(def ^:dynamic *db-uri* (env :datomic-uri))

(defn conn [] (d/connect *db-uri*))

(defn db [] (d/db (conn)))

;; route helper
(defn wrap-db
  "A Ring middleware that provides a request-consistent database connection and
  value for the life of a request."
  [handler]
  (fn [request]
    (let [conn (d/connect *db-uri*)]
      (handler (assoc request
                 :conn conn
                 :db (d/db conn))))))

;; creation
(defn load-dtm [conn filename]
  (info "Loading schema file: " filename)
  ;; parse and submit schema transaction
  @(d/transact conn (read-string (slurp (clojure.java.io/resource filename)))))

(defn create! []
  ;; create database
  (info "Creating database" *db-uri*)
  (d/create-database *db-uri*)
  (let [conn (conn)] (load-dtm conn "db/schema.dtm") conn))
