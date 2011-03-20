(ns clj-r2rml.web.cvs-demo
  ;; for demo
  (:use clj-r2rml.sparql-engine)
  (:use clj-r2rml.web.wiring)
  (:use clj-r2rml.web.xhtml)
  (:use clj-r2rml.web.jetty-foaf-ssl)
  (:use clj-r2rml.web.configuration)
  ;;
  (:use compojure.handler)
  (:use compojure.route)
  (:use compojure.core)
  ;(:use compojure.core ring.adapter.jetty)
  (:use [ring.middleware params
                         keyword-params
                         nested-params
                         multipart-params
                         cookies
                         content-type
                         file-info
                         session])
  (:require [compojure.route :as route]))

;; Demo

(defn- add-wildcard
  "Add a wildcard to the end of a route path."
  [path]
  (str path (if (.endsWith path "/") "*" "/*")))

(defn my-files
  "A route for serving static files from a directory. Accepts the following
keys:
:root - the root path where the files are stored. Defaults to 'public'."
  [path & [options]]
  (-> (GET (add-wildcard path) {{file-path :*} :route-params}
           (let [options (merge {:root "public"} options)]
          (ring.util.response/file-response (second file-path) options)))
      (wrap-file-info (:mime-types options))))


(defroutes main-routes
  (GET "/cvbuilder/cvs/:id" request {:status 200 :headers {"Charset" "ISO-8859-1"} :body (build-xhtml-cv request *sql-context* *server-base*)})
  (GET "/cvbuilder/api/cvs" request (build-acl-check-triples request "foaf:Person" *sql-context* *rdf-ns*))
  (lda-description *test-resources* (clj-r2rml.sparql-engine.SqlSparqlEngine. *sql-context* []))
  (my-files "*"))

(defn wrap-max-overload
  "Always overload method"
  [handler & [opts]]
  (fn [request]
    (let [request (if-let [method (get (:params request) "_method")]
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

(run-jetty  (my-api (var main-routes)) {:port 8080 :ssl-port 8443 :keystore "./keystore" :key-password "Nb9548xK"})
