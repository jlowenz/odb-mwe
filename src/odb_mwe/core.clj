(ns odb-mwe.core
  (:require [clojure.string :refer [join]]
            [clojure.tools.logging :as log])
  (:gen-class)
  (:import (com.tinkerpop.blueprints.impls.orient
            OrientGraphFactory OrientVertex OrientEdge OrientGraph OrientGraphNoTx OrientEdge OrientVertex OrientElement)
           (com.orientechnologies.orient.core.db.document
            ODatabaseDocumentTx)
           (com.orientechnologies.orient.core.sql 
            OCommandSQL)
           (com.orientechnologies.orient.core.sql.query
            OSQLSynchQuery)
           (com.orientechnologies.common.concur
            ONeedRetryException)))

;; A GraphConnection record stores the factory for producing
;; transactional connections to ODB. A connection is required for any
;; of the calls.
(defrecord GraphConnection [^OrientGraphFactory factory 
                            ^String db-connection-path
                            ^int max-tx-retries])

(defn- new-factory 
  "Construct a new connection factory with the given parameters"
  ([path min-threads max-threads]
   (-> (OrientGraphFactory. path) (.setupPool min-threads max-threads)))
  ([path user pass min-threads max-threads]
   (-> (OrientGraphFactory. path user pass) (.setupPool min-threads max-threads))))

(defn ^GraphConnection connect
  "Get an OrientGraphFactory in order to create transactional graph
  instances"
  ([connection-path] (GraphConnection. (new-factory connection-path 4 16)
                                       connection-path
                                       3))
  ([connection-path user pass] (GraphConnection. (new-factory connection-path user pass 4 16)
                                                 connection-path 3))
  ([connection-path max-tx-retries] (GraphConnection. (new-factory connection-path 4 16)
                                                      connection-path
                                                      max-tx-retries))
  ([connection-path user pass max-tx-retries] 
   (GraphConnection. (new-factory connection-path user pass 4 16)
                     connection-path max-tx-retries)))

(defn disconnect 
  "Close the factory and clear the DB pool."
  [conn] 
  (.close (:factory conn)))

(defn ^OrientGraph get-odb
  "Get a transaction interface to ODB."
  [conn] (.getTx (:factory conn)))

(defn try-db-times* 
  "Executes thunk. If an exception is thrown, will retry. At most n
  retries are done. If still some exepection is thrown it is bubbled
  upwards in the call chain."
  [n thunk]
  (loop [n n]
    (if-let [result (try [(thunk)]
                         (catch ONeedRetryException e
                           (when (zero? n) (throw e))))]
      (result 0)
      (recur (dec n)))))

(defmacro try-db-times 
  "Executes body. If an exception is thrown, will retry. At most n
  retries are done. If still the retry exception is thrown, it is
  bubbled upwards"
  [n & body]
  `(try-db-times* ~n (fn [] ~@body)))

(defmacro with-db
  "Perform a transaction with the given DB"
  [conn odb & body]
  `(let [~odb (get-odb ~conn)]
     (try 
       (try-db-times 
        (:max-tx-retries ~conn) 
        (do ~@body (.commit ~odb)))
       (catch Exception e#
         (do (.rollback ~odb) 
             (throw e#)))
       (finally (.shutdown ~odb)))))

(defn- as-class
  [cls]
  (str "class:" cls))

(defn ^OrientVertex add-vertex
  "Add a new vertex to the graph"
  ([graph] (.addVertex graph nil))
  ([graph v-type] (.addVertex graph ^String v-type nil))
  ([graph v-type props]
   (let [arr (into-array Object [props])]
     (.addVertex graph (as-class v-type) arr))))

(defn set-prop! 
  [^OrientElement el ^String name val]
  (.setProperty el name val))

(defn get-prop
  [^OrientElement el ^String name]
  (.getProperty el name))

(defn sql-query
  [graph qry]
  (.execute (.command graph (.setFetchPlan (OCommandSQL. qry) "*:-1")) (into-array [])))

(defn create-vertices
  [g] 
  (let [v1 (add-vertex g "V" {"name" "A"})
        v2 (add-vertex g "V" {"name" "B" "other" v1})]
    (set-prop! v1 "other" v2)
    [v1 v2]))

(defn get-named-vertex
  [g name]
  (let [sql (str "select from V where name='" name "'")
        vs (seq (sql-query g sql))]
    (log/info "vs is: " vs)
    (first vs)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args])
