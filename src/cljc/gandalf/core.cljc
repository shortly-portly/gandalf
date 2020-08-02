(ns gandalf.core)

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

(defmethod create-route :index [{:keys [resource]}]
  (let [resource-name (name resource)]
    [(str "/" resource-name) {:name (keyword resource-name "index")
                              :controllers [{:start (fn [_] (prn resource " :index called"))}]}]))

(defmethod create-route :new [{:keys [resource]}]
  (let [resource-name (name resource)]
    [(str "/" resource-name "/new") {:name (keyword resource-name "new")
                                     :conflicting true
                                     :controllers [{:start (fn [_] (prn resource " :new called"))}]}]))


(defmethod create-route :show [{:keys [resource]}]
  (let [resource-name (name resource)]
    [(str "/" resource-name "/:id") {:name (keyword resource-name "show")
                                     :conflicting true
                                     :controllers [{:start (fn [_] (prn resource " :show called"))}]}]))


(defmethod create-route :edit [{:keys [resource]}]
  (let [resource-name (name resource)]
    [(str "/" resource-name "/:id/edit") {:name (keyword resource-name "edit")
                                          :controllers [{:start (fn [_] (prn resource " :edit called"))}]}]))

(defn create-routes
  "Given a map containing a `resource` and a vector of `actions` create a set of Reitit route defintiions

  Valid actions are

  - :index
  - :new
  - :show
  - :edit

  Example:

      (create-routes {:resource :wibble :actions [:index :new]})
      ;; => [[\"/wibble\" {:name :wibble/index}][\"/wibble/new\" {:name :wibble/new}]"

  [{:keys [resource actions]
    :or {actions [:index :new :show :edit]}}]

  (vec (for [action actions]
     (create-route {:type action :resource resource}))))

(defn status []
  "compiled in clj/gandalf/core.cljc")
