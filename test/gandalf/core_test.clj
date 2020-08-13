(ns gandalf.core-test
  (:require [clojure.test :refer :all]
            [gandalf.core :refer :all]
            [reitit.core :as r]))

(deftest create-resource
  (testing "creating a resource"
    (let [routes (create-routes {:resource :user})
          router (r/router routes)]

      (let [route (r/match-by-path router "/users")]
        (is (= (:path route) "/users"))
        (is (= (get-in route [:data :get :summary]) "Returns a list of users"))
        (is (= (get-in route [:data :post :summary]) "creates a new user, returning the id of the newly created user")))

      (let [route (r/match-by-path router "/users/new")]
        (is (= (:path route) "/users/new"))
        (is (= (get-in route [:data :get :summary]) "Returns a create user form")))

      (let [route (r/match-by-path router "/users/1/edit")]
        (is (= (:path route) "/users/1/edit"))
        (is (= (get-in route [:data :get :summary]) "Returns an edit user form")))

      (let [route (r/match-by-path router "/users/1")]
        (is (= (:path route) "/users/1"))

        (is (= (get-in route [:data :get :summary]) "Returns a user with the given id"))
        (is (= (get-in route [:data :post :summary]) "Updates a user with the given id"))
        (is (= (get-in route [:data :delete :summary]) "Deletes a user with the given id"))))))


(deftest create-resource-with-plural
  (testing "creating a resource with a plural name"
    (let [routes (create-routes {:resource :child :plural :children})
          router (r/router routes)]

      (let [route (r/match-by-path router "/children")]
        (is (= (:path route) "/children"))
        (is (= (get-in route [:data :get :summary]) "Returns a list of children"))
        (is (= (get-in route [:data :post :summary]) "creates a new child, returning the id of the newly created child")))


      (let [route (r/match-by-path router "/children/new")]
        (is (= (:path route) "/children/new"))
        (is (= (get-in route [:data :get :summary]) "Returns a create child form")))

      (let [route (r/match-by-path router "/children/1/edit")]
        (is (= (:path route) "/children/1/edit"))
        (is (= (get-in route [:data :get :summary]) "Returns an edit child form")))

      (let [route (r/match-by-path router "/children/1")]
        (is (= (:path route) "/children/1"))
        (is (= (get-in route [:data :get :summary]) "Returns a child with the given id"))
        (is (= (get-in route [:data :post :summary]) "Updates a child with the given id"))
        (is (= (get-in route [:data :delete :summary]) "Deletes a child with the given id"))))))

(deftest create-specific-actions
  (testing "creating a set of specifc routes for a resource"
    (let [routes (create-routes {:resource :user :actions [:new :index]})
          router (r/router routes)]

      (let [route (r/match-by-path router "/users")]
        (is (= (:path route) "/users"))
        (is (= (get-in route [:data :get :summary]) "Returns a list of users"))
        (is (= (get-in route [:data :post :summary]) nil)))

      (let [route (r/match-by-path router "/users/new")]
        (is (= (:path route) "/users/new"))
        (is (= (get-in route [:data :get :summary]) "Returns a create user form")))

      (let [route (r/match-by-path router "/users/1")]
        (is (= (:path route) nil))))))

(deftest specific-routes
  (testing "create and update routes"
    (let [routes (create-routes {:resource :user :actions [:create :update]})
          router (r/router routes)]

      (let [route (r/match-by-path router "/users")]
        (is (= (:path route) "/users"))
        (is (= (get-in route [:data :get :summary]) nil))
        (is (= (get-in route [:data :post :summary]) "creates a new user, returning the id of the newly created user")))

      (let [route (r/match-by-path router "/users/1")]
        (is (= (:path route) "/users/1"))
        (is (= (get-in route [:data :get :summary]) nil))
        (is (= (get-in route [:data :post :summary]) "Updates a user with the given id"))
        (is (= (get-in route [:data :delete :summary]) nil)))))

  (testing "create and update routes"
    (let [routes (create-routes {:resource :user :actions [:show :edit :delete]})
          router (r/router routes)]

      (let [route (r/match-by-path router "/users/1")]
        (is (= (:path route) "/users/1"))
        (is (= (get-in route [:data :get :summary]) "Returns a user with the given id"))
        (is (= (get-in route [:data :post :summary]) nil))
        (is (= (get-in route [:data :delete :summary]) "Deletes a user with the given id"))))))
