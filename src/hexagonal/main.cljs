(ns hexagonal.main
  (:require ["antd/es/layout" :default Layout]
            ["antd/es/layout/Sider" :default Sider]
            ["antd/es/row" :default Row]
            [reagent.dom]))

(defn- layout []
  [:> Layout
   [:> (.. Layout -Content) "Content"]])

(defn- render []
  (reagent.dom/render [layout] (js/document.getElementById "main")))

(defn reload []
  (render))

(defn init []
  (render))
