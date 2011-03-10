(ns clj-r2rml.web.cvs-demo
  ;; for demo
  (:use clj-r2rml.core)
  (:use clj-r2rml.sparql-engine)
  (:use clj-r2rml.web.wiring)
  ;;
  (:use compojure.handler)
  (:use compojure.core ring.adapter.jetty)
  (:use [ring.middleware params
                         keyword-params
                         nested-params
                         multipart-params
                         cookies
                         session])
  (:require [compojure.route :as route]))

;; Demo

(def *ns*
     {:foaf "http://xmlns.com/foaf/0.1/"
      :vcard "http://www.w3.org/2006/vcard/ns#"
      :cv "http://rdfs.org/resume-rdf/"
      :cvapi "http://localhost:8080/api/"})

(def *db-spec*
     {:classname   "com.mysql.jdbc.Driver"
      :subprotocol "mysql"
      :user        "root"
      :password    ""
      :subname     "//localhost:3306/cvs_development"})

(def *test-resources*
     [;; Candidates
      {:_uri "http://localhost:8080/api/candidates"
       :type :Resource
       :uriTemplate "http://localhost:8080/api/candidates"
       :endPoint {:type :R2RMLMapping
                  :has_r2rml_mapping {:logical-table "candidates"
                                      :subject-map     {:column "uri"}
                                      :property-object-map [{:property "http://www.w3.org/2006/vcard/ns#given-name"
                                                             :column   "name"
                                                             :datatype "xsd:string"}
                                                            {:property "http://www.w3.org/2006/vcard/ns#family-name"
                                                             :column   "surname"
                                                             :datatype "xsd:string"}
                                                            {:property "http://www.w3.org/2006/vcard/ns#bday"
                                                             :column   "birthdate"
                                                             :datatype "xsd:date"}
                                                            {:property "http://www.w3.org/2006/vcard/ns#tel"
                                                             :column   "telephone"
                                                             :datatype "xsd:date"}
                                                            {:property "http://www.w3.org/2006/vcard/ns#adr"
                                                             :column   "address"
                                                             :datatype "xsd:string"}]}
                  :has_r2rml_graph {:table-graph-iri "http://localhost:8080/api/candidates"}}
       :hasOperation [:GET :POST]
       :namedGraphCreationMechanism {:type :NamedGraphCreationMechanism
                                     :uriTemplate "http://localhost:8080/api/candidates/{id}"
                                     :mapped_uri_parts [{:mapped_component_value "id"
                                                         :uri_generator :BeautifyUri
                                                         :properties ["http://www.w3.org/2006/vcard/ns#given-name", "http://www.w3.org/2006/vcard/ns#family-name"]}]}}
      {:_uri "http://localhost:8080/api/candidates/{id}#self"
       :type :Resource
       :uriTemplate "http://localhost:8080/api/candidates/{id}#self"
       :endPoint {:type :R2RMLMapping
                  :has_r2rml_mapping {:logical-table "candidates"
                                      :subject-map     {:column "uri"}
                                      :property-object-map [{:property "http://www.w3.org/2006/vcard/ns#given-name"
                                                             :column   "name"
                                                             :datatype "xsd:string"}
                                                            {:property "http://www.w3.org/2006/vcard/ns#family-name"
                                                             :column   "surname"
                                                             :datatype "xsd:string"}
                                                            {:property "http://www.w3.org/2006/vcard/ns#bday"
                                                             :column   "birthdate"
                                                             :datatype "xsd:date"}
                                                            {:property "http://www.w3.org/2006/vcard/ns#tel"
                                                             :column   "telephone"
                                                             :datatype "xsd:date"}
                                                            {:property "http://www.w3.org/2006/vcard/ns#adr"
                                                             :column   "address"
                                                             :datatype "xsd:string"}]}
                  :has_r2rml_graph {:column-graph "uri"}}
       :hasOperation [:GET :PUT :DELETE]}


      ;; Educations
      {:_uri "http://localhost:8080/api/candidates/{id}/educations"
       :type :Resource
       :uriTemplate "http://localhost:8080/api/candidates/{id}/educations"
       :mappedUriTemplate "http://localhost:8080/api/candidates/{id}#self"
       :endPoint {:type :R2RMLMapping
                  :has_r2rml_mapping {:logical-table "educations"
                                      :subject-map     {:column "uri"}
                                      :property-object-map [{:property "http://rdfs.org/resume-rdf/startDate"
                                                             :column   "start"
                                                             :datatype "xsd:date"}
                                                            {:property "http://rdfs.org/resume-rdf/endDate"
                                                             :column   "end"
                                                             :datatype "xsd:string"}
                                                            {:property "http://rdfs.org/resume-rdf/studiedIn"
                                                             :column   "institution"
                                                             :datatype "http://www.w3.org/2006/vcard/ns#Organization"}
                                                            {:property "http://rdfs.org/resume-rdf/degreeType"
                                                             :column   "titulation"
                                                             :datatype "xsd:string"}
                                                            {:property "http://test.com/api/studiedBy"
                                                             :column   "candidate"
                                                             :datatype "http://xmlns.com/foaf/0.1/Person"}]}
                  :has_r2rml_graph {:column-graph "candidate" }}
       :hasOperation [:GET :POST]
       :namedGraphCreationMechanism {:type :NamedGraphCreationMechanism
                                     :uriTemplate "http://localhost:8080/api/candidates/{id}/educations/{education_id}"
                                     :mapped_uri_parts [{:mapped_component_value "education_id"
                                                         :uri_generator :UniqueIdInt}
                                                        {:mapped_component_value "id"
                                                         :uri_generator :Params}]}}


      {:_uri "http://localhost:8080/api/candidates/{candidate_id}/educations/{id}#self"
       :type :Resource
       :uriTemplate "http://localhost:8080/api/candidates/{candidate_id}/educations/{id}#self"
       :endPoint {:type :R2RMLMapping
                  :has_r2rml_mapping {:logical-table "educations"
                                      :subject-map     {:column "uri"}
                                      :property-object-map [{:property "http://rdfs.org/resume-rdf/startDate"
                                                             :column   "start"
                                                             :datatype "xsd:date"}
                                                            {:property "http://rdfs.org/resume-rdf/endDate"
                                                             :column   "end"
                                                             :datatype "xsd:string"}
                                                            {:property "http://rdfs.org/resume-rdf/studiedIn"
                                                             :column   "institution"
                                                             :datatype "http://www.w3.org/2006/vcard/ns#Organization"}
                                                            {:property "http://rdfs.org/resume-rdf/degreeType"
                                                             :column   "titulation"
                                                             :datatype "xsd:string"}
                                                            {:property "http://test.com/api/studiedBy"
                                                             :column   "candidate"
                                                             :datatype "http://xmlns.com/foaf/0.1/Person"}]}
                  :has_r2rml_graph {:column-graph "uri"}}
       :hasOperation [:GET :PUT :DELETE]}

      ;; Organizations
      {:_uri "http://localhost:8080/api/organizations"
       :type :Resource
       :uriTemplate "http://localhost:8080/api/organizations"
       :endPoint {:type :R2RMLMapping
                  :has_r2rml_mapping {:logical-table "organizations"
                                      :subject-map     {:column "uri"}
                                      :property-object-map [{:property "http://www.w3.org/2006/vcard/ns#organization-name"
                                                             :column   "name"
                                                             :datatype "xsd:string"}
                                                            {:property "http://xmlns.com/foaf/0.1/homepage"
                                                             :column   "homepage"
                                                             :datatype "xsd:string"}]}
                  :has_r2rml_graph {:table-graph-iri "http://localhost:8080/api/organizations"}}
       :hasOperation [:GET :POST]
       :namedGraphCreationMechanism {:type :NamedGraphCreationMechanism
                                     :uriTemplate "http://localhost:8080/api/organizations/{id}"
                                     :mapped_uri_parts [{:mapped_component_value "id"
                                                         :uri_generator :UniqueIdInt}]}}
      {:_uri "http://localhost:8080/api/organizations/{id}#self"
       :type :Resource
       :uriTemplate "http://localhost:8080/api/organizations/{id}#self"
       :endPoint {:type :R2RMLMapping
                  :has_r2rml_mapping {:logical-table "organizations"
                                      :subject-map     {:column "uri"}
                                      :property-object-map [{:property "http://www.w3.org/2006/vcard/ns#organization-name"
                                                             :column   "name"
                                                             :datatype "xsd:string"}
                                                            {:property "http://xmlns.com/foaf/0.1/homepage"
                                                             :column   "homepage"
                                                             :datatype "xsd:string"}]}
                  :has_r2rml_graph {:column-graph "uri"}}
       :hasOperation [:GET]}
      
      ;; Jobs
      {:_uri "http://localhost:8080/api/candidates/{id}/jobs"
       :type :Resource
       :uriTemplate "http://localhost:8080/api/candidates/{id}/jobs"
       :mappedUriTemplate "http://localhost:8080/api/candidates/{id}#self"
       :endPoint {:type :R2RMLMapping
                  :has_r2rml_mapping {:logical-table "jobs"
                                      :subject-map     {:column "uri"}
                                      :property-object-map [{:property "http://rdfs.org/resume-rdf/startDate"
                                                             :column   "start_date"
                                                             :datatype "xsd:date"}
                                                            {:property "http://rdfs.org/resume-rdf/endDate"
                                                             :column   "end_date"
                                                             :datatype "xsd:string"}
                                                            {:property "http://rdfs.org/resume-rdf/employedIn"
                                                             :column   "company"
                                                             :datatype "http://www.w3.org/2006/vcard/ns#Company"}
                                                            {:property "http://rdfs.org/resume-rdf/jobTitle"
                                                             :column   "position"
                                                             :datatype "xsd:string"}
                                                            {:property "http://rdfs.org/resume-rdf/jobDescription"
                                                             :column   "description"
                                                             :datatype "xsd:string"}
                                                            {:property "http://rdfs.org/resume-rdf/heldBy"
                                                             :column   "candidate"
                                                             :datatype "http://xmlns.com/foaf/0.1/Person"}]}
                  :has_r2rml_graph {:column-graph "candidate" }}
       :hasOperation [:GET :POST]
       :namedGraphCreationMechanism {:type :NamedGraphCreationMechanism
                                     :uriTemplate "http://localhost:8080/api/candidates/{id}/jobs/{job_id}"
                                     :mapped_uri_parts [{:mapped_component_value "job_id"
                                                         :uri_generator :UniqueIdInt}
                                                        {:mapped_component_value "id"
                                                         :uri_generator :Params}]}}
      {:_uri "http://localhost:8080/api/candidates/{candidate_id}/jobs/{id}#self"
       :type :Resource
       :uriTemplate "http://localhost:8080/api/candidates/{candidate_id}/jobs/{id}#self"
       :endPoint {:type :R2RMLMapping
                  :has_r2rml_mapping {:logical-table "jobs"
                                      :subject-map     {:column "uri"}
                                      :property-object-map [{:property "http://rdfs.org/resume-rdf/startDate"
                                                             :column   "start_date"
                                                             :datatype "xsd:date"}
                                                            {:property "http://rdfs.org/resume-rdf/endDate"
                                                             :column   "end_date"
                                                             :datatype "xsd:string"}
                                                            {:property "http://rdfs.org/resume-rdf/employedIn"
                                                             :column   "company"
                                                             :datatype "http://rdfs.org/resume-rdf/Company"}
                                                            {:property "http://rdfs.org/resume-rdf/jobTitle"
                                                             :column   "position"
                                                             :datatype "xsd:string"}
                                                            {:property "http://rdfs.org/resume-rdf/jobDescription"
                                                             :column   "description"
                                                             :datatype "xsd:string"}
                                                            {:property "http://rdfs.org/resume-rdf/heldBy"
                                                             :column   "candidate"
                                                             :datatype "http://xmlns.com/foaf/0.1/Person"}]}
                  :has_r2rml_graph {:column-graph "uri" }}
       :hasOperation [:GET :PUT :DELETE]}
      
      ])


