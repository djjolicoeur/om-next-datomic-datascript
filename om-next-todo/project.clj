(defproject om-next-todo "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :creds :gpg}}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.nrepl "0.2.10"]
                 [org.clojure/tools.namespace "0.2.3"]
                 [io.pedestal/pedestal.service "0.4.0"]
                 ;; Remove this line and uncomment one of the next lines to
                 ;; use Immutant or Tomcat instead of Jetty:
                 [io.pedestal/pedestal.jetty "0.4.0"]
                 ;; [io.pedestal/pedestal.immutant "0.4.0"]
                 ;; [io.pedestal/pedestal.tomcat "0.4.0"]
                 [com.stuartsierra/component "0.2.3"]
                 [com.datomic/datomic-pro "0.9.5206"
                  :exclusions [joda-time org.slf4j/slf4j-nop]]
                 [ch.qos.logback/logback-classic "1.1.2"
                  :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.7"]
                 [org.slf4j/jcl-over-slf4j "1.7.7"]
                 [org.slf4j/log4j-over-slf4j "1.7.7"]
                 [org.clojure/core.cache "0.6.3"]
                 [org.clojure/core.memoize "0.5.6"
                  :exclusions [org.clojure/core.cache]]
                 [org.apache.httpcomponents/httpclient "4.3.5"]
                 [environ "1.0.0"]]
  :plugins [[lein-environ "1.0.0"]]
  :min-lein-version "2.0.0"
  :resource-paths ["config", "resources"]
  :profiles {:dev {:source-paths ["dev"]
                   :plugins [[lein-environ "1.0.0"]
                             [cider/cider-nrepl "0.10.0-SNAPSHOT"]]
                   :dependencies [[io.pedestal/pedestal.service-tools "0.4.0"]]
                   :env {:dev-facts? true
                         :datomic-uri "datomic:dev://localhost:4334/om-todo"}}
             :uberjar {:aot [om-todo.app]}})
  ;:main ^{:skip-aot true} dali-api.server)
