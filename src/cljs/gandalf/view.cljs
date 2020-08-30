(ns gandalf.view
  (:require
   [gandalf.widget :as widget]
   [re-frame.core :as rf]))

(defn index-page []
  [:section.section>div.container>div.container-fluid
   (let [view @(rf/subscribe [:view])]
     (if view
     [:div
      (for [widget view]
        ^{:key widget}
      [widget/widget widget])]))])
