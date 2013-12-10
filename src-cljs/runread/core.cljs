(ns runread.core
  (:require [ajax.core :refer [GET POST]]
            [domina :as d]
            [goog.Timer :as timer]
            [goog.events :as events]
            [domina.xpath :as dx]))

(defn convert-state [state]
  (condp = state
      "0" "green"
      "1" "red"
      "2" "yellow"
      "4" "grey"))

(defn build-html [data]
  (let [main-div (dx/xpath "//div[@id='main']")]
    (d/append! main-div (str "<div class=\"project medium-7 columns\">
                               <div class=\"name row\"><a href=\"project.html?project=" (get data "project") "\">" (get data "project") "</a></div>
                               <div class=\"row\">
                                 <div title=\"build\" class=\"small-1 columns " (convert-state (get data "build")) "\"></div>
                                 <div class=\"spacer\"></div>
                                 <div title=\"deploy\" class=\"small-1 columns " (convert-state (get data "deploy")) "\"></div>
                                 <div class=\"spacer\"></div>
                                 <div title=\"test\" class=\"small-1 columns " (convert-state (get data "test")) "\"></div>
                                 <div class=\"spacer\"><a href=\"#\" onclick=\"runread.core.forcedeploy('" (get data "project") "','" "preprod" "');\">-></a></div>
                                 <div title=\"preprod\" class=\"small-1 columns " (convert-state (get data "preprod")) "\"></div>
                                 <div class=\"spacer\"><a href=\"#\" onclick=\"runread.core.forcedeploy('" (get data "project") "','" "production" "');\">-></a></div>
                                 <div title=\"prod\" class=\"small-1 columns " (convert-state (get data "prod")) "\"></div>
                               </div>
                               <div class=\"row\">
                                 <div class=\"small-2 columns\"><a href=\"#\" onclick=\"runread.core.forcebuild('" (get data "project") "');\">Play</a></div>
                               </div>
                              </div>"))))

(defn handler [response]
  (d/destroy-children! (dx/xpath "//div[@id='main']"))
  (mapv #(build-html %) response)
  (.log js/console (str response)))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn get-data []
  (GET "/runread/readstatus" {:handler handler
                              :error-handler error-handler
                              :response-format :json}))

(defn ^:export poll []
  (let [timer (goog.Timer. 2000)]
    (do (get-data)
        (. timer (start))
        (events/listen timer goog.Timer/TICK get-data))))

(defn convert-status [status]
  (condp = status
      "success" "green"
      "fail" "red"))

(defn build-project-html [data]
  (let [main-div (dx/xpath "//div[@id='main']")]
    (d/append! main-div (str "<div class=\"row\">
                                 <div title=\"build\" class=\"small-4 columns " (convert-status (get data "status")) "\"><a href=\"#\" onclick=\"runread.core.showtest('" (get data "project") "','" (get data "buildnr") "');\">" (get data "buildnr") "</a></div>"))))

(defn handler2 [response]
  (d/destroy-children! (dx/xpath "//div[@id='main']"))
  (mapv #(build-project-html %) response)
  (.log js/console (str response)))

(defn ^:export projectread []
  (let [p js/window.location.search
        p (second (clojure.string/split p #"="))]
    (GET (str "/runread/project/" p) {:handler handler2
                                      :error-handler error-handler
                                      :response-format :json})))

(defn handler3 [response]
  (d/destroy-children! (dx/xpath "//div[@id='mainbig']"))
  (d/append! (dx/xpath "//div[@id='mainbig']") (str "<div class=\"row\">" (get response "test") "</div>"))
  (.log js/console (get response "test")))

(defn ^:export showtest [project buildnr]
  (GET (str "/runread/showtest/" project "/" buildnr) {:handler handler3
                                                       :error-handler error-handler
                                                       :response-format :json}))

(defn ^:export forcebuild [project]
  (GET (str "/runread/forcebuild/" project) {:error-handler error-handler
                                             :response-format :json}))

(defn ^:export forcedeploy [project env]
  (GET (str "/runread/forcedeploy/" project "/" env) {:error-handler error-handler
                                             :response-format :json}))
