(ns clj-r2rml.test.sparql-update
  (:use [clj-r2rml.sparql-update] :reload)
  (:use [clojure.test])
  (:use [clojure.pprint]))


(deftest test-triple-compatible
  (let [triple-a ["a" "b" "c"]
        triple-b ["a" "b" "e"]
        map-a (clj-r2rml.sparql-update.UpdateTripleMapper. "test"
                                                           (clj-r2rml.sparql-update.UpdateTermMapper. :subject :constant "a")
                                                           (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "b")
                                                           (clj-r2rml.sparql-update.UpdateTermMapper. :object :constant "c"))
        map-b (clj-r2rml.sparql-update.UpdateTripleMapper. "test"
                                                           (clj-r2rml.sparql-update.UpdateTermMapper. :subject :variable "column_a")
                                                           (clj-r2rml.sparql-update.UpdateTermMapper. :property :variable "column_b")
                                                           (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_c"))]
    (is (triple-compatible? triple-a map-a))
    (is (not (triple-compatible? triple-b map-a)))
    (is (triple-compatible? triple-a map-b))
    (is (triple-compatible? triple-b map-b))))

(deftest make-update-column-context-test
  (let [triple-a ["a" "b" "c"]
        map-a (clj-r2rml.sparql-update.UpdateTripleMapper. "test"
                                                           (clj-r2rml.sparql-update.UpdateTermMapper. :subject :variable "column_a")
                                                           (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "b")
                                                           (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_c"))
        result (make-update-column-contexts triple-a map-a)]
    (is (= 2 (count result)))
    (let [col1 (:column (first result))
          val1 (:value (first result))
          col2 (:column (second result))
          val2 (:value (second result))]
      (is (not= col1 col2))
      (is (not= val1 val2))
      (is (or (= val1 "a") (= val2 "c")))
      (is (or (= col1 "column_a") (= col2 "column_c"))))))

(deftest make-update-row-context-merge-test
  (let [triple-a ["a" "b" "c"]
        row-context (clj-r2rml.sparql-update.UpdateRowContext. [] :insert)
        map-a (clj-r2rml.sparql-update.UpdateTripleMapper. "test"
                                                           (clj-r2rml.sparql-update.UpdateTermMapper. :subject :variable "column_a")
                                                           (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "b")
                                                           (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_c"))
        update-columns (make-update-column-contexts triple-a map-a)]
    (is (can-merge? row-context update-columns))))

(deftest conflicting-contexts-test
  (let [ca (clj-r2rml.sparql-update.UpdateColumnContext. "a" "va")
        cb (clj-r2rml.sparql-update.UpdateColumnContext. "b" "vb")
        ca1 (clj-r2rml.sparql-update.UpdateColumnContext. "a" "vb")
        ca2 (clj-r2rml.sparql-update.UpdateColumnContext. "b" "va")
        caa (clj-r2rml.sparql-update.UpdateColumnContext. "a" "va")]
    (is (not (conflicting-contexts? ca cb)))
    (is (conflicting-contexts? ca ca1))
    (is (not (conflicting-contexts? ca ca2)))
    ;; duplicated but not conflicting
    (is (not (conflicting-contexts? ca caa)))))

(deftest merge-contexts-test
  (let [triple-a ["a" "b" "c"]
        row-context (clj-r2rml.sparql-update.UpdateRowContext. [] :insert)
        map-a (clj-r2rml.sparql-update.UpdateTripleMapper. "test"
                                                           (clj-r2rml.sparql-update.UpdateTermMapper. :subject :variable "column_a")
                                                           (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "b")
                                                           (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_c"))
        compatible-update-column-contexts (make-update-column-contexts triple-a map-a)
        merged (merge-contexts row-context compatible-update-column-contexts)]
    (is (= 2 (count (:update-column-contexts merged))))))

(defn mock-schema-context
  ([] (let [table-mapper-a (clj-r2rml.sparql-update.UpdateTableMapper. "test_letters"
                                                                       [(clj-r2rml.sparql-update.UpdateTripleMapper. "test_letters"
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :subject :variable "column_a")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "b")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_c"))])
            table-mapper-b (clj-r2rml.sparql-update.UpdateTableMapper. "test_numbers"
                                                                       [(clj-r2rml.sparql-update.UpdateTripleMapper. "test_numbers"
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :subject :variable "column_1")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "2")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_3"))])
            table-mapper-c (clj-r2rml.sparql-update.UpdateTableMapper. "triples"
                                                                       [(clj-r2rml.sparql-update.UpdateTripleMapper. "triples"
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :subject :variable "s")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :property :variable "p")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "o"))])
            table-context-a (clj-r2rml.sparql-update.UpdateTableContext. table-mapper-a [])
            table-context-b (clj-r2rml.sparql-update.UpdateTableContext. table-mapper-b [])
            table-context-c (clj-r2rml.sparql-update.UpdateTableContext. table-mapper-c [])]
        (clj-r2rml.sparql-update.UpdateSchemaContext. [table-context-a table-context-b table-context-c]))))

(defn table-context-by-table-name
  ([name schema-context]
     (first (reduce  (fn [ac it] (if (= name (:table-name (:table-mapper it)))
                                  (conj ac it) ac))
                     []
                     (:update-table-contexts schema-context)))))

(deftest get-compatible-table-mappers-test
  (let [test-schema-context (mock-schema-context)
        mapping (get-compatible-table-mappers ["a" "b" "c"] (map :table-mapper (:update-table-contexts test-schema-context)))]
    (is (= 2 (count (keys mapping))))
    (is (= 1 (count (get mapping (:table-mapper (table-context-by-table-name "test_letters" test-schema-context))))))
    (is (= 1 (count (get mapping (:table-mapper (table-context-by-table-name "triples" test-schema-context))))))))

(deftest count-contant-term-mappers-test
  (let [triple-mapper (clj-r2rml.sparql-update.UpdateTripleMapper. "test_letters"
                                                                   (clj-r2rml.sparql-update.UpdateTermMapper. :subject :variable "column_a")
                                                                   (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "b")
                                                                   (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_c"))]
    (is (= 1 (count-constant-term-mappers triple-mapper)))))

(deftest sort-triple-mappers-test
  (let [triple-mapper-a (clj-r2rml.sparql-update.UpdateTripleMapper. "test_letters"
                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :subject :variable "column_a")
                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "b")
                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_c"))
        triple-mapper-b (clj-r2rml.sparql-update.UpdateTripleMapper. "test_letters"
                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :subject :constant "e")
                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "b")
                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_c"))
        triple-mappers [triple-mapper-a
                        triple-mapper-b]
        sorted-triple-mappers (sort-triple-mappers triple-mappers)]
    (is (= triple-mapper-b (first sorted-triple-mappers)))
    (is (= triple-mapper-a (second sorted-triple-mappers)))))

(defn mock-schema-context-2
  ([] (let [table-mapper-a (clj-r2rml.sparql-update.UpdateTableMapper. "test_letters"
                                                                       [(clj-r2rml.sparql-update.UpdateTripleMapper. "test_letters"
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :subject :variable "column_a")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "b")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_c"))
                                                                        (clj-r2rml.sparql-update.UpdateTripleMapper. "test_letters"
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :subject :constant "a")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "b")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_c"))
                                                                        ])
            table-mapper-b (clj-r2rml.sparql-update.UpdateTableMapper. "test_numbers"
                                                                       [(clj-r2rml.sparql-update.UpdateTripleMapper. "test_numbers"
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :subject :variable "column_1")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "2")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_3"))])
            table-mapper-c (clj-r2rml.sparql-update.UpdateTableMapper. "triples"
                                                                       [(clj-r2rml.sparql-update.UpdateTripleMapper. "triples"
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :subject :variable "s")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :property :variable "p")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "o"))])
            table-context-a (clj-r2rml.sparql-update.UpdateTableContext. table-mapper-a [])
            table-context-b (clj-r2rml.sparql-update.UpdateTableContext. table-mapper-b [])
            table-context-c (clj-r2rml.sparql-update.UpdateTableContext. table-mapper-c [])]
        (clj-r2rml.sparql-update.UpdateSchemaContext. [table-context-a table-context-b table-context-c]))))

(deftest sort-table-mappers-map-test
  (let [test-schema-context (mock-schema-context-2)
        mapping (get-compatible-table-mappers ["a" "b" "c"] (map :table-mapper (:update-table-contexts test-schema-context)))
        sorted-mappers-list (sort-table-mappers-map mapping)]
    (let [first-option (first (second (first sorted-mappers-list)))]
      (is (= (:kind (:subject first-option)) :constant))
      (is (= (:kind (:property first-option)) :constant))
      (is (= (:kind (:object first-option)) :variable))
      (is (= (:value (:subject first-option)) "a"))
      (is (= (:value (:property first-option)) "b"))
      (is (= (:value (:object first-option)) "column_c"))
      (is (= 2 (count sorted-mappers-list)))
      (is (= 2 (count (second (first sorted-mappers-list))))))
    (let [second-option (second (second (first sorted-mappers-list)))]
      (is (= (:kind (:subject second-option)) :variable))
      (is (= (:kind (:property second-option)) :constant))
      (is (= (:kind (:object second-option)) :variable))
      (is (= (:value (:subject second-option)) "column_a"))
      (is (= (:value (:property second-option)) "b"))
      (is (= (:value (:object second-option)) "column_c"))))
  (let [test-schema-context (mock-schema-context-2)
        mapping (get-compatible-table-mappers ["e" "b" "c"] (map :table-mapper (:update-table-contexts test-schema-context)))
        sorted-mappers-list (sort-table-mappers-map mapping)]
    (let [first-option (first (second (first sorted-mappers-list)))]
      (is (= (:kind (:subject first-option)) :variable))
      (is (= (:kind (:property first-option)) :constant))
      (is (= (:kind (:object first-option)) :variable))
      (is (= (:value (:subject first-option)) "column_a"))
      (is (= (:value (:property first-option)) "b"))
      (is (= (:value (:object first-option)) "column_c"))
      (is (= 2 (count sorted-mappers-list))))))

(deftest get-compatible-table-mappers-test-2
  (let [test-schema-context (mock-schema-context-2)
        mapping (get-compatible-table-mappers ["e" "b" "c"] (map :table-mapper (:update-table-contexts test-schema-context)))]
    (is (= 2 (count (keys mapping))))
    (is (= 1 (count (get mapping (:table-mapper (table-context-by-table-name "test_letters" test-schema-context))))))
    (is (= 1 (count (get mapping (:table-mapper (table-context-by-table-name "triples" test-schema-context))))))))

(deftest get-compatible-table-mappers-test-3
  (let [test-schema-context (mock-schema-context-2)
        mapping (get-compatible-table-mappers ["a" "b" "c"] (map :table-mapper (:update-table-contexts test-schema-context)))]
    (is (= 2 (count (keys mapping))))
    (is (= 2 (count (get mapping (:table-mapper (table-context-by-table-name "test_letters" test-schema-context))))))
    (is (= 1 (count (get mapping (:table-mapper (table-context-by-table-name "triples" test-schema-context))))))))

(deftest mappers-equality-test
  (is (= (clj-r2rml.sparql-update.UpdateTripleMapper. "test_letters"
                                                      (clj-r2rml.sparql-update.UpdateTermMapper. :subject :variable "column_a")
                                                      (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "b")
                                                      (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_c"))
         (clj-r2rml.sparql-update.UpdateTripleMapper. "test_letters"
                                                      (clj-r2rml.sparql-update.UpdateTermMapper. :subject :variable "column_a")
                                                      (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "b")
                                                      (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_c")))))


(deftest get-table-context-in-schema-context-test
  (let [schema (mock-schema-context-2)
        mapper (clj-r2rml.sparql-update.UpdateTableMapper. "test_letters"
                                                                       [(clj-r2rml.sparql-update.UpdateTripleMapper. "test_letters"
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :subject :variable "column_a")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "b")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_c"))
                                                                        (clj-r2rml.sparql-update.UpdateTripleMapper. "test_letters"
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :subject :constant "a")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "b")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_c"))
                                                                        ])
        mapper-b (clj-r2rml.sparql-update.UpdateTableMapper. "test_letters"
                                                             [(clj-r2rml.sparql-update.UpdateTripleMapper. "test_letters"
                                                                                                           (clj-r2rml.sparql-update.UpdateTermMapper. :subject :variable "column_a")
                                                                                                           (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "b")
                                                                                                           (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_c"))
                                                              (clj-r2rml.sparql-update.UpdateTripleMapper. "test_lettersb"
                                                                                                           (clj-r2rml.sparql-update.UpdateTermMapper. :subject :constant "a")
                                                                                                           (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "b")
                                                                                                           (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_c"))
                                                                        ])
        table-context (clj-r2rml.sparql-update.UpdateTableContext. mapper [])]
    (is (= table-context (get-table-context-in-schema-context schema mapper)))
    (is (nil? (get-table-context-in-schema-context schema mapper-b)))))

(deftest replace-mapping-test
  (let [old [1 2 3 4 5]
        new [1 2 7 4 5]]
    (is (= new (replace-mapping old 3 7)))
    (is (= old (replace-mapping old 43 7)))))

(defn mock-schema-context-3
  ([] (let [table-mapper-a (clj-r2rml.sparql-update.UpdateTableMapper. "test_letters"
                                                                       [(clj-r2rml.sparql-update.UpdateTripleMapper. "test_letters"
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :subject :variable "column_a")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "b")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_c"))
                                                                        (clj-r2rml.sparql-update.UpdateTripleMapper. "test_letters"
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :subject :constant "a")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "b")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_c"))
                                                                        (clj-r2rml.sparql-update.UpdateTripleMapper. "test_letters"
                                                                                                        (clj-r2rml.sparql-update.UpdateTermMapper. :subject :constant "a")
                                                                                                        (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "f")
                                                                                                        (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_g"))
                                                                        ])
            table-mapper-b (clj-r2rml.sparql-update.UpdateTableMapper. "test_numbers"
                                                                       [(clj-r2rml.sparql-update.UpdateTripleMapper. "test_numbers"
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :subject :variable "column_1")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "2")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_3"))])
            table-mapper-c (clj-r2rml.sparql-update.UpdateTableMapper. "triples"
                                                                       [(clj-r2rml.sparql-update.UpdateTripleMapper. "triples"
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :subject :variable "s")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :property :variable "p")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "o"))])
            table-context-a (clj-r2rml.sparql-update.UpdateTableContext. table-mapper-a [])
            table-context-b (clj-r2rml.sparql-update.UpdateTableContext. table-mapper-b [])
            table-context-c (clj-r2rml.sparql-update.UpdateTableContext. table-mapper-c [])]
        (clj-r2rml.sparql-update.UpdateSchemaContext. [table-context-a table-context-b table-context-c]))))

(deftest insert-or-update-row-contexts-test
  (let [schema (mock-schema-context-3)
        triple ["a" "b" "c"]
        mapper (clj-r2rml.sparql-update.UpdateTableMapper. "test_letters"
                                                           [(clj-r2rml.sparql-update.UpdateTripleMapper. "test_letters"
                                                                                                         (clj-r2rml.sparql-update.UpdateTermMapper. :subject :variable "column_a")
                                                                                                         (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "b")
                                                                                                         (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_c"))
                                                            (clj-r2rml.sparql-update.UpdateTripleMapper. "test_letters"
                                                                                                         (clj-r2rml.sparql-update.UpdateTermMapper. :subject :constant "a")
                                                                                                         (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "b")
                                                                                                         (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_c"))
                                                            (clj-r2rml.sparql-update.UpdateTripleMapper. "test_letters"
                                                                                                        (clj-r2rml.sparql-update.UpdateTermMapper. :subject :constant "a")
                                                                                                        (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "f")
                                                                                                        (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_g"))
                                                            ])
        mappings (second (first (compatible-table-mappers-for-triple triple [mapper])))
;        _ (println "1 -------------------------")
;        _ (pprint mappings)
        new-update-columns (map  #(make-update-column-contexts triple %) mappings)]
;        _ (pprint new-update-columns)
    (is (= 0 (count (:update-row-contexts (get-table-context-in-schema-context schema mapper)))))
    (is (= 0 (schema-context-grade schema)))
    (let [updated-schema (insert-or-update-row-contexts mapper new-update-columns schema)]
      (is (= 1 (count (:update-row-contexts (get-table-context-in-schema-context updated-schema mapper)))))
      (is (= 1 (schema-context-grade updated-schema)))
      (let [mappings (second (first (compatible-table-mappers-for-triple ["a" "b" "m"] [mapper])))
            new-update-columns (map  #(make-update-column-contexts ["a" "b" "m"] %) mappings)
;            _ (println "2 ---------------------")
;            _ (pprint mappings)
;            _ (pprint new-update-columns)
            updated-schema-b (insert-or-update-row-contexts mapper new-update-columns updated-schema)]
        (is (= 2 (count (:update-row-contexts (get-table-context-in-schema-context updated-schema-b mapper)))))
        (is (= 2 (schema-context-grade updated-schema-b)))
        (let [mapping (second (first (compatible-table-mappers-for-triple ["a" "f" "n"] [mapper])))
              new-update-columns (make-update-column-contexts ["a" "f" "n"] mapping)
;              _ (println "3 ---------------------")
;              _ (pprint mapping)
;              _ (pprint new-update-columns)
              updated-schema-c (insert-or-update-row-contexts mapper new-update-columns updated-schema-b)]
          (is (= 2 (count (:update-row-contexts (get-table-context-in-schema-context updated-schema-c mapper)))))
          (is (= 2 (schema-context-grade updated-schema-c))))))))
;          (pprint (:update-row-contexts (get-table-context-in-schema-context updated-schema-c mapper))))))))


(deftest next-level-schema-contexts-test
  (let [schema-context (mock-schema-context-3)
        update-context (clj-r2rml.sparql-update.UpdateContext. (map :table-mapper (:update-table-contexts schema-context))
                                                               [schema-context])
        triple ["a" "b" "c"]
        compatible-table-mappers (compatible-table-mappers-for-triple triple (:table-mappers update-context))
        next-schema-contexts (next-level-schema-contexts triple compatible-table-mappers schema-context)]
    (is (= 2 (count next-schema-contexts)))
    (doseq [next-schema-context next-schema-contexts]
      (is (= 1 (schema-context-grade next-schema-context))))
    (let [compatible-table-mappers (compatible-table-mappers-for-triple ["a" "f" "g"] (:table-mappers update-context))
          level2-schema-contexts (next-level-schema-contexts ["a" "f" "g"] compatible-table-mappers (first next-schema-contexts))]
      (is (= 1 (count level2-schema-contexts)))
      (is (= 1 (schema-context-grade (first level2-schema-contexts)))))))


(defn mock-schema-context-4
  ([] (let [table-mapper-a (clj-r2rml.sparql-update.UpdateTableMapper. "test_letters"
                                                                       [(clj-r2rml.sparql-update.UpdateTripleMapper. "test_letters"
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :subject :variable "column_letters_id")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "b")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_b"))
                                                                        (clj-r2rml.sparql-update.UpdateTripleMapper. "test_letters"
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :subject :variable "column_letters_id")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "c")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_c"))
                                                                        ])
            table-mapper-b (clj-r2rml.sparql-update.UpdateTableMapper. "test_numbers"
                                                                       [(clj-r2rml.sparql-update.UpdateTripleMapper. "test_numbers"
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :subject :variable "column_numbers_id")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :property :constant "2")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "column_2"))])
            table-mapper-c (clj-r2rml.sparql-update.UpdateTableMapper. "triples"
                                                                       [(clj-r2rml.sparql-update.UpdateTripleMapper. "triples"
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :subject :variable "s")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :property :variable "p")
                                                                                                                     (clj-r2rml.sparql-update.UpdateTermMapper. :object :variable "o"))])
            table-context-a (clj-r2rml.sparql-update.UpdateTableContext. table-mapper-a [])
            table-context-b (clj-r2rml.sparql-update.UpdateTableContext. table-mapper-b [])
            table-context-c (clj-r2rml.sparql-update.UpdateTableContext. table-mapper-c [])]
        (clj-r2rml.sparql-update.UpdateSchemaContext. [table-context-a table-context-b table-context-c]))))

(deftest generate-update-contexts-test
  (let [schema-context (mock-schema-context-4)
        table-mappers (map :table-mapper (:update-table-contexts schema-context))
        update-context (clj-r2rml.sparql-update.UpdateContext. table-mappers [schema-context])
        triples [["a" "b" "vb"]
                 ["1" "2" "v2"]
                 ["a" "f" "vf"]
                 ["a" "c" "vc"]]
        result (:update-schema-contexts (generate-update-contexts triples update-context))]))

;;    (println "RESULT\n\n\n")
;;    (println (str "CONTEXTS: " (count result)))
;;
;;    (doseq [res result]
;;      (println (str "\n\n---------------------------------------(" (count-column-contexts res) ")\n\n"))
;;      (pprint res)
;;
;;    (println (str "\n\n---------------------------------------\n\n"))
;;    (pprint (first result))
;;    (println (str "\n\n---------------------------------------\n\n"))
;;    (pprint (second result)))))


(deftest generate-deletion-contexts-test
  (let [triples [["a" "b" "c"]]
        schema-context (mock-schema-context-4)
        table-mappers (map :table-mapper (:update-table-contexts schema-context))
        delete-context (clj-r2rml.sparql-update.DeleteContext. table-mappers [schema-context])]
;    (pprint (generate-deletion-contexts triples delete-context))
    ))

;; queries

(deftest select-triple-test
  (let [schema-context (mock-schema-context-4)
        table-mappers (map :table-mapper (:update-table-contexts schema-context))]
    (is (= ["s"] (get-terms (select-triple [:s "b" "n"] table-mappers))))))
    ;;(pprint (select-triple [:s :p :o] table-mappers))
    ;;(println "----------------------------------------")
    ;;(pprint (select-triple [:s "b" "n"] table-mappers))))


(deftest translate-triple
  (let [schema-context (mock-schema-context-4)
        table-mappers (map :table-mapper (:update-table-contexts schema-context))
        triple (select-triple ["a" "c" :c] table-mappers)]
    (is (= "(SELECT DISTINCT column_c AS c FROM test_letters WHERE TRUE AND column_letters_id='a'  AND column_c IS NOT NULL) UNION (SELECT DISTINCT o AS c FROM triples WHERE TRUE AND s='a'  AND p='c'  AND o IS NOT NULL)"
           (translate triple)))))


(deftest translate-triple-and
  (let [schema-context (mock-schema-context-4)
        table-mappers (map :table-mapper (:update-table-contexts schema-context))
        triple (select-and (select-triple ["a" "b" :b] table-mappers)
                           (select-triple ["a" "c" :c] table-mappers))]))
;    (pprint (translate triple))))


(deftest translate-triple-opt
  (let [schema-context (mock-schema-context-4)
        table-mappers (map :table-mapper (:update-table-contexts schema-context))
        triple (select-opt (select-and (select-triple ["a" "b" :b] table-mappers)
                                       (select-triple ["a" "c" :c] table-mappers))
                           (select-triple [:s "b" :o] table-mappers))]))
;    (pprint (translate triple))))

(deftest AND-1-test
  (let [schema-context (mock-schema-context-4)
        table-mappers (map :table-mapper (:update-table-contexts schema-context))
        query (AND ["a" "b" :b]
                   ["a" "c" :c]
                   [:s :p :o])]))
;;    (pprint (translate (query table-mappers)))))
;;    (pprint (translate (query table-mappers)))))


(deftest AND-2-test
  (let [schema-context (mock-schema-context-4)
        table-mappers (map :table-mapper (:update-table-contexts schema-context))
        query (AND ["a" :t :b]
                   ["a" "c" :c]
                   (OPT ["z" :t :u]
                        ["z" :t :o]))]))
;;    (pprint (translate (query table-mappers)))))



(deftest test-translate-insert
  (let [schema-context (mock-schema-context-4)
        table-mappers (map :table-mapper (:update-table-contexts schema-context))
        triples [["a" "b" "vb"]
                 ["1" "2" "v2"]
                 ["a" "f" "vf"]
                 ["a" "c" "vc"]]
        sql (translate-insert triples table-mappers)]
    (println sql)
    ))

(deftest test-translate-delete
  (let [schema-context (mock-schema-context-4)
        table-mappers (map :table-mapper (:update-table-contexts schema-context))
        triples [["a" "b" "vb"]
                 ["1" "2" "v2"]]
        sql (translate-delete triples table-mappers)]
    (println sql)
    ))
