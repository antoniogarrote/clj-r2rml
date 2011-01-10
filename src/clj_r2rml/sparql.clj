(ns clj-r2rml.sparql
  (:import (java.util UUID))
  (:use clj-r2rml.core)
  (:use clj-r2rml.sql))

;;; Types
(defrecord GraphPatternSQL [query terms])
(defrecord SPARQLExpr [expr])


(defn table-alias []
  (str "TBL" (.replace (str (UUID/randomUUID)) "-" "")))

(defn alfa
  ([triples-map]
     (let [query (build-sql-query triples-map)]
       (fn [triple-pattern] (str "(" query ") AS " (table-alias))))))

(defn beta
  ([triples-map]
     (let [query (build-sql-query triples-map)]
       (fn [triple-pattern pos]
         (condp = pos
             :subject "subject"
             :predicate "predicate"
             :object "object"
             (throw (Exception. (str "Unknown position for a triple " (name pos)))))))))


(defn variable?
  ([triple-component]
     (keyword? triple-component)))

(defn not-variable?
  ([triple-component]
     (not (variable? triple-component))))

(defn gen-cond-sql
  ([tp beta-fn]
     (apply
      (comp
       (fn [condition]
         (if (= (get tp :subject)
                (get tp :predicate))
           (str condition " AND " (beta-fn tp :subject) "=" (beta-fn tp :predicate))
           condition))
       (fn [condition]
         (if (= (get tp :subject)
                (get tp :object))
           (str condition " AND " (beta-fn tp :subject) "=" (beta-fn tp :object))
           condition))
       (fn [condition]
         (if (= (get tp :object)
                (get tp :predicate))
           (str condition " AND " (beta-fn tp :object) "=" (beta-fn tp :predicate))
           condition)))
      [(reduce (fn [condition position]
                 (if (not-variable? (get tp position))
                   (str condition " AND " (beta-fn tp position) "='" (get tp position)  "'")
                   condition))
               "True"
               [:subject :predicate :object])])))

(defn name-sql
  ([component]
     (if (keyword? component)
       (name component)
       (if (string? component)
         (str "'" component  "'")
         (if (instance? SPARQLExpr component)
           (:expr component)
           component)))))

(defn gen-pr-sql
  ([tp beta-fn name-fn]
     (str (beta-fn tp :subject) " AS " (name-fn (:subject tp))
          (if (not= (:predicate tp)
                    (:subject tp))
            (str ", " (beta-fn tp :predicate) " AS " (name-fn (:predicate tp)))
            "")
          (if (and (not= (:object tp)
                         (:subject tp))
                   (not= (:object tp)
                         (:predicate tp)))
            (str ", " (beta-fn tp :object) " AS " (name-fn (:object tp)))
            ""))))

(defn gen-pr-sql-names
  ([tp]
     (reduce (fn [ac it] (if (nil? it) ac (conj ac it)))
             '()
             (list (:subject tp)
                   (if (not= (:predicate tp)
                             (:subject tp))
                     (:predicate tp)
                     nil)
                   (if (and (not= (:object tp)
                                  (:subject tp))
                            (not= (:object tp)
                                  (:predicate tp)))
                     (:object tp)
                     nil)))))



(defn trans-tp
  ([tp alfa-fn beta-fn name-fn]
     (GraphPatternSQL.  (str "SELECT DISTINCT " (gen-pr-sql tp beta-fn name-fn) " FROM " (alfa-fn tp) " WHERE " (gen-cond-sql tp beta-fn)) (gen-pr-sql-names tp))))


