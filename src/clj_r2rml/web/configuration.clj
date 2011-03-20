(ns clj-r2rml.web.configuration
    (:use clj-r2rml.core))


(def *redis-server*  {:host "127.0.0.1" :port 6379 :db 0})
(def *redis-counter* "unique-id-int")

(def *rdf-ns*
     {:foaf "http://xmlns.com/foaf/0.1/"
      :vcard "http://www.w3.org/2006/vcard/ns#"
      :cv "http://rdfs.org/resume-rdf/"
      :cvapi "https://antoniogarrote.com/cvbuilder/api/"})

(def *db-spec*
     {:classname   "com.mysql.jdbc.Driver"
      :subprotocol "mysql"
      :user        "root"
      :password    ""
      :subname     "//localhost:3306/cvs_development"})

(def *sql-context* (make-context *db-spec* *rdf-ns*))

(def *server-base* "https://antoniogarrote.com/cvbuilder")


(def *test-resources*
     [;; Candidates
      {:_uri "https://antoniogarrote.com/cvbuilder/api/candidates"
       :type :Resource
       :resource-type "foaf:Person"
       :uriTemplate "https://antoniogarrote.com/cvbuilder/api/candidates"
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
                                                             :datatype "xsd:string"}
                                                            {:property "http://www.w3.org/2006/vcard/ns#email"
                                                             :column   "email"
                                                             :datatype "xsd:string"}]}
                  :has_r2rml_graph {:table-graph-iri "https://antoniogarrote.com/cvbuilder/api/candidates"}}
       :hasOperation [:GET :POST]
       :namedGraphCreationMechanism {:type :NamedGraphCreationMechanism
                                     :uriTemplate "https://antoniogarrote.com/cvbuilder/api/candidates/{id}"
                                     :mapped_uri_parts [{:mapped_component_value "id"
                                                         :uri_generator :BeautifyUri
                                                         :properties ["http://www.w3.org/2006/vcard/ns#given-name", "http://www.w3.org/2006/vcard/ns#family-name"]}]}}
      {:_uri "https://antoniogarrote.com/cvbuilder/api/candidates/{id}#self"
       :type :Resource
       :resource-type "foaf:Person"
       :uriTemplate "https://antoniogarrote.com/cvbuilder/api/candidates/{id}#self"
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
                                                             :datatype "xsd:string"}
                                                            {:property "http://www.w3.org/2006/vcard/ns#email"
                                                             :column   "email"
                                                             :datatype "xsd:string"}]}
                  :has_r2rml_graph {:column-graph "uri"}}
       :hasOperation [:GET :PUT :DELETE]}


      ;; Educations
      {:_uri "https://antoniogarrote.com/cvbuilder/api/candidates/{id}/educations"
       :type :Resource
       :resource-type "cv:Education"
       :uriTemplate "https://antoniogarrote.com/cvbuilder/api/candidates/{id}/educations"
       :mappedUriTemplate "https://antoniogarrote.com/cvbuilder/api/candidates/{id}#self"
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
                                                            {:property "http://rdfs.org/resume-rdf/courseDescription"
                                                             :column   "description"
                                                             :datatype "xsd:string"}
                                                            {:property "https://antoniogarrote.com/cvbuilder/api/studiedBy"
                                                             :column   "candidate"
                                                             :datatype "http://xmlns.com/foaf/0.1/Person"}]}
                  :has_r2rml_graph {:column-graph "candidate" }}
       :hasOperation [:GET :POST]
       :namedGraphCreationMechanism {:type :NamedGraphCreationMechanism
                                     :uriTemplate "https://antoniogarrote.com/cvbuilder/api/candidates/{id}/educations/{education_id}"
                                     :mapped_uri_parts [{:mapped_component_value "education_id"
                                                         :uri_generator :UniqueIdInt}
                                                        {:mapped_component_value "id"
                                                         :uri_generator :Params}]}}


      {:_uri "https://antoniogarrote.com/cvbuilder/api/candidates/{candidate_id}/educations/{id}#self"
       :type :Resource
       :resource-type "cv:Education"
       :uriTemplate "https://antoniogarrote.com/cvbuilder/api/candidates/{candidate_id}/educations/{id}#self"
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
                                                            {:property "http://rdfs.org/resume-rdf/courseDescription"
                                                             :column   "description"
                                                             :datatype "xsd:string"}
                                                            {:property "https://antoniogarrote.com/cvbuilder/api/studiedBy"
                                                             :column   "candidate"
                                                             :datatype "http://xmlns.com/foaf/0.1/Person"}]}
                  :has_r2rml_graph {:column-graph "uri"}}
       :hasOperation [:GET :PUT :DELETE]}

      ;; Organizations
      {:_uri "https://antoniogarrote.com/cvbuilder/api/organizations"
       :type :Resource
       :resource-type "cv:Organization"
       :uriTemplate "https://antoniogarrote.com/cvbuilder/api/organizations"
       :endPoint {:type :R2RMLMapping
                  :has_r2rml_mapping {:logical-table "organizations"
                                      :subject-map     {:column "uri"}
                                      :property-object-map [{:property "http://www.w3.org/2006/vcard/ns#organization-name"
                                                             :column   "name"
                                                             :datatype "xsd:string"}
                                                            {:property "http://xmlns.com/foaf/0.1/homepage"
                                                             :column   "homepage"
                                                             :datatype "xsd:string"}]}
                  :has_r2rml_graph {:table-graph-iri "https://antoniogarrote.com/cvbuilder/api/organizations"}}
       :hasOperation [:GET :POST]
       :namedGraphCreationMechanism {:type :NamedGraphCreationMechanism
                                     :uriTemplate "https://antoniogarrote.com/cvbuilder/api/organizations/{id}"
                                     :mapped_uri_parts [{:mapped_component_value "id"
                                                         :uri_generator :UniqueIdInt}]}}
      {:_uri "https://antoniogarrote.com/cvbuilder/api/organizations/{id}#self"
       :type :Resource
       :resource-type "cv:Organization"
       :uriTemplate "https://antoniogarrote.com/cvbuilder/api/organizations/{id}#self"
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
      {:_uri "https://antoniogarrote.com/cvbuilder/api/candidates/{id}/jobs"
       :type :Resource
       :resource-type "cv:WorkHistory"
       :uriTemplate "https://antoniogarrote.com/cvbuilder/api/candidates/{id}/jobs"
       :mappedUriTemplate "https://antoniogarrote.com/cvbuilder/api/candidates/{id}#self"
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
                                     :uriTemplate "https://antoniogarrote.com/cvbuilder/api/candidates/{id}/jobs/{job_id}"
                                     :mapped_uri_parts [{:mapped_component_value "job_id"
                                                         :uri_generator :UniqueIdInt}
                                                        {:mapped_component_value "id"
                                                         :uri_generator :Params}]}}
      {:_uri "https://antoniogarrote.com/cvbuilder/api/candidates/{candidate_id}/jobs/{id}#self"
       :type :Resource
       :resource-type "cv:WorkHistory"
       :uriTemplate "https://antoniogarrote.com/cvbuilder/api/candidates/{candidate_id}/jobs/{id}#self"
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
