(ns clj-r2rml.sql
  (:use clj-r2rml.core)
  (:use clojure.contrib.sql))


(defn- fixed-sql-rename-generator
  ([val name] (str " '" val "' AS " name)))

(defn- variable-sql-rename-generator
  ([column name] (str " " column " AS " name)))

(defn- subject-sql-generator
  ([term-mapper]
     (if (nil? (:column term-mapper))
       (if (nil? (:constant-value term-mapper))
         (throw (Exception. "Cannot find a way to build the subject of the triple"))
         (fixed-sql-rename-generator (:constant-value term-mapper) "subject"))
       (variable-sql-rename-generator (:column term-mapper) "subject"))))

(defn- property-sql-generator
  ([term-mapper]
     (if (nil? (:property-column term-mapper))
       (if (nil? (:property term-mapper))
         (throw (Exception. "Cannot find a way to build the object of the triple"))
         (fixed-sql-rename-generator (:property term-mapper) "predicate"))
       (variable-sql-rename-generator (:property-column term-mapper) "predicate"))))

(defn- object-sql-generator
  ([term-mapper]
     (if (nil? (:column term-mapper))
       (if (nil? (:constant-value term-mapper))
         (throw (Exception. "Cannot find a way to build the object of the triple"))
         (fixed-sql-rename-generator (:constant-value term-mapper) "object"))
       (variable-sql-rename-generator (:column term-mapper) "object"))))

(defn- property-object-fragments-generator
  ([property-object-maps]
     (map (fn [prop-obj-map]
            (let [property (property-sql-generator prop-obj-map)
                  object (object-sql-generator prop-obj-map)]
              {:property property
               :object object})) property-object-maps)))

(defn- graph-sql-generator
  ([spec-map]
     (cond
      ((comp not nil?) (:table-graph-iri spec-map))  (fixed-sql-rename-generator (:table-graph-iri spec-map) "graph")
      ((comp not nil?) (:row-graph spec-map))        (variable-sql-rename-generator (:row-graph spec-map) "graph")
      ((comp not nil?) (:column-graph-iri spec-map)) (variable-sql-rename-generator (:column-graph-iri) "graph")
      :else nil)))

(defn- build-sql-triple-map-fragment
  ([triples-map counter]
     (let [logical-table (:logical-table triples-map)
           subject-sql-fragment (subject-sql-generator (:subject-map triples-map))
           property-object-fragments (property-object-fragments-generator (:property-object-map triples-map))
           graph-sql-fragment (graph-sql-generator triples-map)]
       (reduce (fn [sql prop-obj]
                 (let [property-sql-fragment (:property prop-obj)
                       object-sql-fragment (:object prop-obj)
                       graph-sql-fragment-row (if (nil? (graph-sql-generator prop-obj)) graph-sql-fragment (graph-sql-generator prop-obj))
                       sql-fragment (str "SELECT " subject-sql-fragment ", " property-sql-fragment ", " object-sql-fragment
                                         ", " graph-sql-fragment-row
                                         " FROM (" logical-table ") AS TBL" counter)]
                   (if (= sql "")
                     (str sql sql-fragment)
                     (str sql " UNION " sql-fragment))))
               ""
               property-object-fragments))))

(defn build-sql-query
  "Generates a SQL query capable of transforming a triple map into a relation with | subject | predicate | object | graph |"
  ([triples-map]
     (let [sql-fragments (map (fn [i] (build-sql-triple-map-fragment (nth triples-map i) i)) (range 0 (count triples-map)))]
       (reduce (fn [sql fragment]
                 (if (= sql "")
                   fragment
                   (str " UNION " fragment)))
               "" sql-fragments))))

(defn run-sql-query
  ([sql-query context namespaces]
     (let [results (normalize-results
                    (with-context-connection context
                      (with-query-results rs [sql-query] (vec rs))))]
       (map (fn [result] (clj-r2rml.core.Quad. (:subject result)
                               (:predicate result)
                               (clj-r2rml.core.Literal. (:object result) nil nil)
                               (:graph result)))
            results))))

(defn run-sql-mapping
  "Executes a SQL query built with the build-sql-query function an returns a collection of Quad objects"
  ([mapping-spec context namespaces]
     (run-sql-query (build-sql-query mapping-spec) context namespaces)))
