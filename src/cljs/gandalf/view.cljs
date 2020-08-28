(ns gandalf.view
  (:require
   [gandalf.widget :as widget]
   [re-frame.core :as rf]))

(defn index-page []
  [:section.section>div.container>div.content
   (when-let [view @(rf/subscribe [:view])]
     (prn "index-page view :" view)
     [:div
      [widget/widget view]])])