(defroutes main-routes
  (lda-description *test-resources* (clj-r2rml.sparql-engine.SqlSparqlEngine. (make-context *db-spec* *ns*) []))
  (GET "/" request (println (str "req >" (params request) "<")) (str "<h1>CV app. demo</h1> "))
  (route/not-found "<h1>Page not found</h1>"))

(defn wrap-max-overload
  "Always overload method"
  [handler & [opts]]
  (fn [request]
    (let [_ (println (str " *** !!! OVERLOADING " request))
          request (if-let [method (get (:params request) "_method")]
                    (assoc-in request [:form-params "_method"]  method)
                    request)]
      (handler request))))

(defn wrap-overload-content-type
  "Always overload method"
  [handler & [opts]]
  (fn [request]
    (let [request (if-let [format (get (:params request) "_format")]
                    (-> request
                        (assoc :content-type  (str "application/" format))
                        (assoc-in [:headers "content-type"] (str "application/" format)))
                    request)]
      (handler request))))

(defn my-api
  "Create a handler suitable for a web API. This adds the following
  middleware to your routes:
    - wrap-params
    - wrap-nested-params
    - wrap-keyword-params"
  [routes]
  (-> routes
      wrap-keyword-params
      wrap-nested-params
      wrap-max-overload
      wrap-overload-content-type
      wrap-params))

;(run-jetty  (my-api (var main-routes)) {:port 8080})


