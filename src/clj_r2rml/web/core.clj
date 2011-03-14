(ns clj-r2rml.web.core
  ;; for demo
  (:use clj-r2rml.core)
  (:use clj-r2rml.sparql-engine)
  (:use clj-r2rml.web.wiring)
  ;;
  (:use compojure.core ring.adapter.jetty)
  (:require [compojure.route :as route]))

;; Demo

(def *rdf-ns*
     {:blogs "http://test.com/api/blogs/"})

(def *db-spec*
     {:classname   "com.mysql.jdbc.Driver"
      :subprotocol "mysql"
      :user        "root"
      :password    ""
      :subname     "//localhost:3306/rdftests"})

(def *test-resources*
     [{:_uri "http://test.com/api/blogs"
       :type :Resource
       :uriTemplate "http://test.com/api/blogs"
       :endPoint {:type :R2RMLMapping
                  :has_r2rml_mapping {:logical-table "Blogs"
                                      :subject-map     {:column "id"}
                                      :property-object-map [{:property "http://test.com/api/blogs/title"
                                                             :column   "title"
                                                             :datatype "xsd:String"}]}
                  :has_r2rml_graph {:table-graph-iri "http://test.com/api/blogs"}}
       :hasOperation [:GET :POST]
       :namedGraphCreationMechanism {:type :NamedGraphCreationMechanism
                                     :uriTemplate "http://test.com/api/blogs/{id}"
                                     :mapped_uri_parts [{:mapped_component_value "id"
                                                         :uri_generator :UniqueIdInt}]}}
      {:_uri "http://test.com/api/blogs/{id}#self"
       :type :Resource
       :uriTemplate "http://test.com/api/blogs/{id}#self"
       :endPoint {:type :R2RMLMapping
                  :has_r2rml_mapping {:logical-table "Blogs"
                                      :subject-map     {:column "id"}
                                      :property-object-map [{:property "http://test.com/api/blogs/title"
                                                             :column   "title"
                                                             :datatype "xsd:String"}]}
                  :has_r2rml_graph {:column-graph "id"}}
       :hasOperation [:GET :PUT :DELETE]}])


(defn function-test
  ([] (GET "/hey" [] "<h1>hey</h1>")))

(defroutes main-routes
  (lda-description *test-resources* (clj-r2rml.sparql-engine.SqlSparqlEngine. (make-context *db-spec* *rdf-ns*) []))
  (GET "/" request (println (str "req >" (params request) "<")) (str "<h1>Hello World3</h1> "))
  (function-test)
  (route/not-found "<h1>Page not found</h1>"))

;(run-jetty (var main-routes) {:port 8080})
