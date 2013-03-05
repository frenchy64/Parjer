(ns parjer.commands
  (:require [parjer.parser :as parser :refer (add-event evt-handler)]
            [parjer.config :refer (fetch-conf)]
            [parjer.network :as net :refer (writeToOut joinChannel writeToIRC)]
            [parjer.types :refer [Conn]]
            [clojure.string :as s :refer (split join)]
            [clojail.core :refer [sandbox]]
            [clojail.testers :refer [secure-tester]]
            [clojure.core.typed :refer [;types
                                        Atom1
                                        ;vars
                                        ann ann-form check-ns tc-ignore def-alias print-env]])
  (:import (clojure.lang IPersistentMap Seqable APersistentVector)))

(def-alias CmdFn [Conn (APersistentVector String) String -> Any])

(def-alias CmdMap (IPersistentMap String CmdFn))

(ann cmd-handler (Atom1 CmdMap))
;FIXME
(tc-ignore
(def cmd-handler (atom {}))
  )

(ann add-cmd [String CmdFn -> Any])
(defn add-cmd [event f]
  (swap! cmd-handler (ann-form #(assoc % event f)
                               [CmdMap -> CmdMap])))

(ann mark String)
(def mark (:mark (fetch-conf)))

(ann clojure.core/re-pattern [String -> java.util.regex.Pattern])

(ann pat java.util.regex.Pattern)
(def pat
  (re-pattern (str "[^" mark "][\\d\\w\\S]*")))

(defmacro event [e & args-body]
  `(add-event ~e (ann-form (fn ~@args-body)
                           ~'[Conn (APersistentVector String) -> Any])))

(event "NOTICE"
       [c x]
       (println "Notice: " x))

(event "PART"
       [c x]
       (println "Part"))

(event "PING"
       [c x]
       (print-env "what is x")
       (writeToOut c (str "PONG :" (nth x 4))) ;nth for core.typed
       (println "Pong Sent"))

(event "MODE"
       [c x]
       (joinChannel c))

;FIXME
(tc-ignore
(event "PRIVMSG"
       [c x]
       (let [name (nth (split (nth x 1) #"!") 0) ;nth for core.typed
             cmd (re-find pat (nth x 4)) ;nth for core.typed
             channel (nth x 3)] ;nth for core.typed
         (if cmd
           (if true ;;; Add ignored users here!
             (if (contains? @cmd-handler cmd)
               ((@cmd-handler cmd) c x channel)))
           (println x))))
  )

;not sure, kw args not supported
(ann clojail.core/sandbox [Any Any * -> [Any -> String]])

;no idea
(ann clojail.testers/secure-tester Any)

;;; Lets sandbox this....better....
(ann sb [Any -> String])
(def sb (sandbox secure-tester :timeout 5000))

(ann excp! [String -> String])
(defn excp! [ev]
  (try (sb (read-string ev))
       (catch Exception e (str "Exception: " (.getMessage e)))))

(defmacro cmd [s & args-body]
  `(add-cmd ~s (ann-form (fn ~@args-body)
                         ~'[Conn (APersistentVector String) String -> Any])))

;;; Common! Tell me how stupid i am!
(cmd "eval"
     [c x channel]
     (let [st (join " " (rest (split (nth x 4) #" ")))]
       (writeToIRC c channel (excp! st))))

(cmd "uptime"
     [c x channel]
     (writeToIRC c channel "NOTIME"))

(cmd "say"
     [c x channel]
     (let [st (join " " (rest (split (nth x 4) #" ")))] ;nth for core.typed
       (writeToIRC c channel st)))


;;; This is random. I am 100% sure!
(cmd "dice"
     [c x channel]
     (writeToIRC c channel "4"))

(cmd "join"
     [c x channel]
     (let [st (nth (split (nth x 4) #" ") 1)] ;nth for core.typed
       (joinChannel c st)))

;BUG! writeToOut call has incorrect arity
(tc-ignore
(cmd "part"
     [c x channel]
     (let [st (nth (split (nth x 4) #" ") 1)] ;nth for core.typed
       (writeToOut (str "PART " st))))
  )
