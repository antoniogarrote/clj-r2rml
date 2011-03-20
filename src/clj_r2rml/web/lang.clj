(ns clj-r2rml.web.lang
  (:use compojure.core)
  (:require [compojure.route :as route]))

(defn uri-or-blank-id
  ([unit env]
     (if (nil? (:_uri unit))
       (let [id (swap! (:anonymous-counter env) inc)]
         (str "_:" id))
       (:_uri unit))))

(defn parse-r2rml-mapping
  ([endpoint env]
     (if (map? endpoint)
       (let [uri (uri-or-blank-id endpoint env)
             r2rml-mapping (:has_r2rml_mapping endpoint)
             _ (println (str "MAPPNG > " r2rml-mapping))
             graph-mapping (:has_r2rml_graph endpoint)
             _ (println (str "GRAPH MAPPING > " graph-mapping))
             mapping (merge r2rml-mapping graph-mapping)
             _ (println (str "merged mapping -> " mapping))]
         (swap! (:mappings env) (fn [m] (assoc m uri mapping)))
         uri)
       endpoint)))

(defn parse-named-graph-creation-mechanism
  ([minter env]
     (if (nil? minter) nil
         (if (map? minter)
           (let [uri (uri-or-blank-id minter env)
                 minter {:template (:uriTemplate minter)
                         :components (:mapped_uri_parts minter)}]
             (swap! (:creation-mechanisms env) (fn [m] (assoc m uri minter)))
             uri)))))

(defn parse-lda-resource
  ([resource env]
     (let [uri (uri-or-blank-id resource env)
           table-mapper (parse-r2rml-mapping (-> resource :endPoint) env)
           minter (parse-named-graph-creation-mechanism (-> resource :namedGraphCreationMechanism) env)
           resource {:template (:uriTemplate resource)
                     :mappedUriTemplate (:mappedUriTemplate resource)
                     :mapping table-mapper
                     :resource-type (:resource-type resource)
                     :minter minter
                     :operations (:hasOperation resource)}]
       (swap! (:resources env) (fn [m] (assoc m uri resource)))
       uri)))

(defn make-new-lda-parsing-env
  ([] {:resources (atom {})
       :mappings (atom {})
       :creation-mechanisms (atom {})
       :anonymous-counter (atom 0)}))

(defn parse-lda-description
  ([parsed-description]
     (let [env (make-new-lda-parsing-env)]
       (doseq [unit parsed-description]
         (condp = (:type unit)
             :Resource (parse-lda-resource unit env)
             :R2RMLMapping (parse-r2rml-mapping unit env)
             :NamedGraphCreationMechanism (parse-named-graph-creation-mechanism unit env)
             (throw (Exception. (str "Unknown LDA mapping component of type" (:type unit))))))
       env)))
