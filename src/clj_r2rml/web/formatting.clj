(ns clj-r2rml.web.formatting
  (:use clj-r2rml.triples)
  (:use [clojure.contrib.json :only [read-json]]))

(defn make-node
  ([[s p o _g]]
     (atom {"@" (:value s)
            (:value p) (:value o)})))

(defn update-node
 ([node [s p o g]]
    (swap! node (fn [m] (assoc m (:value p) (:value o))))))

(defn check-node
  ([nodes [s p o g]]
     (first (filter (fn [n] (= (get @n "@") (:value s))) nodes))))

(defn to-nodes
  ([triples]
     (reduce (fn [nodes triple]
               (let [triple (vals triple)]
                 (if-let [node (check-node nodes triple)]
                   (do (update-node node triple)
                       nodes)
                   (conj nodes (make-node triple)))))
             [] triples)))

(defn update-ns-term
  ([term nss]
     (loop [nss nss]
       (if (empty? nss)
         term
         (let [[ns uri] (first nss)]
           (if (= 0 (.indexOf (safe-name term) (safe-name uri)))
             [(safe-name ns) (second (clojure.contrib.string/split (re-pattern (safe-name uri)) (safe-name term)))]
             (recur (rest nss))))))))


(defn update-ns
  ([nodes ns]
     (doseq [n nodes]
       (println (str "node: " @n))
       (swap! n (fn [m] (reduce (fn [ac [k v]]  (assoc ac k v)) {} (map (fn [[k v]] [(update-ns-term k ns) (update-ns-term v ns)]) m)))))
     nodes))

(defn compact-ns
  ([nodes ns-orig]
     (for [node nodes]
       (let [nss (atom {})
             nodep (reduce (fn [ac [k v]]
                             (if (and (coll? k) (coll? v))
                               (do (swap! nss (fn [m]
                                                (assoc (assoc m (first k) ((keyword (first k)) ns-orig))
                                                   (first v) ((keyword (first v)) ns-orig))))
                                        (assoc ac (clojure.contrib.string/join ":" k)
                                               (clojure.contrib.string/join ":" v)))
                               (if (coll? k)
                                 (do (swap! nss (fn [m] (assoc m (first k) ((keyword (first k)) ns-orig))))
                                     (assoc ac (clojure.contrib.string/join ":" k) v))
                                 (if (coll? v)
                                   (do (swap! nss (fn [m] (assoc m (first v) ((keyword (first v)) ns-orig))))
                                       (assoc ac k (clojure.contrib.string/join ":" v)))
                                   (assoc ac k v)))))
                           {} @node)]
         (assoc nodep "#" @nss)))))

(defn to-json-ld
  ([triples ns jsonp]
     (let [nodes (to-nodes triples)
           _ (println (str "TO JSONFY "  (update-ns nodes ns)))
           json (clojure.contrib.json/json-str (compact-ns (update-ns nodes ns) ns))]
       (if (nil? jsonp)
         json
         (str jsonp "(" json ");")))))

(defn next-blank-id
  ([env]
     (str "_:" (swap! (:blank-node-id env) inc))))

(defn curie-to-uri
  ([term nss]
     (loop [nss nss]
       (if (empty? nss)
         term
         (let [[ns uri] (first nss)
               _ (println (str "checking " ns " , " uri  " -> " term))]
           (if (= 0 (.indexOf (safe-name term) (str (safe-name ns) ":")))
             (str uri (second (clojure.contrib.string/split (re-pattern (str (safe-name ns) ":")) (safe-name term))))
             (recur (rest nss))))))))


(defn parse-json-ld-object
  ([json-ld-object env]
     (let [nss (or (get json-ld-object (keyword "#")) {})
           subj (infer-result-kind (update-ns-term (or (get json-ld-object (keyword "@")) (next-blank-id env)) nss))]
       (reduce (fn [triples property]
                 (let [value (get json-ld-object property)
                       normalized-property (curie-to-uri property nss)
                       normalized-value (curie-to-uri value nss)]
                   (conj triples  [subj (infer-result-kind  (safe-name normalized-property)) (infer-result-kind  normalized-value) (:graph env)])))
               []
               (filter (fn [k] (not (or (= k (keyword "#")) (= k (keyword "@"))))) (keys json-ld-object))))))

(defn parse-json-ld
  ([triples-json] (parse-json-ld triples-json nil))
  ([triples-json graph]
     (let [_ (println (str "*** RAW JSON: " triples-json))
           triples (clojure.contrib.json/read-json triples-json)
           triples (if (map? triples) [triples] triples)
           env {:graph graph :blank-node-id (atom 0)}]
       (apply concat (map (fn [triple] (parse-json-ld-object triple env)) triples)))))