(defn trans-and
  ([gp1 gp2 alfa-fn beta-fn name-fn]
     (let [terms-gp1 (set (:terms gp1))
           terms-gp2 (set (:terms gp2))
           terms-only-gp1 (vec (clojure.set/difference terms-gp1 terms-gp2))
           terms-only-gp2 (vec (clojure.set/difference terms-gp2 terms-gp1))
           terms-intersection-gp1-gp2 (vec (clojure.set/intersection terms-gp1 terms-gp2))
           new-table-gp1 (table-alias)
           new-table-gp2 (table-alias)

           select-sql-terms-only-gp1 (within-commas (map name-fn terms-only-gp1))
           select-sql-terms-only-gp2 (within-commas (map name-fn terms-only-gp2))
           select-sql-terms-intersection (within-commas (map (fn [term] (str "Coalesce(" new-table-gp1 "." (name-fn term) ", " new-table-gp2 "." (name-fn term) ") AS " (name-fn term)))
                                                             terms-intersection-gp1-gp2))
           select-sql (within-commas [select-sql-terms-only-gp1 select-sql-terms-only-gp2 select-sql-terms-intersection])

           inner-join-sql (str "(" (:query gp1) ") " new-table-gp1 " INNER JOIN (" (:query gp2) ") " new-table-gp2)
           join-cond-sql (reduce (fn [condition term] (str condition
                                                          " AND (" new-table-gp1 "." (name-fn term) "=" new-table-gp2 "." (name-fn term) ")"
                                                          " OR " new-table-gp1 "." (name-fn term) " IS NULL"
                                                          " OR " new-table-gp2 "." (name-fn term) " IS NULL"))
                                 "TRUE"
                                 terms-intersection-gp1-gp2)
           sql (str "SELECT DISTINCT " select-sql " FROM " inner-join-sql " ON (" join-cond-sql ")")]
       (GraphPatternSQL. sql (concat terms-only-gp1 terms-only-gp2 terms-intersection-gp1-gp2)))))

(defn trans-opt
  ([gp1 gp2 alfa-fn beta-fn name-fn]
     (let [terms-gp1 (set (:terms gp1))
           terms-gp2 (set (:terms gp2))
           terms-only-gp1 (vec (clojure.set/difference terms-gp1 terms-gp2))
           terms-only-gp2 (vec (clojure.set/difference terms-gp2 terms-gp1))
           terms-intersection-gp1-gp2 (vec (clojure.set/intersection terms-gp1 terms-gp2))
           new-table-gp1 (table-alias)
           new-table-gp2 (table-alias)

           select-sql-terms-only-gp1 (within-commas (map name-fn terms-only-gp1))
           select-sql-terms-only-gp2 (within-commas (map name-fn terms-only-gp2))
           select-sql-terms-intersection (within-commas (map (fn [term] (str "Coalesce(" new-table-gp1 "." (name-fn term) ", " new-table-gp2 "." (name-fn term) ") AS " (name-fn term)))
                                                             terms-intersection-gp1-gp2))
           select-sql (within-commas [select-sql-terms-only-gp1 select-sql-terms-only-gp2 select-sql-terms-intersection])

           inner-join-sql (str "(" (:query gp1) ") " new-table-gp1 " LEFT OUTER JOIN (" (:query gp2) ") " new-table-gp2)
           join-cond-sql (reduce (fn [condition term] (str condition
                                                          " AND (" new-table-gp1 "." (name-fn term) "=" new-table-gp2 "." (name-fn term) ")"
                                                          " OR " new-table-gp1 "." (name-fn term) " IS NULL"
                                                          " OR " new-table-gp2 "." (name-fn term) " IS NULL"))
                                 "TRUE"
                                 terms-intersection-gp1-gp2)
           sql (str "SELECT DISTINCT " select-sql " FROM " inner-join-sql " ON (" join-cond-sql ")")]
       (GraphPatternSQL. sql (concat terms-only-gp1 terms-only-gp2 terms-intersection-gp1-gp2)))))


