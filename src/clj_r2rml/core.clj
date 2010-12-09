(ns clj-r2rml.core
  (:use clojure.contrib.sql)
  (:use clojure.contrib.sql.internal))

;; types

(defrecord TermMapper [property-mapper object-mapper object-type-mapper language-mapper datatype-mapper graph-mapper])
(defrecord ForeignKeyMap [key parent-triples-map join-condition])
(defrecord TriplesMap [logical-table subject-map property-object-map class table-graph-iri row-graph foreign-key-map])
;; Contains the information that will be used at runtime to map the relational data
(defrecord MappingExecutionContext [connection results namespaces blank-id])
;; A RDF Literal
(defrecord Literal [value datatype language])
;; A result quad
(defrecord Quad [subject predicate object graph])
(defrecord TableForeignKey [local name table pos col])

;; property mappers

(defn- property-term-mapper-comp
  "Maps a tuple property to a fixed value"
  ([property] (fn [tuple-map context] property)))

(defn- property-column-term-mapper-comp
  "Maps a tuple to the value of a column"
  ([property-column] (fn [tuple-map context] ((keyword property-column) tuple-map))))

;; object mappers

(defn- column-term-map-mapper-comp
  "Maps an object to the value of a column"
  ([column] (fn [tuple-map context]  ((keyword column) tuple-map))))

(defn- constant-value-term-map-mapper-comp
  "Maps an object to a fixed value"
  ([constant-value] (fn [tuple-map context] constant-value)))

;; datatype mappers

(defn- datatype-term-mapper-comp
  "Maps the datatype of an object to a fixed value"
  ([datatype] (fn [tuple-map context] datatype)))

;; language mappers

(defn- language-term-mapper-comp
  "Maps the language of an object to a fixed value"
  ([language] (fn [tuple-map context] language)))

;; graph mappers

(defn- column-graph-iri-term-mapper-comp
  "Maps the graph for a tuple to a fixed value"
  ([iri] (fn [tuple-map context] iri)))

(defn- column-graph-term-mapper-comp
  "Maps the graph for a tuple to the value of a column"
  ([column] (fn [tuple-map context] ((keyword column) tuple-map))))


;; Auxiliary functions for building the term mapper

(defn- choose-term-mapper
  "Chooses the right mapper for a piece of the specification among some posibilities"
  ([mappers specs]
     (let [common-key (first (clojure.set/intersection (set (keys specs))
                                                       (set (keys mappers))))]
       (if (nil? common-key) nil
           (let [spec-value (common-key specs)
                 mapper (common-key mappers)]
             (apply mapper [spec-value]))))))

;; A special term mapper for the rdf-type-property property

(defn- rdf-type-property-mapper
  "A special term mapper for the rdf-type-property property"
  ([object-mapper] (fn [tuple-map] (object-mapper tuple-map))))


