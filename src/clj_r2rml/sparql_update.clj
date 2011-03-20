(ns clj-r2rml.sparql-update
  (:import (java.util UUID))
  (:use clj-r2rml.core)
  (:use clj-r2rml.sql)
  (:use [clojure.pprint]))

(def *test-mapping*
     [{:logical-table "Blogs"
       :class         "test:Blog"
       :table-graph-iri "test:TestGraph"
       :subject-map     {:column "id"}
       :property-object-map [{:property "test:title"
                              :column   "title"
                              :datatype "xsd:String"}]}
      {:logical-table "Posts"
       :class         "test:Post"
       :table-graph-iri "test:TestGraph"
       :subject-map     {:column "id"}
       :property-object-map [{:property "test:title"
                              :column   "title"
                              :datatype "xsd:String"}
                             {:property "test:content"
                              :column   "body"
                              :datatype "xsd:String"}
                             {:property "test:inBlog"
                              :column   "blog_id"}]}])


(defn group-triples
  ([triples] (sort triples)))

;; data types
(defrecord UpdateTermMapper [position kind value])
(defrecord UpdateTripleMapper [table-name subject property object graph])
(defrecord UpdateTableMapper [table-name triple-mappers])

(defn triple-mapper-to-list
  ([triple-mapper]
     [(:subject triple-mapper)
      (:property triple-mapper)
      (:object triple-mapper)
      (:graph triple-mapper)]))

;; @todo Term mappers are lacking support for the datatype
(defn make-object-term-mapper
  ([position mapping] (if (:column mapping)
                        (UpdateTermMapper. position :variable (:column mapping))
                        (if (:constant-value mapping)
                          (UpdateTermMapper. position :constant (:constant-value mapping))
                          (throw (Exception. ("Unknown values for subjectMap " mapping)))))))

;; @todo Term mappers are lacking support for the datatype
(defn make-property-term-mapper
  ([mapping] (if (:property mapping)
               (UpdateTermMapper. :property :constant (:property mapping))
               (if (:property-column mapping)
                 (UpdateTermMapper. :property :variable (:property-column mapping))
                 (throw (Exception. ("Unknown values for propertyObjectMap " mapping)))))))

;; @todo Term mappers are lacking support for the datatype
;; @todo add more possible values in the mapping for the graph
(defn make-graph-term-mapper
  ([triples-mapping term-mapping]
     (if (:table-graph-iri triples-mapping)
       (UpdateTermMapper. :graph :constant (:table-graph-iri triples-mapping))
       (if (:column-graph triples-mapping)
         (UpdateTermMapper. :variable :variable (:column-graph triples-mapping))
         (if (:column-graph-iri triples-mapping)
           (UpdateTermMapper. :graph :constant (:column-graph-iri triples-mapping))
           (UpdateTermMapper. :graph :constant nil))))))


;; @todo Additional triple mappers for class and graph must be added
(defn make-table-mapper
  ([triples-map]
     (let [table (or (:logical-table triples-map) (:table triples-map))
           subject (make-object-term-mapper :subject (:subject-map triples-map))]
       (UpdateTableMapper. table
                           (map (fn [mapping] (UpdateTripleMapper. table
                                                                  subject
                                                                  (make-property-term-mapper mapping)
                                                                  (make-object-term-mapper :object mapping)
                                                                  (make-graph-term-mapper triples-map mapping)))
                                (:property-object-map triples-map))))))

;; triple pattern manipulation
(defn subject
  ([pattern] (nth pattern 0)))

(defn property
  ([pattern] (nth pattern 1)))

(defn object
  ([pattern] (nth pattern 2)))

(defn graph
  ([pattern] (nth pattern 3)))

(defn extract-position
  ([position]
     (condp = position
         :subject subject
         :property property
         :object object
         :graph  graph
         (throw (Exception. (str "Wrong triple position " position))))))

