(ns inversion.main
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [promesa.core :as p]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reagent.dom]))

(defprotocol Price
  (fetch-pair-price! [this pair]))

(s/def ::currency-symbol (s/and string? #(= (count %) 3)))
(s/def ::pair (s/cat :base ::currency-symbol
                     :quote ::currency-symbol))
(s/def ::price p/promise?)

(s/fdef fetch-pair-price!
  :args (s/cat :this any?
               :pair ::pair)
  :ret ::price)

(defrecord FakePrice []
  Price
  (fetch-pair-price! [_ [base-currency quote-currency]]
    (p/create (fn [resolve _] (resolve 48079.93)))))

(comment
  (fetch-pair-price! (->FakePrice) ["btc" "eur"]))

(defn- investment-return (initial-investment coin-price sell-price)
  (* (/ initial-investment coin-price) sell-price))

(defn- pair->keyword [p]
  (keyword (clojure.string/join "" p)))

(rf/reg-fx
 ::price
 (fn [{:keys [pair on-success on-failure]}]
   (js/console.log "::price" pair on-success on-failure)
   (-> (p/let [price (fetch-pair-price! (->FakePrice) ["btc" "eur"])]
         (js/console.log "\t" price)
         (rf/dispatch (conj on-success {:pair pair :price price})))
       (p/catch (fn [error]
                  (when on-failure
                    #(rf/dispatch (conj on-failure %))))))))

(rf/reg-event-fx
 ::fetch-price
 (fn [{:keys [db]} event]
   (js/console.log "::fetch-price" event)
   {::price {:pair ["btc" "eur"]
             :on-success [::store-price]}}))

(rf/reg-event-fx
 ::store-price
 (fn [{:keys [db]} [_ {:keys [pair price]}]]
   (js/console.log "::store-price" pair price)
   {:db (assoc-in db [:prices (pair->keyword pair)] price)}))


(s/def ::initial-investment number?)
(s/def ::sell-price number?)
(s/def ::price number?)
(s/def ::if-i-invest-today-component
  (s/keys :req-un [::initial-investment ::sell-price ::price]))

(s/fdef if-i-invest-today
  :args (s/cat :model ::if-i-invest-today-component))

(defn- if-i-invest-today [model]
  (let [state (r/atom (dissoc model :price))]
    (fn []
      (let [initial-investment (get @state :initial-investment 0)
            sell-price (get @state :sell-price 0)]
        [:div
         [:p
          [:span "If I invest "]
          [:input {:value     initial-investment
                   :on-change #(swap! state assoc :initial-investment (.. % -target -value))}]
          [:span " in Bitcoin today and it goes to "]
          [:input {:value     sell-price
                   :on-change #(swap! state assoc :sell-price (.. % -target -value))}]
          [:span " per coin it would be worth "]
          [:span (investment-return initial-investment (:price model) sell-price)]
          [:span "."]]]))))

(defn- layout []
  [if-i-invest-today {:price              50000
                      :initial-investment 0
                      :sell-price         0}])

(defn- render []
  (reagent.dom/render [layout] (js/document.getElementById "main")))

(defn reload []
  (rf/clear-subscription-cache!)
  (render))

(defn init []
  (rf/dispatch-sync [:inversion.db/initialize])
  (rf/dispatch [::fetch-price {:pair ["btc" "eur"]}])
  (render))
