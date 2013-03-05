(ns parjer.config
  (:require [clojure.core.typed :refer [ann tc-ignore check-ns]]
            [parjer.types :refer [Serv]]))

(ann get-config [-> Serv])
;needs map casts
(tc-ignore
(defn get-config []
  (read-string (slurp "setup")))
  )

(ann fetch-conf [-> Serv])
(def fetch-conf (memoize get-config))
