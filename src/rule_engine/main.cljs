(ns rule-engine.main
  (:require [clara.rules :as cr]
            [odoyle.rules :as o]))

;; Single rule
;;   - If a txn is from 0xBill, then x
;;   - If a txn value is greater than 10, then x
;;   - If a txn category is Taxes, then x

;; Clara Rules
(comment
  (defrecord Token [symbol decimals])
  (defrecord Transaction [from to value token])

  (cr/defquery get-txns-by-from
    [:?from]
    [?txn <- Transaction (= ?from from)])

  (cr/defquery get-txns-less-than
    [:?value]
    [?txn <- Transaction (> ?value value)])

  (cr/defrule large-funds
    [Transaction (>= ?value 1)]
    =>
    (println (str "Large funds txn " ?from " to " ?to " " ?value)))

  (cr/defsession txns-from-addr 'rule-engine.main)

  (def *txns-from-addr (-> txns-from-addr
                           (cr/insert (map->Transaction {:from "Bob" :to "Alice" :value 1})
                                      (map->Transaction {:from "Alice" :to "Bob" :value 0.2})
                                      (map->Transaction {:from "Bill" :to "John" :value 0.33}))
                           (cr/fire-rules)))

  (cr/query *txns-from-addr get-txns-by-from :?from "Bob")

  (cr/query *txns-from-addr get-txns-less-than :?value 1)

  ;; NOTES:
  ;; The syntax for defining rules and queries is a bit weird and hard to
  ;; read. The whole <- thingy for binding local names, the :from [...] syntax for
  ;; accumulators, and so on. I think we can make this more intuitive.
  ;;
  ;; We are forced to make an icky global var for every rule and query we
  ;; define. Generally it's better for a macro to just return a value and let the
  ;; user def it themselves if they want to.

  )

;; O'Doyle Rules
(comment
  (def rules
    (o/ruleset
     {::txn
      [:what
       [id :txn/from from]
       [id :txn/to to]
       [id :txn/value  value]]

      ::auto-labeling
      [:what
       [::label :label/name name]
       [::label :label/txn-id txn-id]]

      ::my-auto-labeling
      [:what
       [id :txn/from from]
       :when
       (= from "Bob")
       :then
       (o/insert! ::auto-labeling :label/name "FOO")]
      }))

  ;; Create session and add rule
  (def *session
    (atom (reduce o/add-rule (o/->session) rules)))

  (swap! *session (fn [session]
                    (-> session
                        (o/insert 1 {:txn/from "Bob" :txn/to "Alice" :txn/value 1})
                        (o/insert 2 {:txn/from "Alice" :txn/to "Bob" :txn/value 0.5})
                        o/fire-rules)))

  (o/query-all @*session ::txn)
  (o/query-all @*session ::auto-labeling)

  (o/query-all @*session)



  (def rules
    (o/ruleset
     {::move-player
      [:what
       [::time ::total tt]
       :then
       #_(o/reset! (o/insert o/*session* ::player ::x tt))
       (o/insert! ::player ::x tt)]}))

  (def *session
    (atom (reduce o/add-rule (o/->session) rules)))

  (swap! *session
         (fn [session]
           (-> session
               (o/insert ::time ::total 100)
               o/fire-rules)))

  (o/query-all @*session)


  )


(defn main []
  (prn "hello world!"))
