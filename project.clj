(defproject clj-r2rml "0.0.1-SNAPSHOT"
  :description "A implementation of the R2RML proposal by the W3C to map relational data to RDF linked data"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [ring "0.3.7"]
                 [compojure "0.6.1"]
                 [ring/ring-jetty-adapter "0.3.7"]
                 [com.hp.hpl.jena/jena "2.6.2"]
                 [com.hp.hpl.jena/arq "2.8.3"]
                 [hiccup "0.3.4"]]
  :dev-dependencies [[leiningen/lein-swank "1.2.0-SNAPSHOT"]
                     [cdt "1.2"]]
  :aot :all)
;  :jvm-opts ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8030"])
