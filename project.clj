(defproject org.flatland/classlojure "0.7.1"
  :url "https://github.com/flatland/classlojure"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :description "Advanced classloading for clojure."
  :dependencies [[org.clojure/clojure "1.5.0"]]
  :aliases {"testall" ["with-profile" "dev,default:dev,1.4,default:dev,1.6,default" "test"]}
  :profiles {:1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.6 {:dependencies [[org.clojure/clojure "1.6.0-master-SNAPSHOT"]]}}
  :repositories {"sonatype-snapshots" {:url "http://oss.sonatype.org/content/repositories/snapshots"
                                       :snapshots true
                                       :releases {:checksum :fail :update :always}}})
