(ns gandalf.widget
  (:require [clojure.string]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [reitit.frontend.easy :as rfe]))

(defn build-path
  "Generate a full path from a root, possible index and path element.

  A widget generally defines a :path key whose value  is itself a key whose value is the
  value for the widget.

  It is possible to pass a path value of nil which will simply return the root value. This
  allows for a container style widgets that doesn't itself display data but contains widgets
  that do."
  ([root path]
   (if  path (if (vector? path) path (conj [] root path)) root))
  ([root index path]
   (if  path (if (vector? path) path (conj [] root index path)) root) ))

(defn field-path-to-string
  "Returns a string version of the provided keyword path.

  This allows the automatic generation of a label from a keyword."
  [path]
  (-> (name path)
      (clojure.string/replace "_" " ")
      (clojure.string/capitalize)))

(defn label-for
  "Returns the label for a the given `field`.

  `field` is a map that may contain an optional :label key - in which
  case this value is used. If not then the field name is the string version of
  the field path with the first letter capitalised and any underscores replaced
  with spaces."
  [field]
  (if (:label field) (:label field) (field-path-to-string (:path field))))

(def default-button-actions
  {:show [:get-resource]
   :edit [:post-resource]
   :delete [:delete-resource]
   })

(defn build-row-action
  [action resource id]
  ^{:key action}
  [:a {:href (rfe/href (keyword resource (name action)) {:id id})} action])


(defn build-form-action
  [action resource id]
  ^{:key action}
  [:button.btn {:href (rfe/href (keyword resource (name action)) {:id id})} action])

(defmulti widget :type)

(defmethod widget :list-actions [{:keys [path resource actions]}]
  (let [id @(rf/subscribe [:data path])]
    [:div
     (map #(build-row-action % resource id) actions)]))


(defmethod widget :form-actions [{:keys [path resource actions]}]
  (let [id @(rf/subscribe [:data [:user path]])
        ]
    [:div
     (map #(build-form-action % resource id) actions)]))

(defmethod widget :button [{:keys [label dispatch style] :as button-data}]
  [:button.btn.mr-2
   {:class style
    :on-click #(rf/dispatch [dispatch button-data])} label])

(defmethod widget :cell [{:keys [path]}]
  (let [value (r/atom @(rf/subscribe [:data path]))]
    [:div
     [:span @value]]))

(defmethod widget :text-input [{:keys [path schema-path label] :as widget-data}]
  (let [value (r/atom @(rf/subscribe [:data path]))]
    (fn []
      (let [error @(rf/subscribe [:error path])]
        ^{:key widget-data}
        [:div.form-group
         [:label label]
         [:input.form-control
         {:type :text
          :class (if error "is-invalid" "is-valid")
          :value @value
          :on-change #(reset! value (-> % .-target .-value))
          :on-blur #(rf/dispatch [:update path schema-path @value])}]
        (if error [:div.invalid-feedback error]) ]))))

(defmethod widget :text [{:keys [path label] :as widget-data}]
  (let [value (r/atom @(rf/subscribe [:data path]))]

    ^{:key widget-data}
    [:div.row
     [:div.col-sm label]
     [:div.col-sm @value]]))


(defmethod widget :table [view]
  (let [root        (:path view)
        fields      (:fields view)
        row-actions (:row-actions view)
        item-count  @(rf/subscribe [:item-count [root]])]
    [:table.table
     [:thead
      [:tr
       (for [field fields]
         ^{:key field}
         [:th
          (label-for field)])
       (if row-actions [:th "Actions"])]]
     [:tbody
      (for [index (range item-count)]
        ^{:key index}
        [:tr
         (for [field fields]
           ^{:key field}
           [:td
            (let [data-path   (build-path root index (:path field))
                  widget-data (assoc field :path data-path)]
              [widget widget-data])])])]]))

(defmethod widget :card [view]
  (let [root (:path view)
        fields (:fields view)]
    ^{:key view}
    [:div.card
     ^{:key fields}
     [:div.card-body
      (doall (for [field fields]
               (let [data-path (build-path root (:path field))
                     widget-data (assoc field :path data-path)]
               ^{:key field}
                 [widget widget-data])))]]))

(defmethod widget :form [form-data]
  (let [resource (:resource form-data)
        root   (:path form-data resource)
        fields (:fields form-data)
        actions (:actions form-data)]
      [:form
        (for [field fields]
         (let [data-path   (build-path root (:path field))
               schema-path (build-path root (:path field))
               widget-data (assoc field :path data-path :schema-path schema-path)]
           ^{:key (:path field)} [widget widget-data]))

       (for [action actions]
         (let [params (:params action)
               widget-data (assoc action :resource resource :parama params)]
           ^{:key (:path action)} [widget widget-data]))

       ]))

(defmethod widget :submit-btn [button-data]
  (let [resource (:resource button-data)
        params (:params button-data [:id])
        label (:label button-data "Submit")
        style (:style button-data "btn-primary")]
  (fn []
    [:button.btn.mr-2 {:class style
                       :on-click #(rf/dispatch [:post-resource resource :update])} label])))

(defmethod widget :two-columns [view]
  (let [root (:path view)
        column-1 (:column-1 view)
        column-2 (:column-2 view)]
    [:div.row
     ^{:key column-1}
     [:div.col-sm
      (doall (for [field column-1]
               (let [data-path (build-path root (:path field))
                     widget-data (assoc field :path data-path)]
               ^{:key field}
                 [widget widget-data])))]

     ^{:key column-2}
     [:div.col-sm
      (doall (for [field column-2]
               (let [data-path (build-path root (:path field))
                     widget-data (assoc field :path data-path)]
               ^{:key field}
                 [widget widget-data])))]]))
