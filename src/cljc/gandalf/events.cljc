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
 (fn [db [_ resource data]]
   (assoc db resource data)))

(rf/reg-event-db
 :oops
 (fn [db [_ result]]
   (prn "oops")
   (prn result)
   db))
