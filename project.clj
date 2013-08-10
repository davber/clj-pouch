(defproject clj-pouchdb "0.0.1"
  :plugins [[lein-cljsbuild "0.3.2"]]
  :cljsbuild {:builds [{
                        :source-paths ["src-cljsp"]
                        :compiler {:output-to "target/js/clj-couch.js"
                                   :optimizations :whitespace
                                   :pretty-print true
                                   :libs ["js/"]}}]})
