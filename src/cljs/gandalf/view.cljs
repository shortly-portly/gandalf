(ns gandalf.view
  (:require
   [gandalf.widget :as widget]
   [re-frame.core :as rf]))

(defn index-page []
  (prn "index-page called")
  [:section.section>div.container>div.content
   (let [view @(rf/subscribe [:view])]
     (if view
     [:div
      (for [widget view]
      [widget/widget widget])]))])
