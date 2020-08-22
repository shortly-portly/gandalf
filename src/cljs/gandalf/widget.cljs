(ns gandalf.widget
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(defn build-path
  ([root path] (if (vector? path) path (conj root path)))
  ([root index path] (if (vector? path) path (conj root index path))))

(defmulti widget :type)

(defmethod widget :text [{:keys [path label]}]
  (let [value (r/atom @(rf/subscribe [:data path]))]
    (fn []
      [:div
      [:label label]
      [:span @value]])))

(defmethod widget :table [view]
  (fn []
    (let [root   [(:path view)]
          fields (:fields view)
          item-count @(rf/subscribe [:item-count root])]
      [:div
       (for [index (range item-count)]
       (for [field fields]
         (let [data-path   (build-path root index (:path field))
               widget-data (assoc field :path data-path)]

           ^{:key (:path field)}  [widget widget-data])))])))


;;                widget-data (assoc field :data-path data-path :schema-path schema-path)]
;;            ^{:key (:path field)} [widget widget-data]))

;;         (for [action actions]
;;           ^{:key (:dispatch action)} [widget action])])))


;; (defmethod widget :collection [collection-data]
;;   (fn []
;;     (let [root (:data-path collection-data)
;;           fields (:fields collection-data)
;;           item-count @(rf/subscribe [:item-count root])]
;;       [:div
;;        (for [index (range item-count)]
;;          (for [field fields]
;;            (let [data-path (build-path root index (:path field))
;;                  schema-path (build-path root (:path field))
;;                  widget-data (assoc field :data-path data-path :schema-path schema-path)]
;;          ^{:key (:path field)} [widget widget-data])))])))
