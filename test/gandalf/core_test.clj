(ns gandalf.core-test
  (:require [clojure.test :refer :all]
            [gandalf.core :refer :all]))


(deftest create-route-map-test
  (testing "creating a route map"
    (let [route-map (create-route-map  [:index :new :create] :user {})]
      (is (some? (:index route-map)))
      (is (some? (:new route-map)))
      (is (some? (:create route-map)))
      (is (nil? (:update route-map))))))

(deftest create-all-route-map-test
  (testing "creating a route map for all routes"
    (let [route-map (create-route-map  [:index :new :create :show :edit :update :delete] :user {})
          index-route (:index route-map)
          new-route (:new route-map)
          create-route (:create route-map)
          show-route (:show route-map)
          edit-route (:edit route-map)
          update-route (:update route-map)
          delete-route (:delete route-map)]

      (is (some? (:get index-route)))
      (is (= (get-in index-route [:get :summary]) "Returns a list of users"))

      (is (some? (:get new-route)))
      (is (= (get-in new-route [:get :summary]) "Returns a create user form"))


      (is (some? (:post create-route)))
      (is (= (get-in create-route [:post :summary]) "creates a new user, returning the id of the newly created user"))

      (is (some? (:get show-route)))
      (is (= (get-in show-route [:get :summary]) "Returns a user with the given id"))

      (is (some? (:get edit-route)))
      (is (= (get-in edit-route [:get :summary]) "Returns an edit user form"))

      (is (some? (:post update-route)))
      (is (= (get-in update-route [:post :summary]) "Updates a user with the given id"))

      (is (some? (:delete delete-route)))
      (is (= (get-in delete-route [:delete :summary]) "Deletes a user with the given id")))))

;; {:index
;;  {:get
;;   {:summary "Returns a list of schools",
;;    :handler #function[gandalf.core/eval162/fn--164/fn--166]}},
;;  :new
;;  {:conflicting true,
;;   :get
;;   {:summary "Returns a create school form",
;;    :handler #function[gandalf.core/eval178/fn--180/fn--182]}},
;;  :create
;;  {:post
;;   {:summary
;;    "creates a new school, returning the id of the newly created school",
;;    :handler #function[gandalf.core/eval170/fn--172/fn--174]}},
;;  :show
;;  {:conflicting true,
;;   :get
;;   {:summary "Returns a school with the given id",
;;    :handler #function[gandalf.core/eval186/fn--188/fn--190]}},
;;  :edit
;;  {:conflicting true,
;;   :get
;;   {:summary "Returns an edit school form",
;;    :handler #function[gandalf.core/eval194/fn--196/fn--198]}},
;;  :update
;;  {:conflicting true,
;;   :post
;;   {:summary "Updates a school with the given id",
;;    :handler #function[gandalf.core/eval202/fn--204/fn--206]}},
;;  :delete
;;  {:conflicting true,
;;   :delete
;;   {:summary "Deletes a school with the given id",
;;    :handler #function[gandalf.core/eval210/fn--212/fn--214]}}}
