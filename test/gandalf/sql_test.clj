(ns gandalf.sql-test
  (:require [clojure.test :refer :all]
            [gandalf.sql :refer :all]
            ))

(deftest setting-datasource
  (testing "setting the datasource"
    (set-datasource "my-datasource")
    (is (= (@ds "my-datasource")))))
