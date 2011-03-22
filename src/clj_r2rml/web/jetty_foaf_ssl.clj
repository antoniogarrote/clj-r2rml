(ns clj-r2rml.web.jetty-foaf-ssl
  "Adapter for the Jetty webserver handling FOAF-SSL authentications."
  (:import (org.mortbay.jetty.handler AbstractHandler)
           (org.mortbay.jetty Server Request Response)
           (org.mortbay.jetty.bio SocketConnector)
           ;; Modified version of SSLSocketConnector accepting
           ;; client certificates not associated to a valid authority
           (cljr2rml.web FOAFSSLSocketConnector)
           (javax.servlet.http HttpServletRequest HttpServletResponse)
           (com.hp.hpl.jena.rdf.model ModelFactory)
           (com.hp.hpl.jena.rdf.model Model)
           (com.hp.hpl.jena.query QueryFactory Query QueryExecutionFactory))
  (:use clj-r2rml.web.configuration)
  (:require [ring.util.servlet :as servlet])
  (:require redis))

;; Cache for certificates

(defn store-cert-in-cache
  ([uri modulus exp]
     (redis/with-server *redis-server*
       (redis/set uri (str modulus ":" exp)))))

(defn cert-in-cache?
  ([uri]
     (redis/with-server *redis-server*
       (let [match (redis/get uri)]
         (if (nil? match)
           nil
           (let [[modulus exp] (vec (.split match ":"))]
             {:?exp exp :?modulus modulus}))))))


(defn foaf-ssl-sparql-query
  "A SPARQL query to retrieve the modulus and public exponent of the FOAF-SSL certificate"
  ([uri]
     (str "PREFIX cert: <http://www.w3.org/ns/auth/cert#>
PREFIX rsa: <http://www.w3.org/ns/auth/rsa#>
SELECT ?modulus ?exp
WHERE {
   ?key cert:identity <" uri ">;
        a rsa:RSAPublicKey;
        rsa:modulus [ cert:hex ?modulus; ];
        rsa:public_exponent [ cert:decimal ?exp ] .
}")))

(defn- process-model-query-result
  "Transforms a query result into a dicitionary of bindings"
  ([model result]
     (let [vars (iterator-seq (.varNames result))]
       (reduce (fn [acum item] (assoc acum (keyword (str "?" item)) (str (.get result item)))) {} vars))))

(defn extract-modulus-exp
  ([uri model]
     (let [query (foaf-ssl-sparql-query uri)
           query-factory  (QueryFactory/create query)
           query-execution-factory (QueryExecutionFactory/create query-factory model)]
       (map (fn [res] (process-model-query-result model res)) (iterator-seq (.execSelect query-execution-factory))))))


(defn deref-foaf-graph
  "Tries to retry the FOAF RDF graph and extract the modulus and exponent
   values associated to the declared certificate"
  ([uri]
     (let [model (cert-in-cache? uri)]
       (if model [model nil]
           (let [m (ModelFactory/createDefaultModel)]
             (.read m uri)
             (let [modulus-exp (first (extract-modulus-exp uri m))]
               (store-cert-in-cache uri (:?modulus modulus-exp) (:?exp modulus-exp))
               [modulus-exp m]))))))

(defn check-foaf-ssl-cert
  "Checks if the provided certificate is associated to the claimed WebID"
  ([cert]
     (try
       (let [uri (second (first (vec (.getSubjectAlternativeNames (aget cert 0)))))
             public-key (.getPublicKey (aget cert 0))
             exp (.getPublicExponent public-key)
             modulus (.getModulus public-key)
             [modulus-exp foaf-graph] (deref-foaf-graph uri)
             read-exp (:?exp modulus-exp)
             read-modulus (BigInteger. (:?modulus modulus-exp) 16)
             same-exp (= (str exp) (str read-exp))
             same-modulus (= modulus read-modulus)]
         (if (and same-exp same-modulus)
           [uri foaf-graph]  nil))
       (catch Exception ex
         (do (.printStackTrace ex)
             nil)))))


(defn- proxy-handler
  "Returns an Jetty Handler implementation for the given Ring handler."
  [handler]
  (proxy [AbstractHandler] []
    (handle [target ^Request request response dispatch]
      (let [cert (.getAttribute request "javax.servlet.request.X509Certificate")]
        (if cert
          (let [foaf-ssl-result (check-foaf-ssl-cert cert)
                request-map  (servlet/build-request-map request)]
            (if (nil? foaf-ssl-result)
              {:status 400
               :body "not a valid FOAF+SSL request"}
              (let [[webid foaf-graph] foaf-ssl-result
                    augmented-request-map (-> request-map
                                              (assoc :webid webid)
                                              (assoc :foaf foaf-graph))
                    response-map (handler augmented-request-map)]
                (when response-map
                  (servlet/update-servlet-response response response-map)
                  (.setHandled request true)))))

          (let [request-map (servlet/build-request-map request)
                augmented-request-map (-> request-map
                                          (assoc :webid nil)
                                          (assoc :foaf nil))
                response-map (handler augmented-request-map)]
            (when response-map
              (servlet/update-servlet-response response response-map)
              (.setHandled request true))))))))

(defn- add-ssl-connector!
  "Add an SslSocketConnector to a Jetty Server instance."
  [^Server server options]
  (let ;[ssl-connector (SslSocketConnector.)]
      [ssl-connector (FOAFSSLSocketConnector.)]
    (doto ssl-connector
      (.setPort        (options :ssl-port 443))
      (.setKeystore    (options :keystore))
      (.setKeyPassword (options :key-password))
      (.setWantClientAuth true))
    (when (options :truststore)
      (.setTruststore ssl-connector (options :truststore)))
    (when (options :trust-password)
      (.setTrustPassword ssl-connector (options :trust-password)))
    (.addConnector server ssl-connector)))

(defn- create-server
  "Construct a Jetty Server instance."
  [options]
  (let [connector (doto (SocketConnector.)
                    (.setPort (options :port 80))
                    (.setHost (options :host)))
        server    (doto (Server.)
                    (.addConnector connector)
                    (.setSendDateHeader true))]
    (when (or (options :ssl?) (options :ssl-port))
      (add-ssl-connector! server options))
    server))

(defn ^Server run-jetty
  "Serve the given handler according to the options.
  Options:
    :configurator   - A function called with the Server instance.
    :port
    :host
    :join?          - Block the caller: defaults to true.
    :ssl?           - Use SSL.
    :ssl-port       - SSL port: defaults to 443, implies :ssl?
    :keystore
    :key-password
    :truststore
    :trust-password"
  [handler options]
  (let [^Server s (create-server (dissoc options :configurator))]
    (when-let [configurator (:configurator options)]
      (configurator s))
    (doto s
      (.addHandler (proxy-handler handler))
      (.start))
    (when (:join? options true)
      (.join s))
    s))
