(defproject gomoku "0.1.0-SNAPSHOT"
  :description "gomoku"
  :url "https://github.com/furiel/gomoku"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.597"]
                 [ring/ring-core "1.8.0"]
                 [http-kit "2.3.0"]
                 [com.bhauman/figwheel-main "0.2.3"]
                 [com.cognitect/transit-clj "1.0.324"]
                 [com.cognitect/transit-cljs "0.8.256"]
                 [org.clojure/core.async "1.1.587"]
                 [com.bhauman/rebel-readline-cljs "0.1.4"]
                 [reagent "0.10.0"]]
  :aliases {"fig:build" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]
            "fig:min"   ["run" "-m" "figwheel.main" "-O" "advanced" "-bo" "dev"]}
  :main ^:skip-aot gomoku.core
  :target-path "target/%s"
  :resource-paths ["target" "resources"]
  :profiles {:uberjar {:aot :all}})