(defn trans-union
  ([gp1 gp2 alfa-fn beta-fn name-fn]
     (let [terms-gp1 (set (:terms gp1))
           terms-gp2 (set (:terms gp2))
           terms-only-gp1 (vec (clojure.set/difference terms-gp1 terms-gp2))
           terms-only-gp2 (vec (clojure.set/difference terms-gp2 terms-gp1))
           terms-intersection-gp1-gp2 (vec (clojure.set/intersection terms-gp1 terms-gp2))
           new-table-gp1a (table-alias)
           new-table-gp2a (table-alias)
           new-table-gp1b (table-alias)
           new-table-gp2b (table-alias)


           select-sql-terms-only-gp1 (within-commas (map name-fn terms-only-gp1))
           select-sql-terms-only-gp2 (within-commas (map name-fn terms-only-gp2))
           select-sql-terms-intersection1a (within-commas (map (fn [term] (str new-table-gp1a "." (name-fn term)  " AS " (name-fn term)))
                                                               terms-intersection-gp1-gp2))
           select-sql-terms-intersection2b (within-commas (map (fn [term] (str new-table-gp2b "." (name-fn term)  " AS " (name-fn term)))
                                                               terms-intersection-gp1-gp2))
           select-sql1 (within-commas [select-sql-terms-only-gp1 select-sql-terms-only-gp2 select-sql-terms-intersection1a])
           inner-join-sql1 (str "(" (:query gp1) ") " new-table-gp1a " LEFT OUTER JOIN (" (:query gp2) ") " new-table-gp2a " ON (FALSE)")
           select-sql2 (within-commas [select-sql-terms-only-gp1 select-sql-terms-only-gp2 select-sql-terms-intersection2b])
           inner-join-sql2 (str "(" (:query gp2) ") " new-table-gp2b " LEFT OUTER JOIN (" (:query gp1) ") " new-table-gp1b " ON (FALSE)")


           sql (str "SELECT " select-sql1 " FROM " inner-join-sql1 " UNION SELECT " select-sql2 " FROM " inner-join-sql2)]
       (GraphPatternSQL. sql (concat terms-only-gp1 terms-only-gp2 terms-intersection-gp1-gp2)))))



(defn expr-bound
  ([var name-fn] (SPARQLExpr. (str "(" (name-fn var) " IS NOT NULL)"))))

(defn expr-op
  ([op op1 op2 name-fn]
     (SPARQLExpr. (str "(" (name-fn op1) op (name-fn op2) ")"))))

(defn expr-bol
  ([op op1 op2 name-fn]
     (SPARQLExpr. (str "(" (name-fn op1) op (name-fn op2) ")")))
  ([op op1 name-fn]
     (SPARQLExpr. (str "(" op (name-fn op1) ")"))))

(defn trans-expr
  ([gp expr]
     (GraphPatternSQL. (str "SELECT * FROM (" (:query gp) ") " (table-alias) " WHERE " (:expr expr)) (:terms gp))))

(defn trans-select
  ([vars gp alfa-fn beta-fn name-fn]
     (str "SELECT DISTINCT " (within-commas (map name-fn vars)) " FROM (" (:query gp) ") " (table-alias) ";")))

;;; SPARQL DSL

(defn tp
  ([s p o]
     (fn [alfa-fn beta-fn name-fn]
       (trans-tp {:subject s :predicate p :object o} alfa-fn beta-fn name-fn))))

(defn AND
  ([& gps]
     (if (= 2 (count gps))
       (fn [alfa-fn beta-fn name-fn]
         (trans-and ((first gps) alfa-fn beta-fn name-fn)
                    ((second gps) alfa-fn beta-fn name-fn)
                    alfa-fn beta-fn name-fn))
       (fn [alfa-fn beta-fn name-fn]
         (trans-and ((last gps) alfa-fn beta-fn name-fn)
                    ((apply AND ((comp reverse rest reverse) gps)) alfa-fn beta-fn name-fn)
                    alfa-fn beta-fn name-fn)))))

(defn OPT
  ([& gps]
     (if (= 2 (count gps))
       (fn [alfa-fn beta-fn name-fn]
         (trans-opt ((first gps) alfa-fn beta-fn name-fn)
                    ((second gps) alfa-fn beta-fn name-fn)
                    alfa-fn beta-fn name-fn))
       (fn [alfa-fn beta-fn name-fn]
         (trans-opt ((last gps) alfa-fn beta-fn name-fn)
                    ((apply OPT ((comp reverse rest reverse) gps)) alfa-fn beta-fn name-fn)
                    alfa-fn beta-fn name-fn)))))

