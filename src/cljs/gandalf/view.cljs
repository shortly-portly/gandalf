(ns gandalf.view
  (:require
   [gandalf.widget :as widget]
   [re-frame.core :as rf]))

;; (defn index-page []
;;   [:section.section>div.container>div.content
;;    (when-let [resource @(rf/subscribe [:view])
;;               view (:view resource)
;;               data (:data resource)]
;;        [:h1 "wibble"])])

(defn index-page []
  [:section.section>div.container>div.content
   (when-let [view @(rf/subscribe [:view])]

     [widget/widget view])])
