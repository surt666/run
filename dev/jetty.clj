(ns jetty
  (:use run.routes
        ring.adapter.jetty)
  (:require [clojure.tools.logging :as logging]
            [clojure.tools.logging.impl :as impl])
  (:import (org.eclipse.jetty.xml XmlConfiguration)
           (org.eclipse.jetty.webapp WebAppContext)))

(def server (ref nil))

(defn init-server [server]
  (try
    (alter-var-root (var logging/*logger-factory*) (constantly (impl/log4j-factory)))
    (let [config (XmlConfiguration. (slurp "dev/jetty.xml"))]
      (. config configure server))
    (catch Exception e
      (prn "Unable to load jetty configuration")
      (. e printStackTrace))))

(defn start []
  (if @server
    (.start @server)
    (dosync (ref-set server (run-jetty #'app {:port 8080 :configurator init-server :join? false})))))

(defn stop []
  (if @server (.stop @server)))
