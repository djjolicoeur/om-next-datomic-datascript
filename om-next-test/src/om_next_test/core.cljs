(ns om-next-test.core
  (:require [goog.dom :as gdom]
            [goog.net.XhrIo :as xhr]
            [goog.events :as events]
            [goog.net.EventType :as et]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [datascript.core :as d]))


(enable-console-print!)


(def conn (d/create-conn {}))

(defn get-url [url on-success]
  (let [xhr (goog.net.XhrIo.)]
    (doto xhr
      (events/listen et/SUCCESS on-success)
      (.send url "GET"))))

(defn examine [x]
  (.log js/console x) x)

(defn bootstrap-success [event]
  (let [res (-> event
                .-target
                .getResponseJson
                (js->clj :keywordize-keys true))]
    (.log js/console "IMPORTED " (pr-str res))
    (d/transact! conn res)
    (.log js/console
          (str "Q"
               (d/q '[:find (pull ?e [*])
                      :where [?e :todo/title _]] (d/db conn))))))

(defn bootstrap-data []
  (get-url "http://127.0.0.1:8484/api/v0.1/bootstrap" bootstrap-success))


(defmulti read om/dispatch)

(defmethod read :app/counter
  [{:keys [state selector] :as env} _ _]
  {:value (d/q '[:find [(pull ?e ?selector) ...]
                 :in $ ?selector
                 :where [?e :app/title]]
               (d/db state) selector)})

(defmethod read :app/title
  [{:keys [state selector] :as env} _ _]
  {:value (d/q '[:find [(pull ?e ?selector) ...]
                 :in $ ?selector
                 :where [?e :app/title]]
               (d/db state) selector)})




(defmethod read :todo/items
  [{:keys [state selector] :as env} _ _]
  (.log js/console (str "todo/items STATE"  (pr-str state)))
  (.log js/console (str "todo/items SELECTOR "  (pr-str selector)))
  {:value (d/q '[:find [(pull ?e ?selector) ...]
                 :in $ ?selector
                 :where [?e :todo/title _]] (d/db state) selector)})


(defmulti mutate om/dispatch)

(defmethod mutate 'app/increment
  [{:keys [state] :as env} _ entity]
  {:value [:app/counter]
   :action (fn []
              (let [updated (assoc entity :app/count (inc (:app/count entity)))]
                (d/transact! state [updated])))})


(defui Todo
  static om/IQuery
  (query [this]
         '[:db/id :todo/title :todo/description])
  Object
  (render [this]
          (let [{:keys [db/id todo/title todo/description] :as entity}
                (om/props this)]
            (dom/li nil
                    (dom/label nil
                               (str id " , " title " , " description))))))

(def todo (om/factory Todo))

(defui Todos
  static om/IQueryParams
  (params [this]
          {:todo-item (om/get-query Todo)})
  static om/IQuery
  (query [this]
         '[:db/id :todo/title :todo/description])
  Object
  (render [this]
          (.log js/console (str "TODO PROPS " (pr-str (om/props this))))
          (let [list (om/props this)]
            (dom/div nil
                     (apply dom/ul nil
                            (mapv todo list))))))

(def todos (om/factory Todos))


(defui Counter
  static om/IQuery
  (query
   [this]
   '[:db/id :app/count])
  Object
  (render
   [this]
   (.log js/console (str  "COUNTER PROPS " (pr-str (om/props this))))
   (let [{:keys [app/count] :as entity} (om/props this)]
     (dom/div nil
              (dom/span nil (str "Count: " count))
              (dom/button
               #js {:onClick
                    (fn [e]
                      (om/transact! this `[(app/increment ~entity)]))}
               "Click Me")))))

(defui Title
  static om/IQuery
  (query
   [this]
   '[:app/title])
  Object
  (render
   [this]
   (.log js/console (str "TITLE PROPS" (om/props this)))
   (let [{:keys [app/title] :as e} (om/props this)]
     (dom/h2 nil title))))

(def title-view (om/factory Title))

(def counter-view (om/factory Counter))

(defui RootView
  static om/IQueryParams
  (params [this]
          {:app-title (om/get-query Title)
           :counter (om/get-query Counter)
           :todo-list (om/get-query Todos)})
  static om/IQuery
  (query [this]
         '[{:app/counter ?counter}
           {:app/title ?app-title}
           {:todo/items ?todo-list}])
  Object
  (render
   [this]
   (.log js/console (str "ROOT PROPS" (pr-str  (om/props this))))
   (let [{:keys [app/counter app/title todo/items] :as props} (om/props this)]
     (.log js/console (str "TITLE " (pr-str title)))
     (dom/div nil (title-view (first title))
              (counter-view (first counter))
              (dom/h3 nil "ToDos")
              (todos items)))))


(bootstrap-data)

(d/transact! conn
             [{:db/id -1
               :app/title "Pedestal, Datomic, Datascript, OM MY!!!"
               :app/count 0}])

(def reconciler
  (om/reconciler
   {:state conn
    :parser (om/parser {:read read :mutate mutate})}))

(om/add-root! reconciler
              RootView
              (gdom/getElement "app"))
