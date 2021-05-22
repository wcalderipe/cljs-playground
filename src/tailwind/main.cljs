(ns tailwind.main
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
;; MAIN

(defn- render []
  (reagent.dom/render [:h1.text-4xl.font-bold.text-center "hello world"] (js/document.getElementById "main")))

(defn reload []
  (rf/clear-subscription-cache!)
  (render))

(defn init []
  (rf/dispatch-sync [::init-db])
  (render))
