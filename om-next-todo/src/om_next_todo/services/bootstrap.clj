(ns om-next-todo.services.bootstrap
  (:require [datomic.api :as d]
            [ring.util.response :as resp]))


(defn get-everything [db]
  (->> (d/q '[:find (pull ?e [*])
              :where [?e :todo/title _]]
            db)
       (map first)
       (sort-by :db/id)))


(defn bootstrap [{:keys [db-conn] :as request}]
  (let [db (d/db db-conn)]
    (resp/response (get-everything db))))
