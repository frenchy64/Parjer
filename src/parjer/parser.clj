(ns parjer.parser
  (:require [clojure.core.typed :refer [;types
                                        Atom1
                                        ann check-ns def-alias ann-form cf tc-ignore]]
            [parjer.types :refer [Conn]])
  (:import (clojure.lang Seqable IPersistentMap APersistentVector)))

(def-alias EvtMap (IPersistentMap String [Conn (APersistentVector String) -> Any]))

(ann evt-handler (Atom1 EvtMap))
;problems with {} <: (IPersistentMap Nothing Nothing)
(tc-ignore
(def evt-handler
  (atom {}))
  )

(ann add-event [String [Conn (APersistentVector String) -> Any] -> Any])
(defn add-event [event f]
  (swap! evt-handler (ann-form #(assoc % event f)
                               [EvtMap -> EvtMap])))

(ann clojure.core/re-matches [java.util.regex.Pattern String -> (U String
                                                                   (APersistentVector String)
                                                                   nil)])

(ann re-parse [String -> (APersistentVector String)])
(defn re-parse [x]
  ;core.typed isn't quite good enough for postconditions yet
  ;{:post [(vector? x)]}
  (let [m (re-matches #"^(?:[:](\S+) )?(\S+)(?: (?!:)(.+?))?(?: [:](.+))?$" x)
        _ (assert (vector? m))]
    m))

(ann irc-parse [Conn String -> Any])
;problems with {} <: (IPersistentMap Nothing Nothing)
(tc-ignore
(defn irc-parse [conn x]
  (let [msg (re-parse x)
        cmd (get msg 2)]
    (if (contains? @evt-handler cmd)
      ((@evt-handler cmd) conn msg))
    (println cmd)
    (println x)))
  )
