(ns parjer.network
  (:require [parjer.parser :refer (irc-parse)]
            [parjer.config :refer (fetch-conf)]
            [parjer.types :refer [Conn Serv]]
            [clojure.core.typed :refer [def-alias ann AnyInteger check-ns tc-ignore]])
  (:import (java.net Socket)
           (java.io PrintWriter InputStreamReader BufferedReader)
           (clojure.lang Seqable)))

(ann nick String)
(def nick (:nick (fetch-conf)))

(ann server Serv)
(def server (fetch-conf))

(ann conn-handler [Conn -> Any])
;core.typed doesn't support while
(tc-ignore
(defn conn-handler [c]
  (while (nil? (:exit @c))
    (let [msg (.readLine (:in @c))]
      (irc-parse c msg))))
  )

(ann connect [Serv -> Conn])
; problems inferring constructors?
(tc-ignore
(defn connect [serv]
  (let [sock (Socket. (:server server) (:port server))
        in (BufferedReader. (InputStreamReader. (.getInputStream sock)))
        out (PrintWriter. (.getOutputStream sock))
        conn (ref {:in in :out out})]
    (doto
        (Thread.
         #(conn-handler conn)) (.start))
    conn))
  )

(ann writeToOut [Conn String -> PrintWriter])
;FIXME
(tc-ignore
(defn writeToOut [c msg]
  (doto (:out @c)
    (.println (str msg "\r"))
    (.flush)))
  )

(ann sendinfo [Conn -> PrintWriter])
(defn sendinfo [conn]
  (writeToOut conn (str "NICK " nick))
  (writeToOut conn (str "USER " nick " 0 * :" nick)))

(ann writeToIRC [Conn String String -> PrintWriter])
(defn writeToIRC [c chan msg]
  (writeToOut c (str "PRIVMSG " chan " :" msg)))

(ann joinChannel (Fn [Conn -> PrintWriter]
                     [Conn String -> PrintWriter]))
(defn joinChannel
  ([c] (writeToOut c (str "JOIN " (:chan server))))
  ([c chan] (writeToOut c (str "JOIN " chan))))

(ann ccn [-> Any])
(defn ccn []
  (let [irc (connect server)]
    (sendinfo irc)))