;; row insertion context
(defrecord UpdateColumnContext [column value])
(defrecord UpdateRowContext [update-column-contexts sql-operation])
(defrecord UpdateTableContext [table-mapper update-row-contexts])
(defrecord UpdateSchemaContext [update-table-contexts])
(defrecord UpdateContext [table-mappers update-schema-contexts])


(defn make-update-column-context
  "Defines the column and value resulting of applying
   a compatible term-mapper to a triple at a certain
   position"
  ([triple position-fn term-mapper]
     (let [value (position-fn triple)
           column (:value term-mapper)]
       (UpdateColumnContext. column value))))

(defn uniq
  "Removes columns with duplicate values"
  ([update-column-contexts]
     (reduce (fn [ac ucc]
               (if (some (fn [old-ucc]
                           (println (str "checking [" (:column ucc) "," (:value ucc) "] vs [" (:column old-ucc) "," (:value old-ucc) "]"))
                           (if (and (= (:column ucc) (:column old-ucc))
                                    (= (:value ucc) (:value old-ucc)))
                             true
                             (if (= (:column ucc) (:column old-ucc))
                               (throw (Exception. (str "Conflicting value for column " (:column ucc))))
                               false)))
                          ac)
                 ac (conj ac ucc)))
             []
             update-column-contexts)))

(defn make-update-column-contexts
  "Generates the update column context from a compatible
   triple mapper and a single triple"
  ([triple compatible-triple-mapper]
     (loop [positions [:subject :property :object :graph]
            contexts []]
       (let [position (first positions)]
         (if (empty? positions)
           (uniq contexts)
           (if (= :constant (:kind (position compatible-triple-mapper)))
             (recur (rest positions)
                    contexts)
             (recur (rest positions)
                    (conj contexts
                          (make-update-column-context
                           triple
                           (extract-position position)
                           (position compatible-triple-mapper))))))))))

(defn conflicting-contexts?
  "Checks if two column context are conflicting"
  ([column-context-a column-context-b]
     (println (str "CONFLICTING? " (and (= (:column column-context-a) (:column column-context-b))
                                        (not= (:value column-context-a) (:value column-context-b)))
                   " "
                   [(:column column-context-a) (:column column-context-b)]
                   " "
                   [(:value column-context-a) (:value column-context-b)]
                   ))
     (and (= (:column column-context-a) (:column column-context-b))
          (not= (:value column-context-a) (:value column-context-b)))))

