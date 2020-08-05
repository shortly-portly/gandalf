(ns gandalf.core
  (:require
   [gandalf.events]
   [re-frame.core :as rf]))

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
  (let [resource-name (name resource)]
    [(str "/" resource-name) (merge {:name (keyword resource-name "index")
                                     :controllers [{:start (fn [_] (rf/dispatch [:resource-index resource]))}]}
                                    attrs)]))

(defmethod create-route :new [{:keys [resource attrs]}]
  (let [resource-name (name resource)]
    [(str "/" resource-name "/new") (merge {:name (keyword resource-name "new")
                                            :conflicting true
                                            :controllers [{:start (fn [_] (prn resource " :new called"))}]}
                                           attrs)]))

(defmethod create-route :show [{:keys [resource attrs]}]
  (let [resource-name (name resource)]
    [(str "/" resource-name "/:id") (merge {:name (keyword resource-name "show")
                                            :conflicting true
                                            :controllers [{:start (fn [_] (prn resource " :show called"))}]}
                                           attrs)]))

(defmethod create-route :edit [{:keys [resource attrs]}]
  (let [resource-name (name resource)]
    [(str "/" resource-name "/:id/edit") (merge {:name (keyword resource-name "edit")
                                                 :controllers [{:start (fn [_] (prn resource " :edit called"))}]}
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
      ;; => [[\"/wibble\" {:name :wibble/index}][\"/wibble/new\" {:name :wibble/new}]"

  [{:keys [resource actions]
    :or {actions [:index :new :show :edit]}
    :as attrs}]

  (vec (for [action actions]
         (create-route {:type action :resource resource :attrs (dissoc attrs :type :resource)}))))

(defn status []
  "compiled in clj/gandalf/core.cljc with leading slashes")
