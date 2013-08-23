(ns clj-pouchdb.core
  "Wrapper for PouchDB for ClojureScript"
  (:require [nat.pouchdb :as ignore]
            [cljs.core.async :as async :refer [put! <! >! timeout chan alt!!]]
            goog.debug)
  (:require-macros [cljs.core.async.macros :as m :refer [go alt!]]))

(defn- hash-to-obj
  "Convert a CLJS structure to a JS object, yielding empty JS object for nil input"
  [obj]
  (let [jso (or (clj->js obj) (js-obj))]
    jso))

(defn- obj-to-hash
  "Convert a JS object to a hash, yielding nil for nil JS object"
  [obj]
  (if obj (js->clj obj :keywordize-keys true) {}))

(declare *blob-builder*)

(defn- create-blob
  "Create a Blob object from the given data and data type"
  [data data-type]
  (try
    (js/Blob (array data) (js-obj "type" data-type))
    (catch js/Error e
      (when-not *blob-builder*
      (set! *blob-builder*
            (or js/WebKitBlobBuilder
                js/MozBlobBuilder
                js/MSBlobBuilder))
      (cond
       (and (.-name e) "TypeError" *blob-builder*)
       (let [bb (*blob-builder*.)]
         (.append bb data)
         (let [blob (.getBlob bb data-type)]
           blob))
       (= (.-name e) "InvalidStateError")
       (js/Blob (array data) (js-obj "type" data-type))
       true
       (throw (js/Error. "Cannot create blob on this platform")))))))

(defmulti to-blob type)
(defmethod to-blob (type "foo")
  [str]
  (let [blob (create-blob str "text/plain")]
    blob))
(defmethod to-blob js/Blob
  [blob]
  blob)
(defmethod to-blob :default
  [obj]
  (throw (js/Error. (str "to-blob with strange value " obj " of type " (type obj)))))

(defn- create-error
  "Create an error hash from an error, having :error as the sole key"
  [err]
  (when err {:error (obj-to-hash err)}))

(defmulti create-result type)
(defmethod create-result js/Blob
  [blob]
  blob)
(defmethod create-result :default
  [jso]
  (obj-to-hash jso))

(defn- responder
  "Create am [err resp] callback putting a proper structure to the given channel"
  [c]
  (fn [err resp]
    (let [obj (or (create-error err) (create-result resp))]
      (put! c obj))))

(defn create-db
  "Create a new PouchDB database given optional name and options"
  [& [name & [options]]]
  (js/PouchDB. name (hash-to-obj options)))

(defn destroy-db
  "Destroys a PouchDB database, returning a channel to the result"
  [name]
  (let [c (chan 1)]
    (PouchDB/destroy name (responder c))
    c))

(defn put-doc
  "Put a document, returning a channel to the result"
  [db doc & [options]]
  (let [opts (hash-to-obj options)
        js-doc (hash-to-obj doc)
        c (chan 1)]
    (.put db js-doc opts (responder c))
    c))

(defn post-doc
  "Create a new document letting PouchDB generate the _id, returning a channel
   holding the result"
  [db doc & [options]]
  (let [opts (hash-to-obj options)
        js-doc (hash-to-obj doc)
        c (chan 1)]
    (.post db js-doc opts (responder c))
    c))

(defn get-doc
  "Get document given ID, returning a channel to the result"
  [db docid & [options]]
  (let [c (chan 1)]
    (.get db docid (hash-to-obj options) (responder c))
    c))

(defn remove-doc
  "Remove document, returning channel to result"
  [db doc & [options]]
  (let [c (chan 1)]
    (.remove db (hash-to-obj doc) (hash-to-obj options) (responder c))
    c))

(defn bulk-docs
  "Create a batch of documents, returning channel to result"
  [db docs & [options]]
  (let [c (chan 1)]
    (.bulkDocs db (js-obj "docs" (hash-to-obj docs)) (hash-to-obj options) (responder c))
    c))

(defn all-docs
  "Fetch (all) documents, returning channel to result"
  [db & [options]]
  (let [c (chan 1)]
    (.allDocs db (hash-to-obj options) (responder c))
    c))

(defn changes
  "React on changes, returning channel with result"
  [db & [options]]
  (let [c (chan 1)]
    (.changes db (hash-to-obj options) (responder c))
    c))

(defn replicate-db
  "Replicate database"
  [source target & [options]]
  (PouchDB/replicate source target (hash-to-obj options)))

(defn put-attachment
  "Put an attachment to a document, returning channel to result"
  [db docid attachmentid rev doc type]
  (let [c (chan 1)]
    (.putAttachment db docid attachmentid rev (to-blob doc) type (responder c))
    c))

(defn get-attachment
  "Get an attachment from a document, returning channel to result"
  [db docid attachmentid & [options]]
  (let [c (chan 1)]
    (.getAttachment db docid attachmentid (hash-to-obj options) (responder c))
    c))

(defn remove-attachment
  "Remove attachment from a document, returning channel to result"
  [db docid attachmentid rev]
  (let [c (chan 1)]
    (.removeAttachment db docid attachmentid rev (responder c))
    c))

(defn query
  "Query the database, returning channel to results"
  [db fun & [options]]
  (let [c (chan 1)]
    (.query db fun (hash-to-obj options) (responder c))
    c))

(defn info
  "Get info about database, returning channel to result"
  [db]
  (let [c (chan 1)]
    (.info db (responder c))
    c))

(defn compact
  "Compact the database, returning channel to result"
  [db & [options]]
  (let [c (chan 1)]
    (.compact db (hash-to-obj options) (responder c))
    c))

(defn revs-diff
  "Get subset of differing revisioned documents, returning channel to result"
  [db diff]
  (let [c (chan 1)]
    (.revsDiff db diff (responder c))
    c))

(defn enable-all-dbs
  "Enable or disable access to all dataabases"
  [flag]
  (set! (.-enableAllDbs js/PouchDB) flag))

(defn all-dbs
  "Get all databases, returning channel to result"
  []
  (let [c (chan 1)]
    (PouchDB/allDbs (responder c))
    c))

(defn debug!
  "Set debugging of Pouch operations"
  [flag]
  (set! (.-DEBUG js/PouchDB) flag))
