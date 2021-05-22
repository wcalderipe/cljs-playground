(ns hexagonal.http-client)

(defprotocol HttpClient
  "Abstraction for outgoing HTTP requests."

  (req! [this req]))
