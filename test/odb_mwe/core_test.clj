(ns odb-mwe.core-test
  (:use midje.sweet)
  (:require [clojure.test :refer :all]
            [clojure.tools.logging :as log]
            [odb-mwe.core :refer :all]))

(def tconn (atom nil))

;; Try different DB names
(defn connect-memory-db []
  (let [conn (connect "memory:test/mem")]
    (reset! tconn conn)))
(defn connect-plocal-db []
  (let [conn (connect "plocal:/tmp/odb_local")]
    (reset! tconn conn)))
(defn connect-remote-db []
  (let [conn (connect "remote:localhost/odb_remote" "root" "password")]
    (reset! tconn conn)))

(defn disconnect-any []
  (disconnect @tconn)
  (reset! tconn nil))

(defn create-fact
  [connect-fun msg]
  (with-state-changes [(before :facts (connect-fun))
                       (after :facts (disconnect-any))]
    (fact "create"
          (with-db @tconn g
            (let [vs (create-vertices g)
                  va (first vs)
                  vb (second vs)]
              (count vs) => 2
              (-> va (get-prop "name")) => "A"
              (-> vb (get-prop "name")) => "B"
              (-> va (get-prop "other")) => vb
              (-> vb (get-prop "other")) => va)))))

(defn select-fact
  [connect-fun msg]
  (with-state-changes [(before :facts (connect-fun))
                       (after :facts (disconnect-any))]
    (fact "select"
          (print (str "TEST ANNOTATION: " msg))
          (with-db @tconn g
            (let [va (get-named-vertex g "A")
                  vb (get-named-vertex g "B")]
              (-> va (get-prop "name")) => "A"
              (-> vb (get-prop "name")) => "B"
              (-> va (get-prop "other")) => vb
              (-> vb (get-prop "other")) => va)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MEMORY
(create-fact connect-memory-db "mem")
(select-fact connect-memory-db "mem")


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; PLOCAL
(create-fact connect-plocal-db "plocal")
(select-fact connect-plocal-db "plocal")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; REMOTE
(create-fact connect-remote-db "remote")
(select-fact connect-remote-db "remote")
