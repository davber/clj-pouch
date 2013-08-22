(ns clj-pouchdb.t_core
  "Some unit tests for the PuchDB wrapper"
  (:require-macros ;; [cemerick.cljs.test :refer (deftest with-test run-tests is testing)]
                   [cljs.core.async.macros :as m :refer [go alt!]]
                   [cljs.core.async.test-helpers :as h :refer [runner deftest is is= testing]]
                   )
  (:require ;; [cemerick.cljs.test :as t]
            [clj-pouchdb.core :as core]
            [cljs.core.async :as async :refer [<! >! timeout chan]]))

(set-print-fn! (fn [& xs] (.log js/console (apply str xs))))

(def DB "http://127.0.0.1:5984/foo")
(declare db)
(core/debug! true)

(defn- reset-db []
  (set! db (do (core/destroy-db DB) (core/create-db DB))))

(deftest new-db-has-no-docs
  (reset-db)
  (let [c (core/all-docs db {"include-docs" true})]
    (go
     (let [res (<! c)]
       (is= (:total_rows res) 0)))))

(deftest adding-doc-yields-singleton-db
  (reset-db)
  (go
   (<! (core/post-doc db {:name "David" :age 46}))
   (let [res (<! (core/all-docs db))]
     (is= (:total_rows res) 1))))

(deftest updating-doc-works
  (reset-db)
  (go
   (is= (:total_rows (<! (core/all-docs db))) 0)
   (let [doc  {:name "David" :age 46}
         post-res (<! (core/post-doc db doc)
         _ (print "post-doc returned " post-res)
         put-res (<! (core/put-doc db (merge doc {:_id (:id post-res)
                                                  :_rev (:rev post-res)})))
         _ (print "put-doc returned " put-res)
         get-res (<! (core/get-doc db (:id put-res)))
         _ (print "get-doc returned " get-res)]
     (is= (:name get-res) "Bosse"))))

(deftest reset-db-works
  (reset-db)
  (go
   (let [res (<! (core/all-docs db))]
     (print "reset-db got " res)
     (is= (:total_rows res) 0))))
