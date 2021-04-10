(ns hexagonal.main
  (:require [reagent.dom]))

(defn- render []
  (reagent.dom/render [:div [:h1 "Hello World"]] (js/document.getElementById "main")))

(defn reload []
  (render))

(defn init []
  (render))
