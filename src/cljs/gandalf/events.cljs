(ns gandalf.events
  (:require
    [re-frame.core :as rf]
    [ajax.core :as ajax]
    [reitit.core :as r]))

;;dispatchers

(rf/reg-event-fx
 :get-resource
 (fn [{:keys [db]} [_ action resource params ]]
   (let [router (:router db)
         path-name (keyword resource (name action))
         match (r/match-by-name router path-name params)
         path  (:path match)]
     {:http-xhrio {:method :get
                   :uri path
                   :format (ajax/transit-request-format)
                   :response-format (ajax/transit-response-format)
                   :on-success [:set-resource-view]
                   :on-failure [:oops]}})))

;; The :set-resource event is called whenever a succesful request to :get-resource event for a resource
;; has been received.
;;
;; The reply will contain up to four elements from the server:
;;
;; resource - the name of the for which the data, view and schema apply.
;; data - the actual data for this resource e.g. a list of user records.
;; view - a map defining how to display the data.
;; schema - a malli schema definition defining how the data should be validated.
(rf/reg-event-db
 :set-resource-view
 (fn [db [_ {:keys [resource data schema view]}]]
   (prn ":set-resource-view called")
   (-> db
       (assoc :resource resource)
       (assoc-in [:data resource] data)
       (assoc-in [:schema resource] schema)
       (assoc-in [:view resource] view)
       )))


;; The :oops event is called whenever a http request to the server fails for some reason.
;; It displays the returned error message to the console.
(rf/reg-event-db
 :oops
 (fn [db [_ result]]
   (prn "oops")
   (prn result)
   db))

(rf/reg-event-db
 :set-router
 (fn [db [_ router]]
   (assoc db :router router)))

;; The :view subscription function returns the definition of how the currently defined :resource should
;; be displayed. Note: It is possible that no :resource has been defined in which case this function should
;; return nil.
;;
;; The data returned by this function is a map of widgets with each widget defining the associated
;; data to be displayed as a path under the :data key.
;;
;; Examples:
;;
;; Assuming the current resource is :users
;;
;; (rf/subscribe [:view])
;;
;; would return
;;
;; {:path :users
;;        :type :table
;;        :fields
;;        [{:path :first_name
;;          :label "First Name"
;;          :type :text}

;;         {:path :last_name
;;          :label "Last Name"
;;          :type :text}

;;         {:path :email
;;          :label "Email"
;;          :type :text}]}

(rf/reg-sub
 :view
 (fn [db _]
   (let [resource (:resource db)]
     (if resource
       (get-in db [:view resource])
       nil))))

;; The :item-count subscription returns the number of items in a collection with the given path under the
;; :data key.
;;
;; Examples:
;;
;; Assume you have a collection of posts under the :posts key.
;;
;; (rf/subscribe [:item-count :posts)
;;
;; would return the number of posts in the collection.

(rf/reg-sub
 :item-count
 (fn [db [_ path]]
   (let [data (:data db)]
     (count (get-in data path)))))

;; The :data subscription returns the data associated with the given path under the :data
;; key in the db.
;;
;; Examples:
;;
;; (rf/subscribe [:data [:user :first_name]) would return the first name of the user held under the :data key.
;;
;; For collections you need to also pass in the index. So if we had a collection of :users and wanted the first name
;; of the second entry we do:
;;
;; (rf/subscribe [:data [:user 1 :first_name]])

(rf/reg-sub
 :data
 (fn [db [_ path]]
   (let [data (:data db)]
     (get-in data path))))

