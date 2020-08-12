(ns gandalf.events
  (:require
    [re-frame.core :as rf]
    [ajax.core :as ajax]))

;;dispatchers

(rf/reg-event-fx
 :resource-index
 (fn [{:keys [db]} _]
     {:http-xhrio {:method :get
                   :uri "/wibbles"
                   :format (ajax/transit-request-format)
                   :response-format (ajax/transit-response-format)
                   :on-success [:set-new-org]
                   :on-failure [:oops]}}))

(rf/reg-event-db
 :set-new-org
 (fn [db [_ result]]
   (prn ":set-new-org called")
   db))

(rf/reg-event-db
 :oops
 (fn [db [_ result]]
   (prn "oops")
   (prn result)
   db))
