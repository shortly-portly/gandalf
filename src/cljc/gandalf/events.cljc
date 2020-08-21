(ns gandalf.events
  (:require
    [re-frame.core :as rf]
    [ajax.core :as ajax]))

;;dispatchers

(rf/reg-event-fx
 :resource-index
 (fn [{:keys [db]} [_ resource url]]
     {:http-xhrio {:method :get
                   :uri url
                   :params {:resource resource}
                   :format (ajax/transit-request-format)
                   :response-format (ajax/transit-response-format)
                   :on-success [:set-resource-index resource]
                   :on-failure [:oops]}}))

(rf/reg-event-db
 :set-resource-index
 (fn [db [_ resource {:keys [data schema view]}]]
   (-> db
       (assoc :resource resource)
       (assoc-in [:data resource] data)
       (assoc-in [:schema resource] schema)
       (assoc-in [:view resource] view))))

(rf/reg-event-db
 :oops
 (fn [db [_ result]]
   (prn "oops")
   (prn result)
   db))

(rf/reg-sub
 :view
 (fn [db _]
   (let [resource (:resource db)]
     (if resource
       (get-in db [:view resource])
       nil))))