(defn- build-term-map
  "Builds a TermMapper as a collection of mappers for the provided specification:

    :property          RDF property component of the RDFTermMap instance

    :column            Object value component of the RDFTermMap instance.
                       Specifically, this property must be a column name in the logical table

    :property-column   Used when the property name is not a constant and instead comes from the values
                       in a column

    :datatype          (Optional property) Specifies the datatype for the object value component of the
                       RDFTermMap instance

    :language          (Optional property) Specifies the language for the object value component of the
                       RDFTermMap instance

    :column-graph-iri  Used to specify the RDF named graph for the triples that are constructed with the
                       (property, object) pair of the RDFTermMap

    :column-graph      Same as :column-graph-iri when the IRI is stored in a column of the logical table

    :constant-value    Used in place of the rr:column property to specify a constant value for the object
                       component .

    :rdf-type-property Further indicates that any object value for this property is also the value of the
                       rdf:type property
"
  ([spec]
     (let [property-mappers {:property property-term-mapper-comp
                             :property-column property-column-term-mapper-comp}
           object-mappers   {:column column-term-map-mapper-comp
                             :constant-value constant-value-term-map-mapper-comp}
           language-mappers {:language language-term-mapper-comp}
           datatype-mappers {:datatype datatype-term-mapper-comp}
           graph-mappers    {:column-graph column-graph-term-mapper-comp
                             :column-graph-iri column-graph-iri-term-mapper-comp}
           val (TermMapper. (choose-term-mapper property-mappers spec)
                            (choose-term-mapper object-mappers spec)
                            (if (nil? (:rdf-type-property spec))
                              nil
                              (rdf-type-property-mapper (choose-term-mapper object-mappers spec)))
                            (choose-term-mapper language-mappers spec)
                            (choose-term-mapper datatype-mappers spec)
                            (choose-term-mapper graph-mappers spec))]
       (TermMapper. (choose-term-mapper property-mappers spec)
                    (choose-term-mapper object-mappers spec)
                    (if (nil? (:rdf-type-property spec))
                      nil
                      (rdf-type-property-mapper (choose-term-mapper object-mappers spec)))
                    (choose-term-mapper language-mappers spec)
                    (choose-term-mapper datatype-mappers spec)
                    (choose-term-mapper graph-mappers spec)))))

;;; Foreign keys

(defn- build-foreign-key-map
  "Builds a ForeignKeyMap from the provided specification.

    :key                   Specifies the constraint component of the ForeignKey instance.

    :parent-triples-map    Specifies the TriplesMap corresponding to the parent table component, of the ForeignKey
                           instance.

    :join-condition        Specifies the join condition of the ForeignKey instance.
"
  ([opts] (ForeignKeyMap. (:key opts) (:parent-triples-map opts) (:join-condition opts))))


(defn build-triples-map
  "Builds a triple map from a provided specification:

    :logical-table         Specifies the logical table (i.e., a valid SQL query or a table or view name plus
                           its owner name) whose rows are mapped to RDF triples by this TriplesMap instance.

    :subject-map           Specifies the mapping to obtain the IRI or blank node that is used as the subject
                           of all the RDF triples generated from one row of the table.

    :property-object-map   Specifies the mapping to obtain the (property, object) pair for each RDF triple
                           corresponding to a column and its values in the table.

    :class                 Specifies the RDFS class associated with a TriplesMap instance.

    :table-graph-iri       Specifies the graph IRI that would contain all the RDF triples in a TriplesMap
                           instance.

    :row-graph             Sames table-graph-iri but for row using a column of the table.

    :foreign-key-map       Specifies the mapping to obtain the property, and a join condition that can be used
                           to retrieve the object
"
  ([opts]
     (TriplesMap. (:logical-table opts)
                  (build-term-map (:subject-map opts))
                  (map build-term-map (:property-object-map opts))
                  (:class opts)
                  (:table-graph-iri opts)
                  (:row-graph opts)
                  (build-foreign-key-map (:foreign-key-map opts)))))

