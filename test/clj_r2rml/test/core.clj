(ns clj-r2rml.test.core
  (:use [clj-r2rml.core] :reload)
  (:use [clojure.test]))

(def *db-spec*
     {:classname   "com.mysql.jdbc.Driver"
      :subprotocol "mysql"
      :user        "root"
      :password    ""
      :subname     "//localhost:3306/rdftests"})

(deftest should-generate-default-mapping-with-no-foreign-keys
      (let [*context* (make-context *db-spec* {})
            results (default-mapping-triples "http://clj-r2rml.test" {"Referenced" "select * from Referenced"} *context*)
            table-triples (filter (fn [triple] (= (:predicate triple) "rdf:type")) results)
            literal-triples (filter (fn [triple] (instance? clj-r2rml.core.Literal (:object triple))) results)]
        (is (= (count results) 8))
        ;; table triples
        (is (= (count table-triples) 2))
        (is (= (set (map (fn [triple] (:subject triple)) table-triples))
               (set ["http://clj-r2rml.test/Referenced/id=4#_" "http://clj-r2rml.test/Referenced/id=1#_"])))
        (is (= "http://clj-r2rml.test/Referenced" (:object (first table-triples))))
        (is (= (:object (first table-triples)) (:object (second table-triples))))
        ;; literal triples
        (is (= (count literal-triples) 6))
        (doseq [triple literal-triples]
          (is (= "http://www.w3.org/TR/xmlschema-2/#integer" (:datatype (:object triple)))))))


(deftest should-generate-default-mapping-with-foreign-keys
      (let [*context* (make-context *db-spec* {})
            results (default-mapping-triples "http://clj-r2rml.test" {"Referencing" "select * from Referencing"} *context*)
            table-triples (filter (fn [triple] (= (:predicate triple) "rdf:type")) results)
            literal-triples (filter (fn [triple] (instance? clj-r2rml.core.Literal (:object triple))) results)
            reference-triples (filter (fn [triple] (and (not (= (:predicate triple) "rdf:type"))
                                                     (not (instance? clj-r2rml.core.Literal (:object triple))))) results)]
        (is (= (count results) 6))
        (is (= (count results)
               (+ (count table-triples)
                  (count literal-triples)
                  (count reference-triples))))
        ;; table triples
        (is (= (count table-triples) 2))
        (is (= (set (map (fn [triple] (:subject triple)) table-triples))
               (set ["http://clj-r2rml.test/Referencing/id=11#_" "http://clj-r2rml.test/Referencing/id=10#_"])))
        (is (= "http://clj-r2rml.test/Referencing" (:object (first table-triples))))
        (is (= (:object (first table-triples)) (:object (second table-triples))))
        ;; literal triples
        (is (= (count literal-triples) 2))
        (doseq [triple literal-triples]
          (is (= "http://www.w3.org/TR/xmlschema-2/#integer" (:datatype (:object triple)))))
        ;; reference triples
        (is (= (set ["http://clj-r2rml.test/Referenced/refcol1=5,refcol2=6#_"
                     "http://clj-r2rml.test/Referenced/refcol1=2,refcol2=3#_"])
               (set (map (fn [triple] (:object triple)) reference-triples))))))

;;; Tests

;;; <#TriplesMap1>
;;;     a rr:TriplesMap;
;;;     rr:logicalTable "
;;;        Select ('_:Department' || deptno) AS deptid
;;;             , deptno
;;;             , dname
;;;             , loc
;;;          from dept
;;;        ";
;;;     rr:class xyz:dept;
;;;     rr:tableGraphIRI xyz:DeptGraph;
;;;     rr:subjectMap [ a rr:BlankNodeMap; rr:column "deptid";
;;;                     rr:InverseExpression "{alias.}deptno = substr({alias.}deptid,length('_:Department')+1)"];
;;;     rr:propertyObjectMap [ rr:property dept:deptno; rr:column "deptno"; rr:datatype xsd:positiveInteger ];
;;;     rr:propertyObjectMap [ rr:property dept:name; rr:column "dname" ];
;;;     rr:propertyObjectMap [ rr:property dept:location; rr:column "loc" ];
;;;     rr:propertyObjectMap [ rr:property dept:COMPANY; rr:constantValue "XYZ Corporation" ];
;;; .

(def test-spec
     [{:logical-table   "select concat('_:Department',deptno) AS deptid, deptno, dname, loc from Dept"
       :class           "xyz:dept"
       :table-graph-iri "xyz:DeptGraph"
       :subject-map     {:column "deptid"}
       :property-object-map [{:property "dept:deptno"
                              :column   "deptno"
                              :datatype "xsd:positiveInteger"}
                             {:property "dept:name"
                              :column   "dname"}
                             {:property "dept:location"
                              :column   "loc"}
                             {:property       "dept:COMPANY"
                              :constant-value "XYZ Corporation"}]}])

(deftest should-apply-a-mapping
      (let [*context* (make-context *db-spec* {})
            results (:results (run-mapping test-spec *context* {}))
            table-triples (filter (fn [triple] (= (:predicate triple) "rdf:type")) results)]
        (is (= (count results) 5))
        (is (= (count table-triples) 1))
        (is (= "xyz:dept" (:object (first table-triples))))))
