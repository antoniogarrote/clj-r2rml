(ns clj-r2rml.sparql-parser
  (:import [org.mozilla.javascript Context])
  (:use [clojure.contrib.json :only [read-json]]))

(defn read-script
  ([] (slurp "./sparql_parser.js"))
  ([file-path] (slurp file-path)))


(def *ctx* (Context/enter))
(def *scope* (let [scope (.initStandardObjects *ctx*)]
               (.evaluateString *ctx* scope (read-script) "sparql-parser.js" 1 nil)
               scope))


(defn parse-sparql [query]
  (let [jsfn (.get *scope* "sparql_query" *scope*)
        args (make-array String 1)]
    (aset args 0 query)
    (let [result (.call jsfn *ctx* *scope* *scope* args)]
      (println (Context/toString result))
      (read-json result))))