;; Executes a query withing a persistent connection stored in the execution
;; context for the mapping
(defmacro with-context-connection
  ([context & body]
     `(let [conn# (:connection ~context)]
        (binding [clojure.contrib.sql.internal/*db* (assoc clojure.contrib.sql.internal/*db*
                                                      :connection conn# :level 0 :rollback (atom false))]
          ~@body))))


(defn- run-subject-triple-mapping
  "Runs a mapping for a subject"
  ([subject-mapper tuple-map context]
     ((:object-mapper subject-mapper) tuple-map context)))


(defn- check-ns
  "Checks if a string is a CURIE for one of the known namespaces"
  ([maybe-literal namespaces]
     (reduce (fn [ac [prefix url]]
               (or ac
                   (if (= 0 (.indexOf maybe-literal (str prefix ":")))
                     true
                     false)))
             false namespaces)))

(defn check-literal
  "Checks if a provided string is literal, URI or CURIE"
  ([maybe-literal context]
     (if (= (.indexOf maybe-literal "<http") 0)
       maybe-literal
       (if (check-ns maybe-literal (:namespaces context))
         maybe-literal
         (Literal. maybe-literal nil nil)))))

(defn- build-quad
  "Builds a new quad for the provided context"
  ([subject prop-obj-map tuple-map default-graph context]
     (let [property ((:property-mapper prop-obj-map) tuple-map context)
           object   ((:object-mapper prop-obj-map) tuple-map context)
           language (if (nil? (:language-mapper prop-obj-map))
                      nil ((:language-mapper prop-obj-map) tuple-map context))
           datatype (if (nil? (:datatype-mapper prop-obj-map))
                      nil ((:datatype-mapper prop-obj-map) tuple-map context))
           graph    (if (nil? (:graph-mapper prop-obj-map))
                      default-graph ((:graph-mapper prop-obj-map) tuple-map context))]
       (Quad. subject  property (if (and (nil? datatype) (nil? language))
                                  (check-literal object context)
                                  (Literal. object datatype language))
              graph))))

(defn normalize-results
  "Transforms into strings the objects returned by the DB"
  ([tuples-map]
     (map (fn [tuple]
            (reduce (fn [ac k] (assoc ac k (try (String. (get tuple k))
                                               (catch Exception ex
                                                 (str (get tuple k))))))
                    {} (keys tuple)))
          tuples-map)))

(defn- run-table-mapping
  "Runs the mapping for a single table in a provided execution context"
  ([triples-map context]
     (let [results (:results context)
           {logical-table   :logical-table
            subject-map     :subject-map
            property-object-map :property-object-map
            class           :class
            table-graph-iri :table-graph-iri
            row-graph       :row-graph
            foreign-key-map :foreign-key-map} triples-map
           tuples-map (normalize-results
                       (with-context-connection context
                         (with-query-results rs [logical-table] (vec rs))))
           quads-table (map (fn [tuple-map]
                              (let [graph-iri (if (nil? row-graph) table-graph-iri
                                                  (get tuple-map (keyword row-graph)))
                                    subject (run-subject-triple-mapping (build-term-map subject-map) tuple-map context)]
                                (map (fn [prop-obj-map]
                                       (build-quad subject (build-term-map prop-obj-map) tuple-map graph-iri context))
                                     property-object-map)))
                            tuples-map)
           quads-table (if (nil? class) quads-table
                           (concat quads-table (map (fn [tuple-map]
                                                      (let [graph-iri (if (nil? row-graph) table-graph-iri
                                                                          (get tuple-map (keyword row-graph)))
                                                            subject (run-subject-triple-mapping (build-term-map subject-map) tuple-map context)]
                                                        (Quad. subject "rdf:type" class graph-iri)))
                                                    tuples-map)))]
       (assoc context :results (concat results (flatten quads-table))))))

;; Default namespaces
(def *default-ns*
     {"rdf"	"http://www.w3.org/1999/02/22-rdf-syntax-ns#"
      "rdfs"	"http://www.w3.org/2000/01/rdf-schema#"
      "xsd"	"http://www.w3.org/2001/XMLSchema#"})



(defn- literal-to-str
  "String representation of a literal"
  ([literal]
     (if (and (nil? (:language literal)) (nil? (:datatype literal)))
       (str "\"" (:value literal) "\"")
       (if (nil? (:language literal))
         (if (= 0 (.indexOf (:datatype literal) "http"))
           (str "\"" (:value literal) "\"^^<" (:datatype literal)">")
           (str "\"" (:value literal) "\"^^" (:datatype literal)))
         (str "\"" (:value literal) "\"@" (:language literal))))))

(defn- to-turtle
  "Serializes a set of quads using the Turtle syntax"
  ([ns triples]
     (let [output (StringBuilder.)]
       (doseq [[pref uri] ns]
         (.append output (str "@prefix " pref ": " uri " . ")))
       (doseq [triple triples]
         (let [s (:subject triple)
               p (:predicate triple)
               o (if (instance? clj-r2rml.core.Literal (:object triple))
                   (literal-to-str (:object triple))
                   (:object triple))]
           (if (= 0 (.indexOf s "http:"))
             (.append output (str "<" s "> "))
             (.append output (str s " ")))
           (if (= 0 (.indexOf p "http:"))
             (.append output (str "<" p "> "))
             (.append output (str p " ")))
           (if (= 0 (.indexOf o "http:"))
             (.append output (str "<" o "> "))
             (.append output (str o ".\r\n")))))
       (str output))))

;; Public interface

(defn make-context
  ([db-spec namespaces]
     (MappingExecutionContext. (get-connection db-spec)
                               []
                               (merge namespaces *default-ns*)
                               0)))
(defn run-mapping
  "Transforms a mapping into a RDF dataset using the provided connection and set of namespaces"
  ([mapping-spec context namespaces]
     (reduce (fn [context triples-map]
               (run-table-mapping triples-map context))
             context
             mapping-spec)))

(defn to-rdf
  "Serializes a set of quads into a certain RDF syntax"
  ([ns solution format]
     (let [ns (merge ns *default-ns*)]
       (condp = format
           "ttl" (to-turtle ns solution))))
  ([ns solution] (to-rdf ns solution "ttl")))


;;; Direct Mapping

(defn build-rows-description
  "Builds a map describin the table to be mapped"
  ([context schema table-name]
     (with-context-connection context
       (with-query-results rs [(str "describe " schema "." table-name)] (vec rs))))
  ([context table-name]
     (with-context-connection context
       (with-query-results rs [(str "describe " table-name)] (vec rs)))))



(defn get-primary-keys
  ([rows-description]
     (reduce (fn [ac r] (if (= "PRI" (:key r))
                         (conj ac (:field r))
                         ac))
             []
             rows-description)))

(defn- foreign-key-constraint?
  ([c] (and (not= "PRIMARY" (:constraint_name c))
            (not (nil? (:referenced_table_name c))))))

(defn- add-foreign-key-constraint
  ([acum c]
     (let [local (:column_name c)
           name (:constraint_name c)
           table (:referenced_table_name c)
           pos (:ordinal_position c)
           col (:referenced_column_name c)
           old-value (get acum name)
           new-key (TableForeignKey. local name table pos col)]
       (if (nil? old-value)
         (assoc acum name new-key)
         (if (map? old-value)
           (assoc acum name [old-value new-key])
           (assoc acum name (conj old-value new-key)))))))

(defn- collect-foreign-keys
  ([constraints acum]
     (if (empty? constraints) acum
         (let [constraint (first constraints)]
           (if (foreign-key-constraint? constraint)
             (recur (rest constraints) (add-foreign-key-constraint acum constraint))
             (recur (rest constraints) acum))))))

(defn get-foreign-keys
  "Retrieve the foreign keys for a INNODB MYSQL table"
  ([context table-name]
     (let [query (str "select * from information_schema.key_column_usage where TABLE_NAME='" table-name  "'")
           description (with-context-connection context
                         (with-query-results rs [query] (vec rs)))]
       (collect-foreign-keys description {}))))


(defn table-iri
  "The IRI that identifies a table is created by concatenating the base IRI with the table name"
  ([table-name base-iri] (str base-iri "/" table-name)))


(defn within-commas
  "Wraps a list of elements between commas"
  ([elements]
     (let [commas (drop 1 (repeat (count elements) ","))]
       (reduce str (concat (interleave elements commas) (list (last elements)))))))

(defn column-iri
  "Creates a single column / multi column iri provided the table iri"
  ([table-iri columns]
     (let [commas (drop 1 (repeat (count columns) ","))
           columns-part (reduce str (concat (interleave columns commas) (list (last columns))))]
       (str table-iri "#" columns-part))))

;;; Mapping Rules

(defn- make-key-iri-generator
  ([primary-keys table-iri]
     (fn [tuple-map context]
       (let [keys-vals (map (fn [c] (str c "=" (get tuple-map (keyword (.toLowerCase c))))) primary-keys)]
         (assoc context :current-row-subject (str table-iri "/" (within-commas keys-vals) "#_"))))))

(defn build-shared-subject-generator
  "Returns a function capable of generating the IRI that identifies a row"
  ([table-iri rows-description]
     (let [primary-keys (get-primary-keys rows-description)]
       (if (empty? primary-keys)
         (fn [tuple-map context]
           (let [next-blank-id (:blank-id context)
                 next-blank-uri (str "_:" next-blank-id)]
             (-> context
                 (assoc :current-row-subject next-blank-uri)
                 (assoc :blank-id (inc next-blank-id)))))
         (make-key-iri-generator primary-keys table-iri)))))

(defn build-table-triples-generator
  "Returns a function capable of generating triple about the table for each row in the table."
  ([table-iri]
     (fn [tuple-map context]
       (let [current-row-subject (:current-row-subject context)
             predicate "rdf:type"
             object table-iri
             results (:results context)]
         (assoc context :results (conj results (Quad. current-row-subject predicate object nil)))))))

(defn starts-with
  ([string prefix]
     (= 0 (.indexOf string prefix))))

(defn map-sql-to-xml-dataype
  "A mapping of SQL types according to http://www.w3.org/TR/2010/WD-rdb-direct-mapping-20101118/#XMLdt"
  ([sqldatatype]
     (cond
      (starts-with sqldatatype "int") "http://www.w3.org/TR/xmlschema-2/#integer"
      (starts-with sqldatatype "float") "http://www.w3.org/TR/xmlschema-2/#float"
      (starts-with sqldatatype "date") "http://www.w3.org/TR/xmlschema-2/#date"
      (starts-with sqldatatype "time") "http://www.w3.org/TR/xmlschema-2/#time"
      (starts-with sqldatatype "timestamp") "http://www.w3.org/TR/xmlschema-2/#dateTime"
      (starts-with sqldatatype "char")    :plain-literal
      (starts-with sqldatatype "varchar") :plain-literal
      (starts-with sqldatatype "string")  :plain-literal
      :else (let [type (aget (.split sqldatatype "\\(") 0)]
              (str "http://github.com/antoniogarrote/clj-r2rml/sqltypes#" type)))))

(defn build-literal-column-generator
  "Returns a function capable of generating a triple for a column value in a tuple row"
  ([table-iri row-column]
     (let [type-url (map-sql-to-xml-dataype (:type row-column))]
       (fn [tuple-map context]
         (let [subject (:current-row-subject context)
               value (get tuple-map (keyword (.toLowerCase (:field row-column))))]
           (if (nil? value) nil
               (if (= :plain-literal type-url)
                 (Quad. subject (column-iri table-iri [(:field row-column)])
                        (Literal. value
                                  nil
                                  nil)
                        nil)
                 (Quad. subject (column-iri table-iri [(:field row-column)])
                        (Literal. value
                                  type-url
                                  nil)
                        nil))))))))


(defn build-literal-triples-generator
  "Returns a function capable of generating the triples for a row columns in the table"
  ([table-iri rows-description]
     (let [columns-literal-parsers (map (partial build-literal-column-generator table-iri) rows-description)]
       (fn [tuple-map context]
         (let [triples-and-nils (map (fn [parser] (parser tuple-map context)) columns-literal-parsers)
               triples (filter (comp not nil?) triples-and-nils)]
           (assoc context :results (concat triples (:results context))))))))

(defn build-foreign-key-triple-generator
  ([base-iri table-iri-val foreign-key]
     (if (map? foreign-key)
       ;; Single column foreign key
       (let [local (:local foreign-key)
             local-uri (column-iri table-iri-val [local])
             referenced-table-iri (table-iri (:table foreign-key) base-iri)
             referenced-object-generator (fn [tuple-map] (let [obj (get tuple-map (keyword (.toLowerCase local)))]
                                                          (str referenced-table-iri
                                                               "/"
                                                               (within-commas [(str (:col foreign-key) "=" obj)])
                                                               "#_")))]
         (fn [tuple-map context]
           (let [subject (:current-row-subject context)]
             (if (nil? (get tuple-map (keyword (.toLowerCase local)))) nil?
                 (let [object (referenced-object-generator  tuple-map)]
                   (Quad. subject local-uri object nil))))))
       ;; multi column foreign key
       (let [locals (map :local foreign-key)
             local-uri (column-iri table-iri-val locals)
             referenced-table-iri (table-iri (:table (first foreign-key)) base-iri)
             referenced-object-generator (fn [tuple-map] (let [objs (map (fn [local] (str local "=" (get tuple-map (keyword (.toLowerCase local))))) locals)]
                                                          (str referenced-table-iri
                                                               "/"
                                                               (within-commas objs)
                                                               "#_")))]
         (fn [tuple-map context]
           (let [subject (:current-row-subject context)]
             (if (some nil? (map (fn [l] (get tuple-map (keyword (.toLowerCase l)))) locals))
               nil
               (let [object (referenced-object-generator tuple-map)]
                 (Quad. subject local-uri object nil)))))))))

(defn build-reference-triples-generator
  "Returns a function capable of generating the triples referencing other subjects in different tables"
  ([base-iri table-iri foreign-keys]
     (let [foreign-key-generators (map (fn [foreign-key] (build-foreign-key-triple-generator base-iri table-iri foreign-key)) (vals foreign-keys))]
       (fn [tuple-map context]
         (let [results (map (fn [g] (g tuple-map context)) foreign-key-generators)]
           (assoc context :results (concat (filter (comp not nil?) results)
                                           (:results context))))))))

(defn build-triples-for-tuple-maps
  ([tuple-maps empty-context shared-subject-generator table-triples-generator literal-triples-generator referenced-triples-generator]
     (reduce (fn [context tuple-map]
               (->> context
                    (shared-subject-generator tuple-map)
                    (table-triples-generator tuple-map)
                    (literal-triples-generator tuple-map)
                    (referenced-triples-generator tuple-map)))
             empty-context
             tuple-maps)))

(defn purge-foreign-key-columns
  "Remove foreign keys from the row descriptions"
  ([rows-description foreign-keys]
     (let [fk-column-names (map (fn [fk-desc] (:local fk-desc)) (flatten (vals foreign-keys)))]
       (filter (fn [row] (not (some (fn [fk-column-name] (= (:field row) fk-column-name)) fk-column-names)))
               rows-description))))

(defn default-mapping-triples
  ([base-iri table-queries-map context]
     (reduce (fn [results-ac [table query]]
               (let [rows-description (build-rows-description context table)
                     foreign-keys (get-foreign-keys context table)
                     rows-description (purge-foreign-key-columns rows-description foreign-keys)
                     tuple-maps (normalize-results
                                 (with-context-connection context
                                   (with-query-results rs [query] (vec rs))))
                     empty-context (assoc context :results [])
                     table-iri-val (table-iri table base-iri)

                     ;; generators
                     shared-subject-generator (build-shared-subject-generator table-iri-val rows-description)
                     table-triples-generator  (build-table-triples-generator table-iri-val)
                     literal-triples-generator (build-literal-triples-generator table-iri-val rows-description)
                     referenced-triples-generator (build-reference-triples-generator base-iri table-iri-val foreign-keys)

                     ;; run generators and get triples
                     result-context (build-triples-for-tuple-maps tuple-maps
                                                                  empty-context
                                                                  shared-subject-generator
                                                                  table-triples-generator
                                                                  literal-triples-generator
                                                                  referenced-triples-generator)]
                 (concat results-ac (:results context) (:results result-context))))
             []
             table-queries-map)))
