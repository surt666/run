(defproject runread "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.6"]
                 [me.raynes/fs "1.4.4"]
                 [cheshire "5.2.0"]
                 [domina "1.0.2"]
                 [cljs-ajax "0.2.2"]
                 [org.clojure/clojurescript "0.0-2080"]
                 [org.slf4j/slf4j-simple "1.6.6"]
                 [org.clojure/tools.logging "0.2.6"]
                 [ring/ring-core "1.1.5" :exclusions [javax.servlet/servlet-api]]
                 [ring/ring-servlet "1.1.5" :exclusions [javax.servlet/servlet-api]]
                 [javax.servlet/servlet-api "2.5"] ;because of lein with-profile user ring uberwar
                 [log4j "1.2.17" :exclusions [javax.mail/mail
                                              javax.jms/jms
                                              com.sun.jdmk/jmxtools
                                              com.sun.jmx/jmxri]]]

  :plugins [[lein-ring "0.8.7"]
            [lein-cljsbuild "1.0.0"]
            [yij/lein-plugins "1.0.12"]]
  :hooks [leiningen.cljsbuild]
  :cljsbuild {
    :builds [{:source-paths ["src-cljs"]
              :compiler {:output-to "resources/public/js/main.js"
                         :optimizations :whitespace
                         :pretty-print true}}]}

  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[clj-stacktrace "0.2.4"]
                                  [ring/ring-jetty-adapter "1.1.5"]
                                  [org.eclipse.jetty/jetty-xml "7.6.1.v20120215"]
                                  [org.eclipse.jetty/jetty-webapp "7.6.1.v20120215"]
                                  [org.eclipse.jetty/jetty-plus "7.6.1.v20120215"]
                                  [javax.servlet/servlet-api "2.5"]]}}

  :ring {:handler runread.routes/app
         :war-exclusions [#"servlet-api-.+.jar" #"javax\.servlet*"]
         :servlet-path-info? false
         :web-xml "web.xml"
         :init runread.routes/servlet-init})
