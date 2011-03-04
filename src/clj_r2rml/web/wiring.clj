(ns clj-r2rml.web.wiring
  (:use clj-r2rml.sparql-engine)
  (:use clj-r2rml.sparql-update)
  (:use clj-r2rml.web.lang)
  (:use compojure.core ring.adapter.jetty)
  (:require [compojure.route :as route])
  (:require [compojure.core :as compojure]))

(defn from-env
  ([env k id]
     (get (deref (k env)) id)))

(defn extract-path-from-uri
  ([uri]
     (let [minus-scheme (clojure.string/join "/" (rest (clojure.string/split uri #"//")))
           path (clojure.string/join "/" (rest (clojure.string/split minus-scheme #"/")))]
       (str "/" path))))

(defn path-to-compojure-template
  ([path-template]
     (-> path-template
         (clojure.string/replace "{" ":")
         (clojure.string/replace "}" ""))))

(defn template-to-graph-uri
  ([uri-template args]
     (println (str "hey -> " args " vs " uri-template))
     (reduce (fn [uri [k v]]
               (println (str "replacing " k " in " v))
               (clojure.string/replace uri (str "{" k "}") (str v)))
             uri-template
             args)))

(defn resource-get
  ([args resource env sparql-engine]
     (let [_ (println (str "template " (:template resource)))
           table-mapper (make-table-mapper (from-env env :mappings (:mapping resource)))
           _ (println (str "TABLE MAPPING" (from-env env :mappings (:mapping resource))))
           minter (from-env env :creation-mechanisms (:minter resource))
           _ (println (str "SPARQL ENGINE " sparql-engine))
           sparql-engine (assoc sparql-engine :table-mappers [table-mapper])
           graph (template-to-graph-uri (:template resource) (or args {}))
           _ (println (str "WITH GRAPH " graph " -> " (str "SELECT ?s ?p ?o { GRAPH <" graph "> { ?s ?p ?o } }")))]
       (str (execute sparql-engine (str "SELECT ?s ?p ?o { GRAPH <" graph "> { ?s ?p ?o } }"))))))

(defn resource-post
  ([args resource env sparql-engine] "<h1> POST to implement</h1>"))

(defn resource-put
  ([args resource env sparql-engine] "<h1> PUT to implement</h1>"))

(defn resource-delete
  ([args resource env sparql-engine] "<h1> DELETE to implement</h1>"))

(defn lda-resource
  ([resource env sparql-engine]
     (let [uri (:template resource)
           path (extract-path-from-uri uri)
           compojure-path (path-to-compojure-template path)]
       (map (fn [operation]
              (condp = operation
                  :GET (GET compojure-path [args] (resource-get args resource env sparql-engine))
                  :POST (POST compojure-path [args] (resource-get args resource env sparql-engine))
                  :PUT (PUT compojure-path [args] (resource-get args resource env sparql-engine))
                  :DELETE (DELETE compojure-path [args] (resource-get args resource env sparql-engine))))
            (:operations resource)))))

(defn lda-description
  ([description sparql-engine]
     (let [parsed-description (parse-lda-description description)]
       #(apply compojure/routing %
               (flatten (map (fn [resource] (lda-resource resource parsed-description sparql-engine))
                             (vals (deref (:resources parsed-description)))))))))
