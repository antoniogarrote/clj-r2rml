(ns clj-r2rml.test.sparql-engine
  (:use [clj-r2rml.sparql-engine] :reload)
  (:use [clj-r2rml.sparql-parser] :reload)
  (:use [clj-r2rml.sparql-update] :reload)
  (:use [clj-r2rml.core] :reload)
  (:use clojure.contrib.sql)
  (:use clojure.contrib.sql.internal)
  (:use [clojure.test])
  (:use [clojure.pprint]))

(def *db-spec*
     {:classname   "com.mysql.jdbc.Driver"
      :subprotocol "mysql"
      :user        "root"
      :password    ""
      :subname     "//localhost:3306/rdftests"})

(def *test-mapping-blogs*
     [{:logical-table "Blogs"
       :class         "http://test.com/Blog"
;       :table-graph-iri "http://test.com/TestGraph"
       :subject-map     {:column "id"}
       :property-object-map [{:property "http://test.com/title"
                              :column   "title"
                              :datatype "xsd:String"}]}
      {:logical-table "Posts"
       :class         "http://test.com/Post"
;       :table-graph-iri "http://test.com/TestGraph"
       :subject-map     {:column "id"}
       :property-object-map [{:property "http://test.com/title"
                              :column   "title"
                              :datatype "xsd:String"}
                             {:property "http://test.com/content"
                              :column   "body"
                              :datatype "xsd:String"}
                             {:property "http://test.com/inBlog"
                              :column   "blog_id"}]}])


;;(deftest test-make-ns-env
;;  (let [query {:token "query", :kind "update"
;;               :prologue {:token "prologue"
;;                          :base {:token "base", :value "a"}
;;                          :prefixes [{:token "prefix", :prefix "test", :local "b"}]}
;;               :units [{:kind "insertdata", :token "executableunit"
;;                        :quads [{:subject {:token "uri", :prefix nil, :suffix nil, :value "a"}
;;                                 :predicate {:token "uri", :prefix nil, :suffix nil, :value "b"}
;;                                 :object {:token "uri", :prefix nil, :suffix nil, :value "c"}
;;                                 :graph nil}]}]}
;;        env (make-ns-env query)]
;;    (is (= "a" (:default (:ns env))))
;;    (is (= {"test" "b"} (:prefixes (:ns env))))))
;;
;;
;;(deftest test-update-to-triples-simple
;;  (let [env {:ns {:default "a", :prefixes {"test" "b"}}}
;;        unit {:kind "insertdata", :token "executableunit"
;;              :quads [{:subject {:token "uri", :prefix nil, :suffix nil, :value "a"}
;;                       :predicate {:token "uri", :prefix nil, :suffix nil, :value "b"}
;;                       :object {:token "uri", :prefix nil, :suffix nil, :value "c"}
;;                       :graph nil}]}]
;;    (is (= '(["a" "b" "c" nil])
;;           (update-unit-to-triples unit env)))))
;;
;;(deftest test-update-to-triples-simple-2
;;  (let [env {:ns {:default "a", :prefixes {"test" "b"}}}
;;        unit {:kind "insertdata", :token "executableunit"
;;              :quads [{:subject {:token "uri", :prefix nil, :suffix nil, :value "a"}
;;                       :predicate {:token "uri", :prefix "test", :suffix "b", :value nil}
;;                       :object {:token "uri", :prefix nil, :suffix nil, :value "c"}
;;                       :graph nil}]}]
;;    (is (= '(["a" "bb" "c" nil])
;;           (update-unit-to-triples unit env)))))
;;
;;(deftest test-update-to-triples-simple-3
;;  (let [env {:ns {:default "a", :prefixes {"test" "b"}}}
;;        unit {:kind "insertdata", :token "executableunit"
;;              :quads [{:subject {:token "uri", :prefix nil, :suffix nil, :value "a"}
;;                       :predicate {:token "uri", :prefix "test", :suffix "b", :value nil}
;;                       :object {:token "uri", :prefix "", :suffix "", :value nil}
;;                       :graph nil}]}]
;;    (is (= '(["a" "bb" "a" nil])
;;           (update-unit-to-triples unit env)))))

;;"PREFIX test: <http://test.com/Blog> SELECT ?s { ?s test:title \"c\" }"
(deftest test-run-insert-query
  (let [table-mappers (map make-table-mapper *test-mapping-blogs*)
        query "PREFIX test: <http://test.com/> PREFIX blog: <http://test.com/Blog/> INSERT DATA { blog:1 test:title \"c\" }"
        ctx (make-context *db-spec* {})
        engine (clj-r2rml.sparql-engine.SqlSparqlEngine. ctx table-mappers)]
    (with-context-connection ctx
      (do-commands "delete from Blogs;" "delete from Posts;"))
    (execute engine query)
    (let [res (execute engine "SELECT ?s { ?s ?p \"c\" }")]
      (is (= "http://test.com/Blog/1" (:s (first res))))
      (println (str "RESULT: " (vec res))))))
