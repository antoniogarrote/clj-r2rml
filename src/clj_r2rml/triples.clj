(ns clj-r2rml.triples)

(defn safe-name
  ([term]
     (let [str-term (str term)]
       (if (.startsWith str-term ":")
         (clojure.contrib.string/join ":" (rest (clojure.contrib.string/split #":" str-term)))
         term))))

(defn infer-result-kind
  ([value]
     (if (.startsWith value "http")
       {:token :uri :value value}
       (if (.startsWith value "_:")
         {:token :blank :value value}
         {:token :literal :value value}))))

(defn blank?
  ([x] (or (nil? x) (= x ""))))

(defn make-ns-env
  ([parsed-query]
     (let [prologue (:prologue parsed-query)
           default (if (= (prologue :base) "")
                     nil (-> prologue :base :value))
           prefixes (reduce (fn [m token] (assoc m (:prefix token) (:local token))) {} (:prefixes prologue))]
       {:ns {:default default :prefixes prefixes}})))

(defn make-empty-ns-env
  ([]
     {:ns {:default nil :prefixes {}}}))

(defn update-base-env
  ([env base]
     (assoc-in env [:ns :default] base)))

(defn add-prefix-env
  ([env prefix value]
     (assoc-in env [:ns :prefixes prefix] value)))

(defn check-prefix
  ([prefix env]
     (let [val (get (-> env :ns :prefixes) prefix)]
       (if (nil? val)
         (throw (Exception. (str "No registered prefix in current environment for value: " prefix)))
         val))))

(defn normalize-uri
  ([term env]
     (if (blank? (:prefix term))
       (if (blank? (:value term))
         (if (blank? (:suffix term))
           (-> env :ns :default)
           (str (-> env :ns :default) (:suffix term)))
         (:value term))
       (let [prefix-val (check-prefix (:prefix term) env)]
         (str prefix-val (:suffix term))))))


(defn normalize-term
  ([term env]
     (condp = (safe-name (:token term))
         "uri" (normalize-uri term env)
         "literal" (:value term)
         "var" (keyword (:value term))
         "blank" (str "_:" (:label term))
         (throw (Exception. (str "Unknown URI component " term))))))

(defn normalize-term-obj
  ([term env]
     (condp = (safe-name (:token term))
         "uri" {:token "uri" :value (normalize-uri term env)}
         "literal" {:token "literal" :value (:value term)}
         "var" {:token "var" :value (keyword (:value term))}
         "blank" {:token "blank" :value (str "_:" (:label term))}
         (throw (Exception. (str "Unknown URI component " term))))))


(defn normalize-term-string
  ([term env]
     (if (string? term)
       term
       (condp = (safe-name (:token term))
           "uri" (str "<" (:value term) ">")
           "literal"  (str "\"" (:value term) "\"")
           "var" (str "?" (:value term))
           "blank" (:value term)
           (throw (Exception. (str "Unknown URI component " term)))))))

(defn normalize-triple
  ([token env]
     [(normalize-term (:subject token) env)
      (normalize-term (:predicate token) env)
      (normalize-term (:object token) env)
      ;; If graph is nil -> default graph
      (if (nil? (:graph token))
        nil
        (normalize-term (:graph token) env))]))

(defn normalize-triple-obj
  ([token env]
     [(normalize-term-obj (:subject token) env)
      (normalize-term-obj (:predicate token) env)
      (normalize-term-obj (:object token) env)
      ;; If graph is nil -> default graph
      (if (nil? (:graph token))
        nil
        (normalize-term-obj (:graph token) env))]))

(defn normalize-triple-string
  ([token env]
     [(normalize-term-string (nth token 0) env)
      (normalize-term-string (nth token 1) env)
      (normalize-term-string (nth token 2)  env)
      ;; If graph is nil -> default graph
      (if (nil? (nth token 3))
        nil
        (normalize-term-string (nth token 3) env))]))

