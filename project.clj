(defproject gandalf "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [
                 [cljs-ajax "0.8.0"]
                 [honeysql "1.0.444"]
                 [metosin/reitit "0.5.2"]
                 [metosin/reitit-frontend "0.4.2"]
                 [metosin/reitit-middleware "0.5.5"]
                 [metosin/reitit-spec "0.5.1"]
                 [metosin/malli "0.0.1-SNAPSHOT"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.773"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [re-frame "1.0.0"]
                 [reagent "0.10.0"]
                 ]
  :plugins [[lein-codox "0.10.7"]
            [lein-cloverage "1.1.2"]
            [lein-cljsbuild "1.1.8"]
            ]
  :codox {:metadata {:doc/format :markdown}}
  :repl-options {:init-ns gandalf.core}
  :source-paths ["src/clj" "src/cljs" "src/cljc"]
:cljsbuild
{:builds {:minify {:source-paths ["src/cljc"]
                   :compiler {:optimizations :advanced
                              :pretty-print false}}
          :dev {:source-paths ["src-cljs"]
                :compiler {:optimizations :whitespace}}}}

  :aliases {"wibble" ["do" "clean," "compile," "cljsbuild" "once," "install"]}

  )
