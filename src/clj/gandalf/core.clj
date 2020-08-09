(ns gandalf.core
 (:require [reitit.core :as r]) )

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
  "Given a http `action`, returns the corresponding Reitit route definition.

  | action | route |
  |--------|-------|
  | :index | [\"/foo\" {:name :foo/index}]
  | :new   | [\"/foo/new\" {:name :foo/new}]
  | :show  | [\"/foo/:id\" {:name :foo/show}
  | :edit  | [\"/foo/:id/edit\" {:name :foo/edit}

      (route {:type :show :resource :user})
      ;; => [\"/user/:id\" {:name :user/show}]"

  :type)

(defmethod create-route :index [{:keys [resource attrs]}]
  {:index {:get {:summary (str "Returns a list of " (pluralise resource (:plural attrs)))
                 :name (keyword (name resource) "index")
                 :handler (fn [_]
                            {:status 200
                             :body "ok"})}}})

(defmethod create-route :create [{:keys [resource attrs]}]
  {:create {:post {:summary (str "creates a new " (name resource) ", returning the id of the newly created " (name resource))
                   :handler (fn [_]
                              {:status 200
                               :body "ok"})}}})

(defmethod create-route :new [{:keys [resource attrs]}]
  {:new {:conflicting true
         :get {:summary (str "Returns a create " (name resource) " form")
               :handler (fn [_]
                          {:status 200
                           :body "ok"})}}})

(defmethod create-route :show [{:keys [resource attrs]}]
  {:show {:conflicting true
          :get {:summary (str "Returns a " (name resource) " with the given id")
                :handler (fn [_]
                           {:status 200
                            :body "ok"})}}})

(defmethod create-route :edit [{:keys [resource attrs]}]
  {:edit {:conflicting true
          :get {:summary (str "Returns an edit " (name resource) " form")
                :handler (fn [_]
                           {:status 200
                            :body "ok"})}}})

(defmethod create-route :update [{:keys [resource attrs]}]
  {:update {:conflicting true
            :post {:summary (str "Updates a " (name resource) " with the given id")
                   :handler (fn [_]
                              {:status 200
                               :body "ok"})}}})

(defmethod create-route :delete [{:keys [resource attrs]}]
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
  [actions resource attrs]
  (into {} (for [action actions]
             (create-route {:type action :resource resource :attrs (dissoc attrs :type :resource)}))))

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
  "Given a map containing a `resource` and a vector of `actions` create a set of Reitit route defintiions

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
    :as attrs}]

  (let [resource-plural (pluralise resource (:plural attrs))
        route-map (create-route-map actions resource attrs)]

    (vec (remove nil?
                 [(str "/" resource-plural)
                  (index-create-routes route-map)
                  (new-route route-map)
                  (edit-route route-map)
                  (show-update-delete-routes route-map)]))))
