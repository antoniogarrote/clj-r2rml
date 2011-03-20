(ns clj-r2rml.web.acl
    (:use clj-r2rml.core)
    (:use clojure.contrib.sql))


(def *acls-table* "acls")

(defn build-acl-sql-query
  ([graph permission]
     (str "SELECT `" (name permission) "`, `maker` from " *acls-table* " where `graph`='" graph "'")))

(defn allowed?
  ([webid graph permission sql-context]
     (let [sql-query (build-acl-sql-query graph permission)
           result (with-context-connection sql-context
                    (with-query-results rs [sql-query] (vec rs)))]
       (if (empty? result)
         true
         (let [permission-granted (get (first result) permission)
               maker (get (first result) :maker)
               _ (println (str "ACL-CHECK -> permission " permission-granted " maker " maker " webid " webid))]
           (condp = (keyword permission-granted)
               :owner (= webid maker)
               :all   true
               false))))))

(defn grant-permissions
  ([graph rdf-type maker read write sql-context]
     (let [query (str "INSERT into acls (`maker`,`graph`,`read`,`write`,`rdftype`) values ('" maker "','" graph "','" (name read) "','" (name write) "','" rdf-type "');")]
       (with-context-connection sql-context
         (do-commands query)))))


(defn revoke-permissions
  ([graph maker sql-context]
     (let [query (str "DELETE FROM acls WHERE `graph`='" graph "' AND `maker`='" maker "';")]
       (with-context-connection sql-context
         (do-commands query)))))

(defn owned-by
  ([maker sql-context]
     (let [sql-query (str "SELECT `graph` FROM acls WHERE `maker`='" maker "';")]
       (with-context-connection sql-context
         (with-query-results rs [sql-query] (vec rs)))))
  ([maker rdf-type sql-context]
     (let [sql-query (str "SELECT `graph` FROM acls WHERE `maker`='" maker "' AND `rdftype`='" rdf-type "';")]
       (with-context-connection sql-context
                    (with-query-results rs [sql-query] (vec rs))))))
