(ns om-next-todo.components.db
  (:require [com.stuartsierra.component :as component]
            [clojure.walk :refer [postwalk]]
            [datomic.api :as d]
            [clojure.java.io :as io]
            [io.pedestal.log :refer [info]]))


(defn bootstrap [conn]
  (let [schema (io/reader "resources/schema.edn")]
    (doseq [datom (datomic.Util/readAll schema)]
      @(d/transact conn [datom]))))

(defn load-facts [conn]
  (let [facts (io/reader "resources/dev-facts.edn")]
    (doseq [datom (datomic.Util/readAll facts)]
      @(d/transact conn [datom]))))

(defrecord Datomic [datomic-uri dev-facts? conn]
  component/Lifecycle
  (start [this]
    (info "START DB " nil)
    (if conn
      this
      (let [_ (info "URI " datomic-uri)
            datomic-uri (cond-> datomic-uri
                          dev-facts? (str "-" (java.util.UUID/randomUUID)))
            db (d/create-database datomic-uri)
            conn (d/connect datomic-uri)]
        (info "LOADING SCHEMA" nil)
        (bootstrap conn)
        (when dev-facts?
          (info "LOADING FACTS" nil)
          (load-facts conn))
        (assoc this :conn conn :datomic-uri datomic-uri))))
  (stop [this]
    (if conn
      (do
        (info "STOPPING DB" nil)
        (when dev-facts?
          (info "DELETING DB" nil)
          (d/delete-database (:datomic-uri this)))
        (info "STOPPED DB" nil)
        (assoc this :conn nil :datomic-uri nil))
      this)))

(defn new-datomic-db []
        (map->Datomic {}))
