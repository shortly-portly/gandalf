(ns gandalf.sql
  (:require [clojure.java.jdbc :as j]
            [clojure.pprint]
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

(defn default-edit-query
  "Returns the default query for the :edit action if one isn't provided for the resource."
  [resource-map]

  (let [resource-name (get resource-map :resource)
        table-name (get resource-map :table resource-name)]
    {:select [:*]
     :from [table-name]
     :where [:= :id :?id]}))

(defn default-update-query
  "Returns the default update sql query for the :update action if one isn't provided for the resource.

  Note that we don't provide the list of fields/value to update as this will be provided at runtime."
  [resource-map]

  (let [resource-name (get resource-map :resource)
        table-name (get resource-map :table resource-name)]
    {:update table-name
     :where [:= :id :?id]}))

(defn fetch-results
  [query params]
  (j/query @ds (sql/format query :params params)))

(defn update-query
  [query params]
  (let [id {:id (:id params)}
        params (dissoc params :id)
        query (assoc query :set params)]
    (try
      (j/execute! @ds(sql/format query id))
  (catch org.sqlite.SQLiteException e
    (clojure.pprint/pprint e)))))
  

;; ------------------------------------------------------------------------
;;
;;
(comment
(def conn {:datasource db/*db*})

(j/query conn ["select * from users"])
)