(defn UNION
  ([& gps]
     (if (= 2 (count gps))
       (fn [alfa-fn beta-fn name-fn]
         (trans-union ((first gps) alfa-fn beta-fn name-fn)
                      ((second gps) alfa-fn beta-fn name-fn)
                      alfa-fn beta-fn name-fn))
       (fn [alfa-fn beta-fn name-fn]
         (trans-union ((last gps) alfa-fn beta-fn name-fn)
                      ((apply UNION ((comp reverse rest reverse) gps)) alfa-fn beta-fn name-fn)
                      alfa-fn beta-fn name-fn)))))

(defn SELECT
  ([vars gp]
     (fn [alfa-fn beta-fn name-fn]
       (trans-select vars (gp alfa-fn beta-fn name-fn) alfa-fn beta-fn name-fn))))

(defn Bound
  ([v]
     (fn [alfa-fn beta-fn name-fn]
       (expr-bound v name-fn))))
(defn And
  ([v1 v2]
     (fn [alfa-fn beta-fn name-fn]
       (let [v1 (if ((comp not fn?) v1) v1 (v1 alfa-fn beta-fn name-fn))
             v2 (if ((comp not fn?) v2) v2 (v2 alfa-fn beta-fn name-fn))]
         (expr-bol " AND " v1 v2 name-fn)))))

(defn Or
  ([v1 v2]
     (fn [alfa-fn beta-fn name-fn]
       (let [v1 (if ((comp not fn?) v1) v1 (v1 alfa-fn beta-fn name-fn))
             v2 (if ((comp not fn?) v2) v2 (v2 alfa-fn beta-fn name-fn))]
         (expr-bol " OR " v1 v2 name-fn)))))

(defn Not
  ([v1]
     (fn [alfa-fn beta-fn name-fn]
       (let [v1 (if ((comp not fn?) v1) v1 (v1 alfa-fn beta-fn name-fn))]
         (expr-bol " NOT " v1 name-fn)))))

(defn Gt
  ([v1 v2]
     (fn [alfa-fn beta-fn name-fn]
       (let [v1 (if ((comp not fn?) v1) v1 (v1 alfa-fn beta-fn name-fn))
             v2 (if ((comp not fn?) v2) v2 (v2 alfa-fn beta-fn name-fn))]
         (expr-op ">" v1 v2 name-fn)))))

(defn Lt
  ([v1 v2]
     (fn [alfa-fn beta-fn name-fn]
       (let [v1 (if ((comp not fn?) v1) v1 (v1 alfa-fn beta-fn name-fn))
             v2 (if ((comp not fn?) v2) v2 (v2 alfa-fn beta-fn name-fn))]
         (expr-op "<" v1 v2 name-fn)))))

(defn Eq
  ([v1 v2]
     (fn [alfa-fn beta-fn name-fn]
       (let [v1 (if ((comp not fn?) v1) v1 (v1 alfa-fn beta-fn name-fn))
             v2 (if ((comp not fn?) v2) v2 (v2 alfa-fn beta-fn name-fn))]
         (expr-op "=" v1 v2 name-fn)))))

(defn FILTER
  ([expr gp]
     (fn [alfa-fn beta-fn name-fn]
       (trans-expr (gp alfa-fn beta-fn name-fn) (expr alfa-fn beta-fn name-fn)))))


(defn trans
  ([gps alfa-fn beta-fn name-fn]
     (gps alfa-fn beta-fn name-fn))
  ([gps spec]
     (trans gps (alfa spec) (beta spec) name-sql)))

; SELECT ?s ?c WHERE { ?s <dept:name> ?p .
;                      ?s <dept:COMPANY> ?c .
;                      ?s "something" ?d.
;                      OPT <_:1> ?p ?c . }
;
; (trans
;  (SELECT [:s :c] (OPT
;                   (AND (tp :s "dept:name" :p)
;                        (tp :s "dept:COMPANY" :c)
;                        (tp :s "something" :d))
;                   (tp "_:1" :p :c)))
;  test-spec)

; (trans
;   (SELECT [:s :p] (FILTER (Bound :s) (tp :s "dept:name" :p)))
;   test-spec)
