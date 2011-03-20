(ns clj-r2rml.web.xhtml
    (:use clj-r2rml.core)
    (:use clojure.contrib.sql)
    (:use [hiccup core page-helpers]))


(defn build-candidate-select
  ([]
     "SELECT * FROM candidates WHERE `uri`=?;"))

(defn build-educations-select
  ([]
     "SELECT * FROM educations WHERE `candidate`=?;"))

(defn build-jobs-select
  ([]
     "SELECT * FROM jobs WHERE `candidate`=?;"))

(defn generate-ns
  ([nss] (reduce (fn [ac [ns uri]] (assoc ac (keyword (str "xmlns:" (name ns))) uri))
                 {} nss)))

(defn present?
  ([value]
     (not (or (nil? value) (and (string? value) (= "" value))))))

(defmacro xhtml+rdfa
  "XHTML+RDFa tat"
  [nss & contents]
  `(let [options# (merge {:version "XHTML+RDFa 1.0" :xmlns "http://www.w3.org/1999/xhtml"}
                         (generate-ns ~nss))]
     (html {:mode :xml}
           (xml-declaration "iso-8859-1")
           "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML+RDFa 1.0//EN\" \"http://www.w3.org/MarkUp/DTD/xhtml-rdfa-1.dtd\">"
           [:html options#
            ~@contents])))

(defn gen-candidate-div
  ([candidate-data]
     [:dl
      (when (present? (:address candidate-data))
        (list [:dt "Address:"]
            [:dd {:property "vcard:adr"} (:address candidate-data)]))
      (when (present? (:telephone candidate-data))
        (list [:dt "Telephone:"]
            [:dd {:property "vcard:tel"} (:telephone candidate-data)]))
      (when (present? (:birthdate candidate-data))
        (list [:dt "Birthdate:"]
            [:dd {:property "vcard:bday"} (:birthdate candidate-data)]))
      (when (present? (:email candidate-data))
        (list [:dt "Email:"]
            [:dd {:property "vcard:email"} (:email candidate-data)]))]))

(defn gen-work-experience-div
  ([job]
     [:dl {:about (:uri job)
           :typeof "cv:WorkHistory"}
      (when (present? (:start_date job))
        (list [:dt "Start Date:"]
            [:dd {:property "cv:startDate"} (:start_date job)]))
      (when (present? (:end_date job))
        (list [:dt "End Date:"]
            [:dd {:property "cv:endDate"} (:end_date job)]))
      (when (present? (:position job))
        (list [:dt "Position:"]
            [:dd {:property "cv:jobTitle"} (:position job)]))
      (when (present? (:company job))
        (list [:dt "Company:"]
            [:dd {:rel "cv:employedIn"}
             [:span {:typeof "cv:Company"}
              [:span {:property "cv:Name"}
               (:company job)]]]))
      (when (present? (:description job))
        (list [:dt "Description:"]
          [:dd {:property "cv:jobDescription"} (:description job)]))]))

(defn gen-education-div
  ([education]
     [:dl {:about (:uri education)
           :typeof "cv:Education"}
      (when (present? (:start education))
        (list [:dt "Start Date:"]
            [:dd {:property "cv:startDate"} (:start education)]))
      (when (present? (:end education))
        (list [:dt "End Date:"]
            [:dd {:property "cv:endDate"} (:end education)]))
      (when (present? (:titulation education))
        (list [:dt "Position:"]
            [:dd {:property "cv:degreeType"} (:titulation education)]))
      (when (present? (:institution education))
        (list [:dt "Institution:"]
            [:dd {:rel "cv:studiedIn"}
             [:span {:typeof "cv:Organization"}
              [:span {:property "cv:Name"}
               (:institution education)]]]))
      (when (present? (:description education))
        (list [:dt "Description:"]
          [:dd {:property "cv:educationDescription"} (:description education)]))]))

(defn gen-xhtml-body
  ([nss candidate-data educations-data jobs-data]
     (xhtml+rdfa nss
                 [:head
                  [:title (str (:name candidate-data) " " (:surname candidate-data) "'s CV" )]
                  [:link {:rel "stylesheet" :href "/css/resume.css"} ]]
                 [:body
                  [:div {:about (:uri candidate-data) :typeof "foaf:Person vcard:VCard" :id "profile" :class "cv_section"}
                   [:h1
                    [:span {:property "vcard:given-name" :class "name" }
                     (:name candidate-data)]
                    [:span {:property "vcard:family-name" :class "name"}
                     (:surname candidate-data)]]
                   (gen-candidate-div candidate-data)]
                  [:div {:id "work_experience" :class "cv_section"}
                   [:h2 "Work Experience"]
                   (map (fn [job] (gen-work-experience-div job)) jobs-data)]
                  [:div {:id "educations" :class "cv_section"}
                   [:h2 "Academic Background"]
                   (map (fn [education] (gen-education-div education)) educations-data)]])))

(defn sort-tuples
  ([tuples]
     (sort (fn [a b]
             (try (let [sda (java.util.Date. (or (:start_date a)
                                            (:start a)))
                   sdb (java.util.Date. (or (:start_date b)
                                            (:start b)))]
                    (- (compare sda sdb)))
                  (catch Exception ex 0))) tuples)))

(defn build-xhtml-cv
  ([request sql-context server-base]
     (let [id (:id (:params request))
           candidate-uri (str server-base "/api/candidates/" id "#self")]
       (with-context-connection sql-context
         (let [rs-candidate (with-query-results rs [(build-candidate-select) candidate-uri] (vec rs))
               rs-jobs (sort-tuples (with-query-results rs [(build-educations-select) candidate-uri] (vec rs)))
               rs-educations (sort-tuples (with-query-results rs [(build-jobs-select) candidate-uri] (vec rs)))]
           (apply gen-xhtml-body {:foaf "http://xmlns.com/foaf/0.1/"
                                  :vcard "http://www.w3.org/2006/vcard/ns#"
                                  :cv "http://rdfs.org/resume-rdf/"}
                  [(first rs-candidate) rs-jobs rs-educations]))))))
