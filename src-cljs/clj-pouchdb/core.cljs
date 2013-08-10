(ns clj-pouchdb.core
  "Wrapper for PouchDB for ClojureScript")

(defn create
  "Create a new PouchDB database given optional name and options"
  [& [name & [options]]]
  (js/PouchDB. name (.strobj options)))

(defn destroy
  "Destroys a PouchDB database"
  [name & [cb]]
  (PouchDB/destroy name cb))

(defn put
  "Put a docuemnt"
  [db doc & [options & [cb]]]
  (.put db doc (.strobj options) cb))

(defn post
  "Create a new document letting PouchDB generate the _id"
  [db doc & [options & [cb]]]
  (.post db doc (.strobj options) cb))

(defn get
  "Get document given ID"
  [db docid & [options & [cb]]]
  (.get db docid (.strobj options) cb))

(defn remove
  "Remove document"
  [db doc & [options & [cb]]]
  (.remove db doc (.strobj options) cb))

(defn bulk-docs
  "Create a batch of documents"
  [db docs & [options & cb]]
  (.bulkDocs db docs (.strobj options) cb))

(defn all-docs
  "Fetch (all) documents"
  [db & [options & [cb]]]
  (.allDocs db (.strobj options) cb))

(defn changes
  "React on changes"
  [db options]
  (.changes db (.strobj options)))

(defn replicate
  "Replace database"
  [source target & [options]]
  (PouchDB/replicate source target (.strobj options)))

(defn put-attachment
  "Put an attachment to a document"
  [db docid attacheentid rev doc type & [cb]]
  (.putAttachment db docid attachmentid rev doc type cb))

(defn get-attachment
  "Get an attachment from a document"
  [db docid attachmentid & [options & [cb]]]
  (.getAttachment db docid attachmentid (.strobj options) cb))

(defn remove-attachment
  "Remove attachment from a document"
  [db docid attachmentid rev & [cb]]
  (.removeAttachment db docid attachmentid rev cb))

(defn query
  "Query the database"
  [db fun & [options & [cb]]]
  (.query db fun (.strobj options) cb))

(defn info
  "Get info about database"
  [db cb]
  (.info db cb))

(defn compact
  "Compact the database"
  [db & [options & [cb]]]
  (.compact db (.strobj options) cb))

(defn revs-diff
  "Get subset of differing revisioned documents"
  [diff & [cb]]
  (.revsDiff db diff cb))

(defn enable-all-dbs
  "Enable or disable access to all dataabases"
  [flag]
  (set! (.-enableAllDbs js/PouchDB) flag))


(defn all-dbs
  "Get all databases"
  [cb]
  (PouchDB/allDbs cb))
