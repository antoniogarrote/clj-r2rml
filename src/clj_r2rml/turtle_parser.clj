(ns clj-r2rml.turtle-parser
  (:use clj-r2rml.triples)
  (:import [org.mozilla.javascript Context])
  (:use [clojure.contrib.json :only [read-json]]))

(defn- read-script
  ([] (slurp "./turtle_parser.js"))
  ([file-path] (slurp file-path)))


(def *scope-turtle* (let [*ctx* (Context/enter)
                   scope (.initStandardObjects *ctx*)]
               (.evaluateString *ctx* scope (read-script) "turtle-parser.js" 1 nil)
               (Context/exit)
               scope))


(defn parse-turtle [doc]
  (let [*ctx* (Context/enter)
        jsfn (.get *scope-turtle* "turtle_doc" *scope-turtle*)
        args (make-array String 1)]
    (aset args 0 doc)
    (let [result (.call jsfn *ctx* *scope-turtle* *scope-turtle* args)]
      (Context/exit)
      (read-json result))))


(defn insert-triples
  ([triples env acum]
     (let [new-triples (map (fn [t] (normalize-triple-obj t @env))triples)]
       (swap! acum #(concat % new-triples)))))

(defn turtle-doc-to-triples
  ([doc]
     (let [parsed-doc (parse-turtle doc)
           env (atom (make-empty-ns-env))
           quads (atom [])]
       (doseq [token parsed-doc]
         (condp = (:token token)
             "base" (swap! env #(update-base-env % (:value token)))
             "prefix" (swap! env #(add-prefix-env % (:prefix token) (:local token)))
             "triples" (insert-triples (:triplesContext token) env quads)
             (throw (Exception. (str "Unknown token " (:token token))))))
       @quads)))
