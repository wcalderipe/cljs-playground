(ns inversion.db
  (:require [re-frame.core :as rf]))

(def default-db {:prices {}})

(rf/reg-event-db
 ::initialize
 (fn [_ _] default-db))
