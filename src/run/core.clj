(ns run.core
  (:require [me.raynes.fs :as fs]
            [me.raynes.fs.compression :as fsc]
            [cheshire.core :refer :all]))

(def root "/home/m00522/abs/")

(defn readproject [dir]
  (let [build (str (first (slurp (str root dir "/build_flag"))))
        deploy (str (first (slurp (str root dir "/deploy_flag"))))
        test (str (first (slurp (str root dir "/test_flag"))))
        preprod (str (first (slurp (str root dir "/preprod_flag"))))
        prod (str (first (slurp (str root dir "/production_flag"))))]
    {:project dir
     :build build
     :deploy deploy
     :test test
     :preprod preprod
     :prod prod}))

(defn readstatus []
  (let [dirs (filter #(and (not= % "build") (not= % "projects")) (fs/list-dir root))
        res (mapv #(when (fs/directory? (str root %)) (readproject %)) dirs)]
    {:status 200, :headers {"Content-Type" "application/json" "expires" "0" "cache-control" "no-cache"} :body (generate-string res)}))

(defn project-builds [project dir]
  (let [status (if (fs/file? (str root "build/" project "/" dir "/success")) "success" "fail")]
    {:buildnr (Integer/parseInt dir)
     :status status
     :project project}))

(defn read-project [project]
  (let [dirs (fs/list-dir (str root "build/" project))
        status (if (fs/file? (str root "build/" project "/success")) "success" "fail")
        res (reverse (sort-by :buildnr (mapv #(project-builds project %) dirs)))]
    {:status 200, :headers {"Content-Type" "application/json" "expires" "0" "cache-control" "no-cache"} :body (generate-string res)}))

(defn read-test [project buildnr]
  (let [file (str root "build/" project "/" buildnr "/testoutput")
        res (if (fs/file? file) (slurp file) "No Test Data")]
    {:status 200, :headers {"Content-Type" "application/json" "expires" "0" "cache-control" "no-cache"} :body (generate-string {:test res})}))

(defn force-build [project]
  (spit (str root project "/latest") "1")
  (clojure.java.shell/sh "ansible-playbook" "-i" "ci" "build.yml" "--extra-vars" (str "project=" project) :dir "/home/m00522/playbooks"))

(defn force-deploy [project env]
  (spit (str root project "/" env "_flag") "2")
  (clojure.java.shell/sh "ansible-playbook" "-i" env "deploy.yml" "--extra-vars" (str "project=" project) :dir "/home/m00522/playbooks"))
