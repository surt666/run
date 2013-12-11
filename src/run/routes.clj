(ns run.routes
  (:use compojure.core
        run.core)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.tools.logging :as logging]
            [clojure.tools.logging.impl :as logging-impl]))

(defn servlet-init[]
  (alter-var-root (var logging/*logger-factory*) (constantly (logging-impl/log4j-factory))))

(defroutes handler

 (GET ["/:context/ping", :context #".[^/]*"]  []
      {:status 200, :headers {"Content-Type" "text/plain" "expires" "0" "cache-control" "no-cache"} :body "pong" })

 (GET ["/:context/readstatus", :context #".[^/]*"] []
      (readstatus))

 (GET ["/:context/project/:project", :context #".[^/]*"] [project]
      (read-project project))

 (GET ["/:context/showtest/:project/:buildnr", :context #".[^/]*"] [project buildnr]
      (read-test project buildnr))

 (GET ["/:context/forcebuild/:project", :context #".[^/]*"] [project]
      (force-build project))

 (GET ["/:context/forcedeploy/:project/:env", :context #".[^/]*"] [project env]
      (force-deploy project env))

 (route/resources "/")
 (route/not-found "Route not found"))

(def app
  (-> (handler/site handler)))
