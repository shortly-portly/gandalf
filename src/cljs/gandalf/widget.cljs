(ns gandalf.widget
  (:require [clojure.string]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [reitit.frontend.easy :as rfe]
            ))

(defn build-path
  ([root path] (if (vector? path) path (conj root path)))
  ([root index path] (if (vector? path) path (conj root index path))))

(defn field-path-to-string
  "Returns a string version of the provided keyword path."
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

(def default-row-actions
  {:show [:get-resource]
   :edit [:post-resource]
   :delete [:delete-resource]})

(defn build-row-action
  [action resource id]
  ^{:key action}
  [:a {:href (rfe/href (keyword resource (name action)) {:id id})} action])

(defmulti widget :type)

(defmethod widget :text [{:keys [path]}]
  (let [value (r/atom @(rf/subscribe [:data path]))]
      [:div
       [:span @value]]))

(defmethod widget :actions [{:keys [path resource actions]}]
  (let [id @(rf/subscribe [:data path])]
      [:div
    (map #(build-row-action % resource id) actions)]))

(defmethod widget :button [{:keys [label dispatch style] :as button-data}]
  (prn ":button dispatch :" dispatch)
    [:button.btn.mr-2
     {:class style
      :on-click #(rf/dispatch [dispatch button-data])} label])

(defmethod widget :cell [{:keys [path]}]
  (let [value (r/atom @(rf/subscribe [:data path]))]
      [:div
       [:span @value]]))

(defmethod widget :table [view]
  (prn ":table :" view)
    (let [root        [(:path view)]
          fields      (:fields view)
          row-actions (:row-actions view)
          item-count  @(rf/subscribe [:item-count root])]
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
