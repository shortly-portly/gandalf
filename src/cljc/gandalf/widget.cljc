(ns gandalf.widget
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(defmulti widget :type)
