(defproject odb-mwe "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-midje "3.2"]
            [lein-junit "1.1.8"]]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/java.classpath "0.2.2"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [org.clojure/tools.logging "0.3.1"]
                 [junit "4.4"]
                 ;; Databases
                 [com.orientechnologies/orientdb-client "2.1.13"]
                 [com.tinkerpop.blueprints/blueprints-core "2.6.0"]
                 [com.orientechnologies/orientdb-graphdb "2.1.13"]]
  :junit ["test"]
  :java-source-paths ["src" "test"]
  :main ^:skip-aot odb-mwe.core
  :target-path "target/%s"
  :profiles {:dev {:dependencies [[midje "1.8.2"]]}
             :uberjar {:aot :all}})
