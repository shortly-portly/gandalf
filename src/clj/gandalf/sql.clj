(ns gandalf.sql
  (:require [clojure.java.jdbc :as j]
            [honeysql.core :as sql]
            [honeysql.helpers :as helpers]))

;; ds holds the reference, datasource, to the database used
;; by the system.
(def ds (atom nil))

(defn set-datasource
  "Set the datasource to use for all database operations.

  The datasource will be defined outside of this library and therefore this
  function needs to be called externally to enable database access."
  [datasource]

  (reset! ds {:datasource datasource}))

(defn default-index-query
  "Returns the default query for the :index action if one isn't provided for the resource."
  [resource-map]

  (let [resource-name (get resource-map :resource)
        table-name (get resource-map :table resource-name)]
    {:select [:*]
     :from [table-name]}
  ))

(defn default-show-query
  "Returns the default query for the :show action if one isn't provided for the resource."
  [resource-map]

  (let [resource-name (get resource-map :resource)
        table-name (get resource-map :table resource-name)]
    {:select [:*]
     :from [table-name]
     :where [:= :id :?id]}))

(defn fetch-results
  [query params]
  (j/query @ds (sql/format query :params params)))

;; ------------------------------------------------------------------------
(comment
(def conn {:datasource db/*db*})

(j/query conn ["select * from users"])
)
