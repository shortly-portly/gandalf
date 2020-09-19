(ns gandalf.events
  (:require
    [re-frame.core :as rf]
    [ajax.core :as ajax]
    [reitit.core :as r]
    [malli.core :as m]
    [malli.edn :as edn]
    [malli.util :as mu]
    [malli.error :as me]
    [malli.transform :as mt]))

(defn validate [db widget-schema value data-path]
  (let [converted-value (m/decode widget-schema value mt/string-transformer)
         valid? (m/validate widget-schema converted-value)]
     (if valid?
       (->
        (assoc-in db (into [:data] data-path) converted-value)
        (assoc-in  (into [:error] data-path) nil))
       (->
        (assoc-in db (into [:error] data-path)
                  (-> (m/explain widget-schema converted-value)
                      (me/humanize)))))))

;; dispatchers
(rf/reg-event-fx
 :get-resource
 (fn [{:keys [db]} [_ action resource params ]]
   (prn ":get-resource action :" action)
   (prn ":get-resourc resource :" resource)
   (prn ":get-resource params :" params)
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

     ;;                  :on-click #(rf/dispatch [:post-resource resource params ])} label])))
(rf/reg-event-fx
 :post-resource
 (fn [{:keys [db]} [_ resource action]]
   (prn ":get-resource action :" action)
   (prn ":get-resourc resource :" resource)
   (prn ":get-resource params :" (get-in db [:data resource :id]))
   (let [router (:router db)
         path-name (keyword resource (name action))
         id-value (get-in db [:data resource :id])
         match (r/match-by-name router path-name {:id id-value})
         path  (:path match)]

     {:http-xhrio {:method :put
                   :uri path
                   :params (get-in db [:data resource])
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
;;
;; Note the malli schema is passed from the server as a string so needs converting before
;; storing.

(rf/reg-event-db
 :set-resource-view
 (fn [db [_ {:keys [resource data schema view]}]]
   (-> db
       (assoc :resource resource)
       (assoc-in [:data resource] data)
       (assoc-in [:schema resource] (edn/read-string schema))
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

;; The :button-press event handles the pressing of a button on click event.
(rf/reg-event-db
 :button-press
 (fn [db [_ button-data]]
   (let [path (:path button-data)]
     (assoc-in db path "wibble"))))



;; The :update event updates the db store for the given data-path.
;; For validation an optional scheam-path can be provided
(rf/reg-event-db
 :update
 (fn [db [_ data-path schema-path value]]
   (let [resource (db :resource)
         schema-path (if schema-path schema-path data-path)
         schema (get-in db [:schema resource])
         widget-schema (mu/get-in schema schema-path)]
     (if widget-schema
       (validate db widget-schema value data-path)
       (->
        (assoc-in db (into [:data] data-path) value)
        (assoc-in  (into [:error] data-path) nil))))))

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


(rf/reg-sub
 :error
 (fn [db [_ path]]
   (let [error (:error db)]
     (first (get-in error path)))))
