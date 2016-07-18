(ns soda-ash.elements.helpers
  (:require [clojure.string :as string]))



;; errors


(defn throw-error [k v e-name]
  (throw (js/Error.
          (string/join " "
                       ["Soda-ash"
                        k v
                        "doesn't exist for"
                        e-name
                        "element"]))))

(def throw-type-error
  (partial throw-error :type))



;; class


(defn value->class [v]
  (-> (name v)
      (string/replace #"-" " ")))

(defn add-class->class [v]
  (value->class v))

(defn type->class [v type-set e-name]
  (when-not (= :default v)
    (if (type-set v)
      (value->class v)
      (throw-type-error v e-name))))

(defn bool->class [k v]
  (when v
    (-> (value->class k)
        (string/replace #"\?" ""))))

(defn mod->class [k v]
  (let [k' (name k)
        v' (when-not (= :default v)
             (value->class v))]
    (->> [v' k']
         (remove nil?)
         (string/join " "))))

(defn opt->class [v]
  (when-not (= :default v)
    (value->class v)))

(defn special->class [k v special-map e-name]
  (or ((special-map k) v)
      (throw-error k v e-name)))


(defn soda->class [e-name
                   soda
                   type-set
                   bool-set
                   mod-set
                   opt-set
                   special-map]
  (when (keys soda)
    (->> (for [[k v] soda]
           (cond (= :add-class k) (add-class->class v)
                 (= :type k) (type->class v type-set e-name)
                 (bool-set k) (bool->class k v)
                 (mod-set k) (mod->class k v)
                 (opt-set k) (opt->class v)
                 (special-map k) (special->class k v special-map e-name)
                 :else (throw-error k v e-name)))
         (remove nil?)
         (string/join " "))))


(defn class [ui?
             soda-class
             e-name]
  (let [ui?' (when ui? "ui")]
    (->> [ui?' soda-class e-name]
         (remove nil?)
         (string/join " "))))



;; sanitize


(defn sanitize-soda [soda]
  (dissoc soda :ratom :path :tag :ui?))

(defn sanitize-attrs [attrs]
  (dissoc attrs :soda))



;; state


(defn set-initial-values! [ratom path sanitized-soda]
  (when ratom
    (doseq [[k v] sanitized-soda]
      (swap! ratom assoc-in (flatten [path :soda k]) v))))