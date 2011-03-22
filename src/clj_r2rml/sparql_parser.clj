(ns clj-r2rml.sparql-parser
  (:import [org.mozilla.javascript Context])
  (:use [clojure.contrib.json :only [read-json]]))

(defn read-script
  ([] (slurp "./sparql_parser.js"))
  ([file-path] (slurp file-path)))


(def *scope* (let [*ctx* (Context/enter)
                   scope (.initStandardObjects *ctx*)]
               (.evaluateString *ctx* scope (read-script) "sparql-parser.js" 1 nil)
               (Context/exit)
               scope))


(defn parse-sparql [query]
  (let [*ctx* (Context/enter)
        jsfn (.get *scope* "sparql_query" *scope*)
        args (make-array String 1)]
    (aset args 0 query)
    (let [result (.call jsfn *ctx* *scope* *scope* args)]
      (Context/exit)
      (read-json result))))

