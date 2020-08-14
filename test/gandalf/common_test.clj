(ns gandalf.common-test
  (:require [clojure.test :refer :all]
            [gandalf.common :refer :all]))

(deftest index-route
  (testing "index route"
    (let [[path params] (create-route {:type :index :resource :user})]
      (is (= path "/users"))
      (is (= (:name params) :user/index)))))

(deftest new-route
  (testing "new route"
    (let [[path params] (create-route {:type :new :resource :user})]
      (is (= path "/user/new"))
      (is (= (:name params) :user/new)))))

(deftest show-route
  (testing "show route"
    (let [[path params] (create-route {:type :show :resource :user})]
      (is (= path "/user/:id"))
      (is (= (:name params) :user/show)))))

(deftest edit-route
  (testing "edit route"
    (let [[path params] (create-route {:type :edit :resource :user})]
      (is (= path "/user/:id/edit"))
      (is (= (:name params) :user/edit)))))

(deftest create-index-new-routes
  (testing "create :index and :new routes"
    (let [[index-route new-route] (create-routes {:resource :user :actions [:index :new]})]

      (let [[path params] index-route]
        (is (= path "/users"))
        (is (= (:name params) :user/index)))

      (let [[path params] new-route]
        (is (= path "/user/new"))
        (is (= (:name params) :user/new))))))

(deftest create-default-routes
  (testing "create default routes"
    (let [[index-route new-route show-route edit-route] (create-routes {:resource :user})]

      (let [[path params] index-route]
        (is (= path "/users"))
        (is (= (:name params) :user/index)))

      (let [[path params] new-route]
        (is (= path "/user/new"))
        (is (= (:name params) :user/new)))


    (let [[path params] show-route]
      (is (= path "/user/:id"))
      (is (= (:name params) :user/show)))


    (let [[path params] edit-route]
      (is (= path "/user/:id/edit"))
      (is (= (:name params) :user/edit))))))

(deftest create-resource-with-plural
  (testing "create a resource with a plural name provided"
    (let [[path params] (create-route {:type :index :resource :child :plural :children})]
      (is (= path "/children"))
      (is (= (:name params) :child/index)))))
