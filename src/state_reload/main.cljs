(ns state-reload.main
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [reagent.dom]))

;; -------------------------------------------
;; DB

(def default-db {})

(rf/reg-event-db
 ::init-db
 (fn [_ _] default-db))

;; -------------------------------------------
;; REAGENT

;; 👎 re-evaluation doesn't update the visual state.
(defn form-1-component []
  [:div
   [:p  {:style {:color "red"}} "I am Form 2 component!"]])

;; 👎 re-evaluation doesn't update the visual state.
(defn form-2-component []
  (fn [] ;; rendering fn
    [:div
     [:p  {:style {:color "blue"}} "I am Form 2 component!"]]))

(def color (r/atom "green"))

;; 👍 changing the atom updates the visual state.
(defn stateful-component []
  [:div
   [:p {:style {:color @color}} "I am a Stateful component!"]])

(defn- render []
  (reagent.dom/render [:div
                       [form-1-component]
                       [:hr]
                       [form-2-component]
                       [:hr]
                       [stateful-component]] (js/document.getElementById "main")))

(defn reload []
  (rf/clear-subscription-cache!)
  (render))

(defn init []
  (rf/dispatch-sync [::init-db])
  (render))
