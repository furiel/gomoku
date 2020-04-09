(defproject gomoku "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.597"]
                 [org.immutant/immutant "2.1.10"]
                 [com.bhauman/figwheel-main "0.2.3"]
                 [org.clojure/core.async "1.1.587"]
                 [com.bhauman/rebel-readline-cljs "0.1.4"]
                 [reagent "0.10.0"]]
  :aliases {"fig" ["trampoline" "run" "-m" "figwheel.main"]}
  :main ^:skip-aot gomoku.core
  :target-path "target/%s"
  :resource-paths ["target"]
  :profiles {:uberjar {:aot :all}})
