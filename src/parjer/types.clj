(ns parjer.types
  (:require [clojure.core.typed :refer [def-alias ann AnyInteger]])
  (:import (clojure.lang APersistentSet Symbol ARef)
           (java.io PrintWriter InputStreamReader BufferedReader)))

(def-alias Serv '{:nick String
                  :server String
                  :port AnyInteger
                  :chan String
                  :owner (APersistentSet Symbol)
                  :mark String})

(def-alias Conn (ARef '{:in BufferedReader :out PrintWriter}
                      '{:in BufferedReader :out PrintWriter}))
