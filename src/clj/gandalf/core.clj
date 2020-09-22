(ns gandalf.core
  (:require [malli.core :as m]
            [malli.edn :as edn]
            [malli.util :as mu]
            [reitit.core :as r]
            [reitit.coercion.spec]
            [reitit.ring.middleware.exception :as exception]
            [gandalf.rop :as rop]
           [gandalf.sql :as sql]
            [clojure.pprint]))

(defn pluralise
  "Return the string plural of the given keyword `resource` name.

  An optional second keyword argument, `plural` can be provided which is used as the pluralised
  version of the resource name. If not provided we simply return the string version of the
  resource with an s on the end.

  Example

      (pluralise :child :children)
      ;; => \"children\"

      (pluralise :school)
      ;; => \"schools\""

  [resource plural]
  (if plural
    (str (name plural))
    (str (name resource) "s")))

(defmulti create-route
  "Given a http `action`, returns a map that will be subsequently be combined
  with a URL to form a RESTful route for this action.

  The second argument to this function is a map with the following keys currently defined:

  | Key      | Meaning |
  |----------|---------|
  | action   | One of :index, :create, :new, :show, :edit, :update, :delete
  | resource | The resource name for this action
  | plural   | An optional plural version of the resource to improve documentation of the action

      (create-route {:type :index :resource :user})
      ;; => {:index
      {:get
        {:summary \"Returns a list of users\",
         :handler #function[gandalf.core/eval1391/fn--1393/fn--1395]}}}"

  :type)

(defmethod create-route :index [{:keys [resource] :as resource-map}]
  (let [query (get-in resource-map [:sql :index] (sql/default-index-query resource-map))
        schema (get resource-map :schema [])
        view (get-in resource-map [:view :index])]
    {:index {:get {:summary (str "Returns a list of " (pluralise resource (:plural resource-map)))

                   :handler (fn [_]
                              (let [results (into [] (sql/fetch-results query {}))]
                                {:status 200
                                 :body {:resource resource
                                        :data results
                                        :schema (edn/write-string schema)
                                        :view view}}))}}}))

(defmethod create-route :create [{:keys [resource]}]
  {:create {:post {:summary (str "creates a new " (name resource) ", returning the id of the newly created " (name resource))
                   :handler (fn [foo]
                              (prn foo)
                              {:status 200
                               :body "ok"})}}})

(defmethod create-route :new [{:keys [resource]}]
  {:new {:conflicting true
         :get {:summary (str "Returns a create " (name resource) " form")
               :handler (fn [_]
                          {:status 200
                           :body "ok"})}}})

(defmethod create-route :show [{:keys [resource] :as resource-map}]
  (let [query (get-in resource-map [:sql :show] (sql/default-show-query resource-map))
        schema (get resource-map :schema [])
        view (get-in resource-map [:view :show] [])]
    {:show {:conflicting true
            :get {:summary (str "Returns a " (name resource) " with the given id")
                  :parameters {:path {:id int?}}
                  :handler (fn [{:keys [path-params]}]
                             (let [results (into [] (sql/fetch-results query path-params))]
                               (prn "results for :show :" results)
                               {:status 200
                                :body {:resource resource
                                       :data (first results)
                                       :schema (edn/write-string schema)
                                       :view view}}))}}}))

(defmethod create-route :edit [{:keys [resource] :as resource-map}]
  (let [query (get-in resource-map [:sql :edit] (sql/default-edit-query resource-map))
        schema (get resource-map :schema [])
        view (get-in resource-map [:view :edit] [])]
    {:edit {:conflicting true
            :get {:summary (str "Returns an edit " (name resource) " form")
                  :parameters {:path {:id int?}}
                  :handler (fn [{:keys [path-params]}]
                             (let [results (into [] (sql/fetch-results query path-params))]
                               (prn "results for :edit :" results)
                               (prn "view for :edit :" view)
                               {:status 200
                                :body {:resource resource
                                       :data (first results)
                                       :schema (edn/write-string schema)
                                       :view view}}))}}}))

;; TODO: Need to re-run validation - never trust data coming from the outside world.
;; TODO: Need to check for SQL update errors.
(defmethod create-route :update [{:keys [resource] :as resource-map}]
  (let [query (get-in resource-map [:sql :update] (sql/default-update-query resource-map))]
  {:update {:conflicting true
            :put {:summary (str "Updates a " (name resource) " with the given id")
                   :handler (fn [request]
                              (let [[result error] (rop/=>> (assoc request :sql-query query)
                                                            sql/update-query)]
                                (prn "result...." result)
                                (prn "error....." error)
                              (if error
                                {:status 400
                                 :body {:reason "something bad happened"}}
                                {:status 200
                                 :body {:result result}})))}}}))





(defmethod create-route :delete [{:keys [resource]}]
  {:delete {:conflicting true
            :delete {:summary (str "Deletes a " (name resource) " with the given id")
                     :handler (fn [_]
                                {:status 200
                                 :body "ok"})}}})

(defn create-route-map
  "Given a `resource` and a vector of `actions` generate a map of definitions suitable for building
  a Reitit route map.

  The map produced isn't a valid Reitit route map as it doesn't contain the url elements (just the http action
  definitions)."
  [actions resource-map]
  (into {} (for [action actions]
             (create-route (merge {:type action} resource-map)))))

(defn index-create-routes
  "Create the :index and :create route definitions"
  [route-map]

  (if (or (:index route-map) (:create route-map))

    [""

     (merge
      (if (:index route-map)
        (:index route-map)
        {})

      (if (:create route-map)
        (:create route-map)
        {}))]))

(defn new-route
  "Create the :new route definition"
  [route-map]
  (if (:new route-map)
    ["/new"
     (:new route-map)]))

(defn edit-route
  "Creates the :edit route definition"
  [route-map]
  (if (:edit route-map)
    ["/:id/edit"
     (:edit route-map)]))

(defn show-update-delete-routes
  "Create the :show, :edit, :delete route definitions"
  [route-map]

  (if (or (:show route-map) (:update route-map) (:delete route-map))
    ["/:id"
     (merge

      (if (:show route-map)
        (:show route-map)
        {})

      (if (:update route-map)
        (:update route-map)
        {})

      (if (:delete route-map)
        (:delete route-map)
        {}))]))

(defn create-routes
  "Given a map containing a `resource` and a vector of `actions` create a set of RESTful Reitit route defintiions.

  Valid actions are

  - :index
  - :new
  - :create
  - :show
  - :edit
  - :update
  - :delete"

  [{:keys [resource actions]
    :or {actions [:index :new :create :show :edit :update :delete]}
    :as resource-map}]

  (let [resource-plural (pluralise resource (:plural resource-map))
        route-map (create-route-map actions resource-map)]

    (vec (remove nil?
                 [(str "/" resource-plural)

                  (index-create-routes route-map)
                  (new-route route-map)
                  (edit-route route-map)
                  (show-update-delete-routes route-map)]))))

;; Testing....

(def schema
  [:and
   [:map
    [:x [:and int? [:> 6]]]
    [:y [int?]]]
   [:fn (fn [{:keys [x y]}] (> x y))]])

(def form
  [:fn '(fn [{:keys [x y]}] (> x y))])
