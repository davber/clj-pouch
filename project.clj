(defproject clj-pouchdb "0.0.1"
  :description "Wrapper for PouchDB in ClojureScript"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1859"]
                 [com.cemerick/piggieback "0.0.5"]
                 [org.clojure/clojure "1.5.1"]]
  :plugins [[lein-cljsbuild "0.3.2"]]
  :hooks [leiningen.cljsbuild]
  :source-paths ["src" "js" "core.async/src/main/clojure"]
  :test-paths ["test" "src-cljs"]
  :profiles {:dev {:dependencies [[com.cemerick/clojurescript.test "0.0.4"]]}
             :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}
  :cljsbuild {:repl-listen-port 9191
              :builds [{:id "test"
                        :source-paths ["src-cljs" "test" "core.async/src/test/cljs" "core.async/src/main/clojure/cljs"]
                        :compiler {:output-to "target/js/test.js"
                                   :optimizations :whitespace
                                   :pretty-print true
                                   :foreign-libs [{:file "pouchdb/dist/pouchdb-nightly.js" :provides ["nat.pouchdb"]}]}}]
              :test-commands {"unit-tests" ["runners/runphantom.js" "target/js/test.js"]}})
