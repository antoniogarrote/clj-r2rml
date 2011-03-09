(ns clj-r2rml.web.wiring
  (:use clj-r2rml.sparql-engine)
  (:use clj-r2rml.sparql-update)
  (:use clj-r2rml.turtle-parser)
  (:use clj-r2rml.triples)
  (:use clj-r2rml.web.lang)
  (:use clj-r2rml.web.formatting)
  (:use compojure.core ring.adapter.jetty)
  (:use ring.middleware.params)
  (:require [compojure.route :as route])
  (:require [compojure.core :as compojure]))

(defn wrap-get-override [handler]
    (wrap-params
     (fn [request]
        (if-let [method ((:params request) "_method")]
          (let [method (keyword (.toLowerCase method))]
            (handler (assoc request :request-method method)))
          (handler request)))))

(defn params
  ([request]
     (let [query-string (or (:query-string request) "")
           parts (clojure.contrib.string/split #"&" query-string)]
       (reduce (fn [ac it]
                 (let [[lvalue rvalue] (clojure.contrib.string/split #"=" it)]
                   (if (not (nil? rvalue))
                       (assoc ac lvalue rvalue)
                       ac)))
               (reduce (fn [ac [k v]] (assoc ac (safe-name k) v)) {} (or (:params request) {}))
               parts))))

(defn from-env
  ([env k id]
     (println (str "from env " env " key " k " id " id))
     (get (deref (k env)) id)))

(defn extract-path-from-uri
  ([uri]
     (let [minus-scheme (clojure.contrib.string/join "/" (rest (clojure.contrib.string/split #"//" uri)))
           path (clojure.contrib.string/join "/" (rest (clojure.contrib.string/split #"/" minus-scheme)))]
       (str "/" path))))

(defn path-to-compojure-template
  ([path-template]
     (let [path (->> path-template
                     (clojure.contrib.string/replace-str "{" ":")
                     (clojure.contrib.string/replace-str "}" ""))
           parts (clojure.contrib.string/split #"#" path)]
       (first parts))))

(defn template-to-graph-uri
  ([uri-template args]
     (println (str "hey -> " args " vs " uri-template))
     (reduce (fn [uri [k v]]
               (println (str "replacing " k " in " v " str: " uri))
               (clojure.contrib.string/replace-str (str "{" (name k) "}") (str v) uri))
             uri-template
             args)))

(defn select-content-type
  ([request]
     (println (str "PARAMS: " request))
     (println (params request))
     (if (blank? (get (params request) "_format"))
       (let [content-type (or (or (get (:headers request) "content-type")
                                  (get (:headers request) "Content-Type")) "*/*")
             format (second (clojure.contrib.string/split #"/" content-type))
             format (first (clojure.contrib.string/split #";" format))]
         (keyword format))
       (keyword (get (params request) "_format")))))

(defn parse-body
  ([body media-type]
     (condp = media-type
         :ttl (turtle-doc-to-triples body)
         :turtle (turtle-doc-to-triples body)
         :js  (parse-json-ld body)
         :json  (parse-json-ld body)
         (throw (Exception. "Unsupported document type")))))

(defn format-response
  ([triples media-type request nss]
     (println (str "MEDIA TYPE " media-type))
     (condp = (name media-type)
         "json" (to-json-ld triples nss (get (params request) "_callback"))
         "js" (to-json-ld triples nss (get (params request) "_callback"))
         triples)))

(defn resource-get
  ([request resource env sparql-engine]
     (let [_ (println (str "REQUEST: " request))
           _ (println (str "PARAMS: " (:params request)))
           _ (println (str "PARAMS2: " (params request)))
           _ (println (str "template " (:template resource)))
           media-type (select-content-type request)
           table-mapper (make-table-mapper (from-env env :mappings (:mapping resource)))
           _ (println (str "TABLE MAPPING" (from-env env :mappings (:mapping resource))))
           sparql-engine (assoc sparql-engine :table-mappers [table-mapper])
           graph (template-to-graph-uri (or (:mappedUriTemplate resource) (:template resource)) (or (params request) {}))
           _ (println (str "WITH GRAPH " graph " -> " (str "SELECT ?s ?p ?o { GRAPH <" graph "> { ?s ?p ?o } }")))
           triples (execute sparql-engine (str "SELECT ?s ?p ?o { GRAPH <" graph "> { ?s ?p ?o } }"))]
       {:status 200
        :body (format-response triples media-type request (:namespaces (:sql-backend sparql-engine)))})))

(defn blank-node-id?
  ([t] (= (safe-name (:token t)) "blank")))

(defn valid-triples?
  ([triples]
     (every? blank-node-id? (map first triples))))

(defn valid-triples-update?
  ([triples]
     (not-any? blank-node-id? (map first triples))))

(defn valid-chars
  ([string]
     (loop [s (clojure.contrib.string/lower-case string)
            ac []]
       (if (empty? s)
         (clojure.contrib.string/join "" ac)
         (let [c (first s)]
           (if (and (>= (int c) (int \a)) (<= (int c) (int \z)))
             (recur (rest s)
                    (conj ac c))
             (recur (rest s)
                    ac)))))))

(defn beautify-uri
  ([props triples]
     (loop [uri ""
            props props]
       (if (empty? props)
         (str uri (int (rand 1000000)))
         (let [prop (first props)
               val (str  (valid-chars (:value (nth  (first (filter (fn [[s p o g]] (= (:value p) prop)) triples)) 2))) "-")]
           (recur (str uri val)
                  (rest props)))))))

(defn run-minter
  ([minter resource params triples]
     (let [params-p (reduce (fn [ps comp]
                              (let [_ (println (str "PARAMS: " params))
                                    value (condp = (:uri_generator comp)
                                              :UniqueIdInt (int (rand 1000000)) ; @todo, this is temporal
                                              :BeautifyUri (beautify-uri (:properties comp) triples)
                                              :Params (get params (:mapped_component_value comp))
                                              (throw (str "Minter URI generator not supported " comp)))]
                                (assoc ps (:mapped_component_value comp) value)))
                            params
                            (:components minter))]
       (template-to-graph-uri (:template minter) params-p))))


(defn generate-graph-uri
  ([resource request env triples]
     (let [params (params request)
           minter (from-env env :creation-mechanisms (:minter resource))]
       (run-minter minter resource params triples))))


(defn build-sparql-insert-data
  ([quads env]
     (if (> (count quads) 0)
       (str (reduce (fn [sparql t]
                      (println (str "T: " t))
                      (str sparql
                           " " (normalize-term-string (nth t 0) env)
                           " " (normalize-term-string (nth t 1) env)
                           " " (normalize-term-string (nth t 2) env)
                           "."))
                    (str "INSERT DATA { GRAPH " (normalize-term-string (nth  (first quads) 3) env) " { ")
                    quads) " } }")
       "")))

(defn build-sparql-update-data
  ([graph-uri quads env]
     (str "DELETE WHERE { GRAPH <" graph-uri  "> {?s ?p ?o} };"
          (build-sparql-insert-data quads env))))

(defn build-sparql-delete-data
  ([graph-uri env]
     (str "DELETE WHERE { GRAPH <" graph-uri  "> {?s ?p ?o} };")))

(defn resource-post
  ([request resource env sparql-engine]
     (let [body (clojure.contrib.io/slurp* (:body request))
           media-type (select-content-type request)
           _ (println (str "media type " media-type))
           triples (parse-body body media-type)
           table-mapper (make-table-mapper (from-env env :mappings (:mapping resource)))
           sparql-engine (assoc sparql-engine :table-mappers [table-mapper])
           _ (println (str "TRIPLES: " (vec triples)))]
       (if (valid-triples? triples)
         (let [uri (generate-graph-uri resource request env triples)
               graph-uri (template-to-graph-uri (or (:mappedUriTemplate resource) (:template resource)) (or (params request) {}))
               _ (println (str "GRAPH " graph-uri " -> " (or (:mappedUriTemplate resource) (:template resource)) " vs " resource))
               subject-uri (str uri "#self")
               triples-p (map (fn [[s p o g]] [{:token "uri" :value subject-uri} p o {:token "uri" :value graph-uri}]) triples)
               _ (println (str "TRIPLESP " (vec triples-p)))
               sparql (build-sparql-insert-data triples-p env)
               _ (println (str "SPARQL: " sparql))
               _ (println (str "ENGINE: " (:table-mappers  sparql-engine)))
               rows (execute sparql-engine sparql)
               _ (println (str "RESULTS: " rows))]
           (if (> rows 0)
             {:status 201
              :headers {"Location" uri}
              :body uri}
             {:status 400
              :body "invalid request"}))
         {:status 400
          :body "invalid request"}))))

(defn resource-put
  ([request resource env sparql-engine]
     (let [body (clojure.contrib.io/slurp* (:body request))
           media-type (select-content-type request)
           _ (println (str "media type " media-type))
           _ (println (str "BODY: " body))
           triples (parse-body body media-type)
           table-mapper (make-table-mapper (from-env env :mappings (:mapping resource)))
           sparql-engine (assoc sparql-engine :table-mappers [table-mapper])
           _ (println (str "TRIPLES: " (vec triples)))]
       (if (valid-triples-update? triples)
         (let [uri (first (clojure.contrib.string/split #"#self" (:value (first (first triples)))))
               graph-uri (template-to-graph-uri  (or (:mappedUriTemplate resource) (:template resource)) (or (params request) {}))
               subject-uri (str uri "#self")
               triples-p (map (fn [[s p o g]] [s p o {:token "uri" :value graph-uri}]) triples)
               _ (println (str "GRAPHS: uri " uri " subject-uri " subject-uri " graph uri " graph-uri ))]
           (if (= graph-uri subject-uri)
             (let [sparql (build-sparql-update-data graph-uri triples-p env)
                   _ (println (str "SPARQL: " sparql))
                   _ (println (str "ENGINE: " (:table-mappers  sparql-engine)))
                   rows (execute sparql-engine sparql)
                   _ (println (str "RESULTS: " rows))]
               (if (> rows 0)
                 {:status 200
                  :headers {"Location" subject-uri}
                  :body subject-uri}
                 {:status 400
                  :body "invalid request"}))
             {:status 400
              :body "invalid request"}))
         {:status 400
          :body "invalid request"}))))

(defn resource-delete
  ([request resource env sparql-engine]
     (let [media-type (select-content-type request)
           _ (println (str "media type " media-type))
           table-mapper (make-table-mapper (from-env env :mappings (:mapping resource)))
           sparql-engine (assoc sparql-engine :table-mappers [table-mapper])
           graph-uri (template-to-graph-uri  (or (:mappedUriTemplate resource) (:template resource)) (or (params request) {}))]
       (let [sparql (build-sparql-delete-data graph-uri env)
             _ (println (str "SPARQL: " sparql))
             _ (println (str "ENGINE: " (:table-mappers  sparql-engine)))
             rows (execute sparql-engine sparql)
             _ (println (str "RESULTS: " rows))]
         {:status 200 :body "deleted"}))))


(defn lda-resource
  ([resource env sparql-engine]
     (let [uri (:template resource)
           path (extract-path-from-uri uri)
           compojure-path (path-to-compojure-template path)]
       (map (fn [operation]
              (condp = operation
                  :GET (GET compojure-path request (resource-get request resource env sparql-engine))
                  :POST (POST compojure-path request (resource-post request resource env sparql-engine))
                  :PUT (PUT compojure-path request (resource-put request resource env sparql-engine))
                  :DELETE (DELETE compojure-path request (resource-delete request resource env sparql-engine))))
            (:operations resource)))))


(defn lda-description
  ([description sparql-engine]
     (let [parsed-description (parse-lda-description description)]
       #(apply compojure/routing %
               (flatten (map (fn [resource] (lda-resource resource parsed-description sparql-engine))
                             (vals (deref (:resources parsed-description)))))))))
