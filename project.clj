(defproject dbutil "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :creds :gpg}}

  :signing {:gpg-key "7D1E8DF5"}

  :dependencies [[org.clojure/clojure "1.7.0-alpha3"]
                 [com.taoensso/timbre "3.3.1"]
                 [com.datomic/datomic-pro "0.9.5067"
                  :exclusions [org.slf4j/slf4j-nop joda-time org.slf4j/slf4j-log4j12]]])
