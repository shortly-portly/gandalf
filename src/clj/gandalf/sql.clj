(ns gandalf.sql
(:require [honeysql.core :as sql]
         [honeysql.helpers :refer :all :as helpers]))


(def sqlmap {:select [:*]
             :from [:user]
             })



(comment
(def conn {:datasource db/*db*})

(j/query conn ["select * from users"])
)
