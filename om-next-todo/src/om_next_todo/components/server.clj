(ns om-next-todo.components.server
  (:require [com.stuartsierra.component :as component]
            [datomic.api :as d]
            [io.pedestal.http :as server]
            [io.pedestal.http.route.definition :as rdef]
            [io.pedestal.interceptor :refer [interceptor]]
            [io.pedestal.interceptor.helpers :refer [on-request]]
            [io.pedestal.log :refer [trace debug info warn error]]
            [om-next-todo.service :as service]))


;; Inject db-conn on incoming reqs
(defn inject-db-interceptor [conn]
  (on-request ::inject-db-conn
              (fn [context]
                (info "INJECTING DB CONTEXT ON REQUEST" conn)
                (assoc context :db-conn conn))))


(defrecord ServiceMap [db]
  component/Lifecycle
  (start [this]
    (info "Building pedestal service map...")
    (info "Injecting db " (:conn db))
    (let [db-interceptor (inject-db-interceptor (:conn db))
          service-map (merge service/service
                             {:env :dev
                              ::server/join? false
                              ::server/routes service/routes
                              ::server/allowed-origins
                              {:creds true
                               :allowed-origins (constantly true)}})
          service-map (-> service-map
                          server/default-interceptors
                          server/dev-interceptors
                          (update-in  [::server/interceptors]
                                      conj db-interceptor))]
      (assoc this :service-map service-map)))
  (stop [this]
    (update-in this [:service-map ::server/interceptors] pop)))

(defn new-service-map []
  (map->ServiceMap {}))


(defrecord Server [service-map]
  component/Lifecycle
  (start [this]
    (info "Starting Service")
    (let [server (server/create-server (:service-map service-map))]
      (server/start server)
      (assoc this :server server)))
  (stop [this]
    (update-in this [:server] server/stop)))

(defn new-server []
    (map->Server {}))
