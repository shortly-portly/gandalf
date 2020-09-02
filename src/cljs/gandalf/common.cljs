(ns gandalf.common
  (:require
   [gandalf.events]
   [gandalf.view :as view]
   [re-frame.core :as rf]))

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
  | :index | [\"/foos\" {:name :foo/index}]
  | :new   | [\"/foo/new\" {:name :foo/new}]
  | :show  | [\"/foo/:id\" {:name :foo/show}
  | :edit  | [\"/foo/:id/edit\" {:name :foo/edit}

  The second argument to this function is a map with the following keys currently defined:

  | Key      | Meaning |
  |----------|---------|
  | action   | One of :index, :create, :new, :show, :edit, :update, :delete
  | resource | The resource name for this action
  | plural   | An optional plural version of the resource to improve documentation of the action

      (route {:type :show :resource :user})
      ;; => [\"/user/:id\" {:name :user/show}]"

  :type)

(defmethod create-route :index [{:keys [resource plural attrs]}]
  (let [resource-name (name resource)
        resource-plural (pluralise resource plural)
        url (str "/" resource-plural)]
    [url (merge {:name (keyword resource-name "index")
                 :view #'view/index-page
                 :controllers [{:start (fn [_] (rf/dispatch [:get-resource :index resource {}]))}]}
                attrs)]))

;;                 :controllers [{:start (fn [_] (rf/dispatch [:resource-index resource url (keyword resource-name "index")]))}]}

(defmethod create-route :new [{:keys [resource plural attrs]}]
  (let [resource-name (name resource)
        resource-plural (pluralise resource plural)]
    [(str "/" resource-plural "/new") (merge {:name (keyword resource-name "new")
                                            :conflicting true
                                            :controllers [{:start (fn [_] (prn resource " :new called"))}]}
                                           attrs)]))

(defmethod create-route :show [{:keys [resource plural attrs]}]
  (let [resource-name (name resource)
        resource-plural (pluralise resource plural)]
    [(str "/" resource-plural "/:id") (merge {:name (keyword resource-name "show")
                                              :conflicting true
                                              :view #'view/index-page
                                              :controllers [{:parameters {:path [:id]}
                                                            :start (fn [{:keys [path]}]
                                                                     (rf/dispatch [:get-resource :show resource path]))}]}
                                           attrs)]))

(defmethod create-route :edit [{:keys [resource plural attrs]}]
  (let [resource-name (name resource)
        resource-plural (pluralise resource plural)]
    [(str "/" resource-plural "/:id/edit") (merge {:name (keyword resource-name "edit")
                                                 :view #'view/index-page
                                                 :controllers [{:parameters {:path [:id]}
                                                                :start (fn [{:keys [path]}]
                                                                         (rf/dispatch [:get-resource :edit resource path]))}]}
                                                attrs)]))

(defn create-routes
  "Given a map containing a `resource` and a vector of `actions` create a set of Reitit route defintiions

  Valid actions are

  - :index
  - :new
  - :show
  - :edit

  Example:

      (create-routes {:resource :wibble :actions [:index :new]})
      ;; => [[\"/wibbles\" {:name :wibble/index}][\"/wibble/new\" {:name :wibble/new}]"

  [{:keys [resource actions]
    :or {actions [:index :new :show :edit]}
    :as attrs}]

  (vec (for [action actions]
         (create-route {:type action :resource resource :attrs (dissoc attrs :type :resource)}))))

(defn status []
  "compiled in clj/gandalf/core.cljc with leading slashes")
