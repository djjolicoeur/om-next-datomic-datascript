(ns om-next-todo.system
  (:require [com.stuartsierra.component :as component]
            [om-next-todo.components.server :refer [new-service-map new-server]]
            [om-next-todo.components.db :refer [new-datomic-db]]
            [environ.core :refer [env]]))



(defn local-system []
  (component/system-map
   :datomic-uri (env :datomic-uri)
   :dev-facts? (or  (env :dev-facts?) false)
   :db (component/using (new-datomic-db) [:datomic-uri :dev-facts?])
   :service-map (component/using (new-service-map) [:db])
   :server (component/using (new-server) [:service-map])))
