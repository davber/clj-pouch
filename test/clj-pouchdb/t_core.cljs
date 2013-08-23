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

(deftest clj-pouch-tests
  (go

   (testing "empty DB has no docs"
     (reset-db)
     (let [c (core/all-docs db {"include-docs" true})
           res (<! c)]
       (is= (:total_rows res) 0)))

   (testing "adding doc yields singleton DB"
     (reset-db)
     (<! (core/post-doc db {:name "David" :age 46}))
     (let [res (<! (core/all-docs db))]
       (is= (:total_rows res) 1)))

   (testing "getting non-existing doc fails"
     (reset-db)
     (is (:error (<! (core/get-doc db "foo")))))

   (testing "updating doc works"
     (reset-db)
     (is= (:total_rows (<! (core/all-docs db))) 0)
     (let [doc  {:name "David" :age 46}
           post-res (<! (core/post-doc db doc))
           put-res (<! (core/put-doc db (merge doc {:_id (:id post-res)
                                                    :_rev (:rev post-res)
                                                    :name "Bosse"})))
           get-res (<! (core/get-doc db (:id post-res)))]
       (is= (:name get-res) "Bosse")))

   (testing "reset DB works"
     (reset-db)
     (let [res (<! (core/all-docs db))]
       (is= (:total_rows res) 0)))

   (testing "deleting document works"
     (reset-db)
     (let [doc {:name "Bo"}
           post-res (<! (core/post-doc db doc))
           get-res-1 (<! (core/get-doc db (:id post-res)))
           _ (is= (:name get-res-1) "Bo")
           rem-res (<! (core/remove-doc db get-res-1))
           get-res-2 (<! (core/get-doc db (:id post-res)))]
       (is (:error get-res-2))))

   (testing "deleting document with wrong rev fails"
     (reset-db)
     (let [doc {:name "Bo"}
           post-res (<! (core/post-doc db doc))
           del-res (<! (core/remove-doc db {:_id (:id post-res) :_rev "foo"}))]
       (is (:error del-res))))

   (testing "bulk insertion works"
     (reset-db)
     (let [docs [{:name "Bo"} {:name "Nisse"}]
           post-res (<! (core/bulk-docs db docs))]
       (is (not (:error post-res)))
       (is= (:total_rows (<! (core/all-docs db))) 2)))))
