(ns clj-r2rml.sparql-engine
    (:use clj-r2rml.core)
  (:use clj-r2rml.sparql-parser)
  (:use clj-r2rml.sparql-update)
  (:use clojure.contrib.sql)
  (:use [clojure.contrib.json :only [read-json]]))

(defprotocol SparqlEngine
  "An interface to execute SPARQL engines against a certain backend"
  (execute [this query]))

;; common utility functions

(defn blank?
  ([x] (or (nil? x) (= x ""))))

(defn make-ns-env
  ([parsed-query]
     (let [prologue (:prologue parsed-query)
           default (if (= (prologue :base) "")
                     nil (-> prologue :base :value))
           prefixes (reduce (fn [m token] (assoc m (:prefix token) (:local token))) {} (:prefixes prologue))]
       {:ns {:default default :prefixes prefixes}})))

(defn check-prefix
  ([prefix env]
     (let [val (get (-> env :ns :prefixes) prefix)]
       (if (nil? val)
         (throw (Exception. (str "No registered prefix in current environment for value: " prefix)))
         val))))

(defn normalize-uri
  ([term env]
     (if (blank? (:prefix term))
       (if (blank? (:value term))
         (-> env :ns :default)
         (:value term))
       (let [prefix-val (check-prefix (:prefix term) env)]
         (str prefix-val (:suffix term))))))

(defn normalize-term
  ([term env]
     (condp = (:token term)
         "uri" (normalize-uri term env)
         "literal" (:value term)
         "var" (keyword (:value term))
         (throw (Exception. "Unknown URI component " term)))))

(defn normalize-triple
  ([token env]
     (println (str "NORMALIZING TRIPLE FOR: " token))
     [(normalize-term (:subject token) env)
      (normalize-term (:predicate token) env)
      (normalize-term (:object token) env)]))

;; Select query impl.

(defn build-abstract-query-tree
  ([pattern env]
     (condp = (:token pattern)
         "groupgraphpattern" (if (= 1 (count (:patterns pattern)))
                               (build-abstract-query-tree(first (:patterns pattern)) env)
                               (apply AND (map #(build-abstract-query-tree % env) (:patterns pattern))))
         "basicgraphpattern" (if (= 1 (count (:triplesContext pattern)))
                               (dsl-triple (normalize-triple (first (:triplesContext pattern)) env))
                               (apply AND (map #(dsl-triple (normalize-triple % env)) (:triplesContext pattern))))
         (if (nil? (:predicate pattern))
           (throw (Exception. (str "Token " (:token pattern) " not supported yet in the Abstract Syntax Tree")))
           (dsl-triple (normalize-triple pattern env))))))

(defn project-results
  ([projection results-map]
     (if (and (= 1 (count projection)) (= "*" (:kind (first projection))))
       results-map
       (let [vars (reverse (map (fn [var] (println var) (keyword (:value (:value var)))) projection))]
         (map (fn [result] (reduce (fn [tuple v] (assoc tuple v (get result v))) {} vars)) results-map)))))

(defn execute-query-query
  ([parsed-query sql-context table-mappers]
     (let [env (make-ns-env parsed-query)
           pattern (:pattern (first (:units parsed-query)))
           aqt (build-abstract-query-tree pattern env)
           sql (translate (aqt table-mappers))]
       (println sql)
       (if (nil? sql)
         (throw (Exception. "The query cannot be executed"))
         (let [result (with-context-connection sql-context
                        (with-query-results rs [sql] (vec rs)))
               projection (:projection (first (:units parsed-query)))]
           (project-results projection result))))))

;; Update query impl.


(defn update-unit-to-triples
  ([update-unit env]
     (let [quads (:quads update-unit)]
       (map (fn [token] (normalize-triple token env))
            quads))))

(defn update-query-to-triples
  ([parsed-query env]
     (apply concat (map (fn [unit] (update-unit-to-triples unit env))
                        (:units parsed-query)))))

(defn execute-update-query
  ([parsed-query sql-context table-mappers]
     (let [env (make-ns-env parsed-query)
           triples (update-query-to-triples parsed-query env)
           sql-query (translate-insert triples table-mappers)]
       (if (nil? sql-query)
         (throw (Exception. "The update query cannot be executed"))
         (with-context-connection sql-context
           (do-commands sql-query))))))

;;
;; Engine protocol implementation
;;

(defrecord SqlSparqlEngine [sql-backend table-mappers] SparqlEngine
  (execute [this query]
           (let [parsed-query (parse-sparql query)]
             (condp = (:kind parsed-query)
                 "update" (execute-update-query parsed-query sql-backend table-mappers)
                 "query"  (execute-query-query parsed-query sql-backend table-mappers)))))


(defn make-sql-sparql-engine
  ([db-configuration table-mappers]
     (let [context (make-context db-configuration {})]
       (clj-r2rml.sparql-engine.SqlSparqlEngine. context table-mappers))))