(defn can-merge?
  "a collection of update column contexts can be merged into an
   existing update row if there are no conflics in the value
   of any of the mapped columns"
  ([row-context column-contexts]
     (let [maybe-conflicting-columns (map (fn [column-context]
                                            (map (partial conflicting-contexts? column-context)
                                                 (:update-column-contexts row-context)))
                                          column-contexts)]
       (not (reduce #(or %1 %2) false (flatten maybe-conflicting-columns))))))

(defn merge-contexts
  ([row-context column-contexts]
     (let [old-column-contexts (:update-column-contexts row-context)
           new-column-contexts (reduce (fn [ac new-column-context]
                                         (if (not (nil? (some (partial = new-column-context) old-column-contexts)))
                                           ac
                                           (conj ac new-column-context)))
                                       old-column-contexts
                                       column-contexts)]
       (assoc row-context :update-column-contexts new-column-contexts))))
;;     (assoc row-context :update-column-contexts
;;            (concat (:update-column-contexts row-context)
;;                    column-contexts))))

;; compatibility functions

(defn variable-term?
  ([term-pattern] (keyword? term-pattern)))

(defn variable-mapper?
  ([term-mapper] (= :variable (:kind term-mapper))))

(defn same-value?
  ([term-pattern constant-term-mapper]
     (= (:value constant-term-mapper)
        term-pattern)))

(defn term-compatible?
  ([term-pattern term-mapper]
     (or (variable-term? term-pattern)
         (variable-mapper? term-mapper)
         (same-value? term-pattern term-mapper))))

(defn triple-compatible?
  "Checks if a triple and a triple-mapper are compatible"
  ([triple-pattern triple-mapper]
     (let [subj (subject triple-pattern)
           prop (property triple-pattern)
           obj  (object triple-pattern)
           gph  (graph triple-pattern)]
       (reduce #(and %1 %2)
               true
               [(term-compatible? subj (:subject triple-mapper))
                (term-compatible? prop (:property triple-mapper))
                (term-compatible? obj  (:object triple-mapper))
                (term-compatible? gph (:graph triple-mapper))]))))

(defn table-compatible?
  "Checks if a triple and a table mapper are compatible"
  ([triple-pattern table-mapper]
     (loop [mappers (:triple-mappers table-mapper)
            compatible-mappers []]
       (if (empty? mappers)
         compatible-mappers
         (if (triple-compatible? triple-pattern (first mappers))
           (recur (rest mappers)
                  (conj compatible-mappers (first mappers)))
           (recur (rest mappers)
                  compatible-mappers))))))


;;;;;;;;;;;;;;;;;;;;;;;;;;
;; insertion algoritmh  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn count-constant-term-mappers
  ([triple-mapper]
     (reduce (fn [ac term-mapper]
               (if (= (:kind term-mapper)
                      :constant)
                 (inc ac) ac))
             0 (triple-mapper-to-list triple-mapper))))

(defn sort-triple-mappers
  ([triple-mappers]
     (sort (fn [ma mb] (> (count-constant-term-mappers ma)
                         (count-constant-term-mappers mb)))
           triple-mappers)))

(defn get-compatible-table-mappers
  ([triple table-mappers]
     (reduce (fn [acum table-mapper]
               (let [compatible-triple-mappers (table-compatible? triple table-mapper)]
                 (if (empty? compatible-triple-mappers)
                   acum
                   (assoc acum table-mapper (sort-triple-mappers compatible-triple-mappers)))))
             {}
             table-mappers)))

(defn sort-table-mappers-map
  ([sorted-triple-mappers]
     (let [term-mappers-count (map (fn [[table-mapper sorted-triple-mappers]] [table-mapper (count-constant-term-mappers (first sorted-triple-mappers))]) sorted-triple-mappers)
           sorted-term-mappers-count (sort (fn [[tma ca] [tmb cb]] (> ca cb)) term-mappers-count)]
       ;; we return all the compatible mappings
       (map (fn [[tm c]] [tm (get sorted-triple-mappers tm)]) sorted-term-mappers-count))))

(defn compatible-table-mappers-for-triple
  ([triple table-mappers]
     (let [_ (println (str "CHECKING COMPATIBILITY OF TRIPLE: " (vec triple)))
           compatible-table-mappers (get-compatible-table-mappers triple table-mappers)
           _ (if (empty? compatible-table-mappers) (println (str " -> NO COMPATIBLE")) (println (str " -> COMPATIBLE")))]
       (sort-table-mappers-map compatible-table-mappers))))

(defn get-table-context-in-schema-context
  ([update-schema-context table-mapper]
     (some #(and (= (:table-mapper %) table-mapper) %)
           (:update-table-contexts update-schema-context))))

;; @todo this must be somewhere in clojure standard library
(defn replace-mapping
  ([coll old-value new-value]
     (reduce (fn [ac it]
               (if (= it old-value)
                 (conj ac new-value)
                 (conj ac it)))
             []
             coll)))

(defn select-update-column-and-row-contexts
  ([old-row-column-contexts update-column-contexts-arrays]
     (loop [remaining-update-column-contexts-arrays update-column-contexts-arrays]
       (if (empty? remaining-update-column-contexts-arrays)
         [(first update-column-contexts-arrays)
          (some #(and (can-merge? % (first update-column-contexts-arrays)) %) old-row-column-contexts)]
       (let [update-column-context (first update-column-contexts-arrays)
             row-column-to-merge (some #(and (can-merge? % update-column-context) %) old-row-column-contexts)]
         (if row-column-to-merge
           [update-column-context
            row-column-to-merge]
           (recur (rest remaining-update-column-contexts-arrays))))))))

(defn insert-or-update-row-contexts
  "It checks if the new columns can be added into the table context inside and exisiting
   row context or a new row context must be created.
   After that it updates the collection of row contexts for the table context inside the
   schema context accordingly"
  ([table-mapper update-column-contexts-arrays update-schema-context]
     (let [update-table-context (get-table-context-in-schema-context update-schema-context table-mapper)
           old-row-column-contexts (:update-row-contexts update-table-context)
           ;; The following line tries to find a row context where the new
           ;; column contexts can be merged
           [update-column-contexts row-column-to-merge] (select-update-column-and-row-contexts old-row-column-contexts update-column-contexts-arrays)]
       (if row-column-to-merge
         ;; The new columns are added to the row context to merge in the table context
         (let [new-update-table-context (assoc update-table-context :update-row-contexts (replace-mapping old-row-column-contexts
                                                                                                          row-column-to-merge
                                                                                                          (merge-contexts row-column-to-merge update-column-contexts)))]
           (assoc update-schema-context :update-table-contexts
             (replace-mapping (:update-table-contexts update-schema-context)
                              update-table-context
                              new-update-table-context)))
         ;; There is no row context to merge, one new is created and inserted
         ;; into the collection of row contexts for this table context
         (assoc update-schema-context :update-table-contexts
                (replace-mapping (:update-table-contexts update-schema-context)
                                 update-table-context
                                 (assoc update-table-context :update-row-contexts
                                        (conj old-row-column-contexts (UpdateRowContext. update-column-contexts :insert)))))))))

(defn table-context-grade
  ([table-context]
     (count (:update-row-contexts table-context))))

(defn schema-context-grade
  ([schema-context]
     (let [table-context-grades (map #(table-context-grade %) (:update-table-contexts schema-context))]
       (reduce + table-context-grades))))

(defn first-group
  ([groups] (second (first groups))))

(defn minimum-grade-schema-contexts
  ([schema-contexts]
     (first-group (sort (fn [ga gb] (< (first ga) (first gb)))
                        (group-by schema-context-grade schema-contexts)))))

(defn count-column-contexts
  ([schema-context]
     (let [rows-contexts (flatten (map #(:update-row-contexts %) (:update-table-contexts schema-context)))]
       (reduce + (map #(count (:update-column-contexts %)) rows-contexts)))))

(defn next-level-schema-contexts
  "Updates a schema context adding columns to row contexts or creating
   new row contexts for table contexts according to the results of applying
   a collection of compatible triple mappings to a triple"
  ([triple compatible-table-mappers-table update-schema-context]
     (let [new-column-contexts (map (fn [[table-mapper triple-mappers]]
                                      [table-mapper (map #(make-update-column-contexts triple %) triple-mappers)])
                                    compatible-table-mappers-table)
           new-update-schema-contexts (map (fn [[table-mapper update-column-contexts]]
                                             (insert-or-update-row-contexts table-mapper
                                                                            update-column-contexts
                                                                            update-schema-context))
                                           new-column-contexts)]
       (minimum-grade-schema-contexts new-update-schema-contexts))))
;;       (first-group ; first group, value not key
;;        (sort (fn [ga gb] (< (schema-context-grade ga) (schema-context-grade gb)))
;;              (group-by schema-context-grade
;;                        new-update-schema-contexts))))))

(defn generate-update-contexts
  "Generates the contexts to insert a set of triples in a database.
   If insertion is not possible, it will fail throwing an exception

   The result of this function is a collection of schema mappings with
   the following features:
   - They are equivalent, they all will insert the triples in the database.
   - They are guaranteed to be minimum, all schema mappings will create the
     same number of new rows in the database."
  ([triples update-context]
     ;; We first sort the triple so triples with the same subject will
     ;; we inserted consequently.
     ;; This will make easier to detect efficient insertions with less
     ;; computations
     (let [grouped-triples (group-triples triples)]
       (loop [grouped-triples grouped-triples
              schema-contexts (:update-schema-contexts update-context)]
         (println (str "IN LOOP --> " (vec schema-contexts)))
         (if (empty? grouped-triples)
           (assoc update-context :update-schema-contexts schema-contexts)
           (recur (rest grouped-triples)
                  ;; Each computational step is on the first triple in the
                  ;; sorted collection of triples
                  (let [triple (first grouped-triples)
                        ;; we first get the map with all the compatible triple
                        ;; mappings of the R2RML mapping for this triple
                        compatible-table-mappers (compatible-table-mappers-for-triple triple
                                                                                      (:table-mappers update-context))
                        ;; Provided the compatible triple mappings we update the
                        ;; current update schema contexts with the new update
                        ;; row contexts for the triple.
                        ;; The triple can be inserted merging with a compatible
                        ;; existing update row context or inserting a new row
                        ;; context in the update schema mapping. Thus, some
                        ;; generated update schema mapping can potentially have
                        ;; a greater grade than others
                        new-schema-contexts (flatten (map #(next-level-schema-contexts triple compatible-table-mappers %) schema-contexts))]
                    ;; for the next iteration we just pass the schema mappings
                    ;; with minimum grade
                    (minimum-grade-schema-contexts new-schema-contexts))))))))

;; Insertion DSL

(defn translate-insert-update-row
  ([[table-name row-context]]
     (if (= (:sql-operation row-context) :insert)
       (str "INSERT INTO " table-name "(" (within-commas (map :column (:update-column-contexts row-context)))
            ") VALUES (" (within-commas (map (fn [update-column-context] (str "'" (:value update-column-context) "'")) (:update-column-contexts row-context))) ")")
       (throw (Exception. "not supported yet")))))

(defn translate-insert
  ([triples table-mappers]
     (let [_ (println (str "TRIPLES SPARQL UPDATE: " (vec triples)))
           _ (println (str "TABLE MAPPERS: " table-mappers))
           initial-update-schema-context (UpdateSchemaContext. (map (fn [table-mapper] (clj-r2rml.sparql-update.UpdateTableContext. table-mapper [])) table-mappers))
           initial-update-context (UpdateContext.
                                   table-mappers
                                   [initial-update-schema-context])
           update-context (generate-update-contexts triples initial-update-context)
           _ (println (str "GENERATED UPDATE-CONTEXT: " (vec update-context)))]
       (if (nil? update-context)
         (throw (Exception. "Cannot generate update contexts for insertion"))
         (let [update-schema-context (first (:update-schema-contexts update-context))
               update-row-contexts (map (fn [update-table-context] [(:table-name (:table-mapper update-table-context))
                                                                             (:update-row-contexts update-table-context)])
                                        (:update-table-contexts update-schema-context))
               update-column-contexts (apply concat (map (fn [[table-name row-contexts]]
                                                      (map (fn [row-context] [table-name row-context]) row-contexts))
                                                    update-row-contexts))]
           (within-tokens " ; " (map translate-insert-update-row update-column-contexts)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;
;;  deletion algoritmh  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Delete context
(defrecord DeleteColumnCondition [column-name column-value])
(defrecord DeleteRowContext [projection-columns conditions-columns])
(defrecord DeleteContext [table-mappers update-schema-contexts])

(defn make-delete-row-context-projection
  ([triple-mapper position]
     (let [term-mapper (position triple-mapper)]
       (if (= :constant (:kind term-mapper))
         nil (:value term-mapper)))))

(defn make-delete-row-context-condition
  ([triple triple-mapper position]
     (let [triple-term ((extract-position position) triple)
           term-mapper (position triple-mapper)]
       (if (= :constant (:kind term-mapper))
         nil {:column (:value term-mapper)
              :value  triple-term}))))

(defn make-delete-row-context
  ([triple triple-mapper]
     (loop [positions   [:subject :property :object :graph]
            projections []
            conditions  []]
       (if (empty? positions)
         (DeleteRowContext. (filter #(not (nil? %)) projections)
                            (filter #(not (nil? %)) conditions))
         (let [position (first positions)]
           (recur (rest positions)
                  (conj projections (make-delete-row-context-projection triple-mapper position))
                  (conj conditions (make-delete-row-context-condition triple triple-mapper position))))))))

(defn generate-deletion-contexts
  ([triples delete-context]
     (apply concat (for [triple triples]
                     (let [compatible-table-mappers (compatible-table-mappers-for-triple triple
                                                                                         (:table-mappers delete-context))]
                       (for [[table-mapper triple-mappers] compatible-table-mappers]
                         [table-mapper (map #(make-delete-row-context triple %) triple-mappers)]))))))


(defn translate-delete
  ([triples table-mapper should-join]
     (let [sql (translate-delete triples table-mapper)]
       (if should-join
         (str (within-tokens "; " sql) ";")
         sql)))
  ([triples table-mappers]
     (let [delete-context  (DeleteContext. table-mappers [])
           delete-row-contexts (generate-deletion-contexts triples delete-context)]
       (flatten (map (fn [[table-mapper delete-row-contexts]]
                                        ;(println "delete-row-contexts")
                                        ;(pprint delete-row-contexts)
                                                (map (fn [row-context]
                                                       (str "DELETE FROM " (:table-name table-mapper) " WHERE "
                                                            (within-tokens " AND "
                                                                           (map (fn [cond] (str (:column cond) "='" (:value cond)  "'"))
                                                                                (:conditions-columns row-context)))))
                                                     delete-row-contexts))
                     delete-row-contexts)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;
;;   query  algoritmh   ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;


(defrecord SelectTripleProjection [value name kind])
(defrecord SelectTripleCondition [column value])
(defrecord SelectRowSubselect [table-name projections conditions])

;; translation for a single triple
(defrecord SelectTriple [select-row-subselects])
(defrecord SelectAnd [common-terms exclusive-terms content])
(defrecord SelectOpt [common-terms exclusive-terms content])

(defn name-sql
  ([component]
     (if (keyword? component)
       (name component)
       (if (string? component)
         (str "'" component  "'")))))

(defn make-column-select-projection
  ([triple triple-mapper position]
     (let [triple-term ((extract-position position) triple)
           term-mapper (position triple-mapper)]
       (if (variable-term? triple-term)
         (if (= :constant (:kind term-mapper))
           (SelectTripleProjection. (:value term-mapper) (name-sql triple-term) :constant)
           (SelectTripleProjection. (:value term-mapper) (name-sql triple-term) :column))
         nil))))

(defn make-triple-select-projections
  ([triple triple-mapper]
     (loop [positions   [:subject :property :object :graph]
            projections []]
       (if (empty? positions)
         (filter #(not (nil? %)) projections)
         (let [position (first positions)]
           (recur (rest positions)
                  (conj projections (make-column-select-projection triple triple-mapper position))))))))

(defn make-column-select-condition
  ([triple triple-mapper position]
     (let [triple-term ((extract-position position) triple)
           term-mapper (position triple-mapper)]
       (if (= :constant (:kind term-mapper))
         nil
         (if (variable-term? triple-term)
           nil
           (SelectTripleCondition. (:value term-mapper) triple-term))))))

(defn make-triple-select-conditions
  ([triple triple-mapper]
     (loop [positions   [:subject :property :object :graph]
            conditions []]
       (if (empty? positions)
         (filter #(not (nil? %)) conditions)
         (let [position (first positions)]
           (recur (rest positions)
                  (conj conditions (make-column-select-condition triple triple-mapper position))))))))

(defn make-triple-row-subselect
  ([triple table-mapper triple-mapper]
     (let [conditions (make-triple-select-conditions triple triple-mapper)
           projections (make-triple-select-projections triple triple-mapper)
           table-name (:table-name triple-mapper)]
       (SelectRowSubselect. table-name projections conditions))))

(defn select-triple
  ([triple table-mappers]
     (let [compatible-table-mappers (compatible-table-mappers-for-triple triple
                                                                         table-mappers)]
       (SelectTriple. (flatten (map (fn [[table-mapper triple-mappers]]
                                      (map (fn [triple-mapper] (make-triple-row-subselect triple table-mapper
                                                                                         triple-mapper))
                                           triple-mappers))
                                    compatible-table-mappers))))))


(defmulti get-terms (fn [triple-group] (class triple-group)))

(defmethod get-terms SelectTriple
  ([triple-group]
     (let [projections (:projections (first (:select-row-subselects triple-group)))]
       (map :name projections))))

(defmethod get-terms SelectAnd
  ([triple-and] (concat (:common-terms triple-and)
                        (:exclusive-terms triple-and))))

(defmethod get-terms SelectOpt
  ([triple-opt] (concat (:common-terms triple-opt)
                        (:exclusive-terms triple-opt))))

(defn select-and
  ([gp1 gp2]
     (let [terms-gp1 (set (get-terms gp1))
           terms-gp2 (set (get-terms gp2))
           terms-only-gp1 (vec (clojure.set/difference terms-gp1 terms-gp2))
           terms-only-gp2 (vec (clojure.set/difference terms-gp2 terms-gp1))
           terms-intersection-gp1-gp2 (vec (clojure.set/intersection terms-gp1 terms-gp2))]
       (SelectAnd. terms-intersection-gp1-gp2 (concat terms-only-gp1 terms-only-gp2) [gp1 gp2]))))

(defn select-opt
  ([gp1 gp2]
     (let [terms-gp1 (set (get-terms gp1))
           terms-gp2 (set (get-terms gp2))
           terms-only-gp1 (vec (clojure.set/difference terms-gp1 terms-gp2))
           terms-only-gp2 (vec (clojure.set/difference terms-gp2 terms-gp1))
           terms-intersection-gp1-gp2 (vec (clojure.set/intersection terms-gp1 terms-gp2))]
       (SelectOpt. terms-intersection-gp1-gp2 (concat terms-only-gp1 terms-only-gp2) [gp1 gp2]))))

(defn table-alias []
  (str "TBL" (.replace (str (UUID/randomUUID)) "-" "")))

(defmulti translate (fn [triple-group] (class triple-group)))

(defmethod translate SelectRowSubselect
  ([select-row-subselect]
     (str "(SELECT DISTINCT "
          (within-commas (map (fn [projection] (str (if (= :constant (:kind projection))
                                                     (str "'" (:value projection) "'")
                                                     (:value projection))
                                                   " AS " (:name projection))) (:projections select-row-subselect)))
          " FROM " (:table-name select-row-subselect)
          " WHERE " (reduce (fn [acum-prj projection] (if (= :constant (:kind projection))
                                                       acum-prj
                                                       (str acum-prj " AND " (:value projection) " IS NOT NULL")))
                     (reduce (fn [acum condition] (str acum " AND " (:column condition) "='" (:value condition)"' "))
                             "TRUE"
                             (:conditions select-row-subselect))
                     (:projections select-row-subselect))
          ")")))

(defmethod translate SelectTriple
  ([triple-group]
     (let [subselects (:select-row-subselects triple-group)
           translated-projections (map translate subselects)]
       (within-tokens " UNION " translated-projections))))

(defmethod translate SelectAnd
  ([triple-group]
     (let [gp1 (first (:content triple-group))
           gp2 (second (:content triple-group))
           new-table-gp1 (table-alias)
           new-table-gp2 (table-alias)
           intersection-terms (:common-terms triple-group)
           exclusive-terms (:exclusive-terms triple-group)
           select-sql-terms-intersection (within-commas (map (fn [term] (str "Coalesce(" new-table-gp1 "." term ", " new-table-gp2 "." term ") AS " term))
                                                             intersection-terms))
           select-sql (within-commas (if (empty? intersection-terms) exclusive-terms (concat exclusive-terms [select-sql-terms-intersection])))


           inner-join-sql (str "(" (translate gp1) ") " new-table-gp1 " INNER JOIN (" (translate gp2) ") " new-table-gp2)
           join-cond-sql (reduce (fn [condition term] (str condition
                                                          " AND (" new-table-gp1 "." term "=" new-table-gp2 "." term ")"
                                                          " OR " new-table-gp1 "." term " IS NULL"
                                                          " OR " new-table-gp2 "." term " IS NULL"))
                                 "TRUE"
                                 intersection-terms)]
       (str "SELECT DISTINCT " select-sql " FROM " inner-join-sql " ON (" join-cond-sql ")"))))

(defmethod translate SelectOpt
  ([triple-group]
     (let [gp1 (first (:content triple-group))
           gp2 (second (:content triple-group))
           new-table-gp1 (table-alias)
           new-table-gp2 (table-alias)
           intersection-terms (:common-terms triple-group)
           exclusive-terms (:exclusive-terms triple-group)
           select-sql-terms-intersection (within-commas (map (fn [term] (str "Coalesce(" new-table-gp1 "." term ", " new-table-gp2 "." term ") AS " term))
                                                             intersection-terms))
           select-sql (within-commas (if (empty? intersection-terms) exclusive-terms (concat exclusive-terms [select-sql-terms-intersection])))


           inner-join-sql (str "(" (translate gp1) ") " new-table-gp1 " LEFT OUTER JOIN (" (translate gp2) ") " new-table-gp2)
           join-cond-sql (reduce (fn [condition term] (str condition
                                                          " AND (" new-table-gp1 "." term "=" new-table-gp2 "." term ")"
                                                          " OR " new-table-gp1 "." term " IS NULL"
                                                          " OR " new-table-gp2 "." term " IS NULL"))
                                 "TRUE"
                                 intersection-terms)]
       (str "SELECT DISTINCT " select-sql " FROM " inner-join-sql " ON (" join-cond-sql ")"))))


;; SELECT DSL

(defn dsl-triple
  ([triple]
     (fn [triple-mappers]
       (select-triple triple triple-mappers))))


(defn AND
  ([& gps]
     (println (str "AND FOR " gps))
     (when (= 0 (count gps)) (throw (Exception. (str "EMPTY AND"))))
     (if (= 2 (count gps))
       (fn [triple-mappers]
         (let [gpa (first gps)
               gpb (second gps)
               strct-gpa (if (vector? gpa) ((dsl-triple gpa) triple-mappers)
                             (if (fn? gpa) (gpa triple-mappers) gpa))
               strct-gpb (if (vector? gpb) ((dsl-triple gpb) triple-mappers)
                             (if (fn? gpb) (gpb triple-mappers) gpb))]
           (select-and strct-gpa strct-gpb)))
       (fn [triple-mappers]
         ((apply AND [(first gps)
                      ((apply AND (rest gps)) triple-mappers)])
          triple-mappers)))))

(defn OPT
  ([& gps]
     (if (= 2 (count gps))
       (fn [triple-mappers]
         (let [gpa (first gps)
               gpb (second gps)
               strct-gpa (if (vector? gpa) ((dsl-triple gpa) triple-mappers)
                             (if (fn? gpa) (gpa triple-mappers) gpa))
               strct-gpb (if (vector? gpb) ((dsl-triple gpb) triple-mappers)
                             (if (fn? gpb) (gpb triple-mappers) gpb))]
           (select-opt strct-gpa strct-gpb)))
       (fn [triple-mappers]
         ((apply OPT [(first gps)
                      ((apply OPT (rest gps)) triple-mappers)])
          triple-mappers)))))
