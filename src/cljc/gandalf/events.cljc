(ns gandalf.events
  (:require
    [re-frame.core :as rf]
    [ajax.core :as ajax]))

;;dispatchers

(rf/reg-event-fx
 :resource-index
 (fn [{:keys [db]} _]
     {:http-xhrio {:method :get
                   :uri "/api/organisations/new"
                   :format (ajax/transit-request-format)
                   :response-format (ajax/transit-response-format)
                   :on-success [:set-new-org]}}))
