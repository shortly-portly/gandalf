(ns gandalf.sql
(:require [honeysql.core :as sql]
         [honeysql.helpers :as helpers]))


(def sqlmap {:select [:*]
             :from [:user]
             })


(defn index-query [resource-map]
  (let [resource-name (get resource-map :resource-name)
        table-name (get resource-map :table resource-name)]
    {:select [:*]
     :from [table-name]}
  ))

;; ------------------------------------------------------------------------
(comment
(def conn {:datasource db/*db*})

(j/query conn ["select * from users"])
)
