(ns clj-r2rml.test.web.wiring
  (:use [clj-r2rml.web.wiring] :reload)
  (:use [clojure.test])
  (:use [clojure.pprint]))

(deftest test-paths-templates
  (is (= (path-to-compojure-template
          (extract-path-from-uri "http://test.com/api/blogs/{id}"))
         "/api/blogs/:id")))


(deftest test-template-to-graph-uri
  (is "/hola/1/mundo/test.html"
      (template-to-graph-uri "/hola/{id}/mundo/{mundo}.html"
                             {"id" 1 "mundo" "test"})))
