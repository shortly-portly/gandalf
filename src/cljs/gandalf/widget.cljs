(ns gandalf.widget
  (:require [clojure.string]
            [reagent.core :as r]
            [re-frame.core :as rf]))

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
  (if (:label field) (:label field) (field-path-to-string(:path field))))

(defmulti widget :type)

(defmethod widget :text [{:keys [path label]}]
  (let [value (r/atom @(rf/subscribe [:data path]))]
    (fn []
      [:div
      [:span @value]])))


(defmethod widget :table [view]
  (fn []
    (let [root   [(:path view)]
          fields (:fields view)
          item-count @(rf/subscribe [:item-count root])]
      [:table.table
        [:thead
         [:tr
          (for [field fields]
            ^{:key field}
            [:th
             (label-for field)])]]
       [:tbody
        (for [index (range item-count)]
          ^{:key index}
          [:tr
           (for [field fields]
             ^{:key field}
             [:td
              (let [data-path   (build-path root index (:path field))
                    widget-data (assoc field :path data-path)]
                [widget widget-data])])])]])))