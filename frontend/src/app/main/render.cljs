;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) UXBOX Labs SL

(ns app.main.render
  (:require
   ["react-dom/server" :as rds]
   [app.config :as cfg]
   [app.main.exports :as exports]
   [app.main.exports :as svg]
   [app.main.fonts :as fonts]
   [app.util.http :as http]
   [beicon.core :as rx]
   [clojure.set :as set]
   [rumext.alpha :as mf]))

(defn- text? [{type :type}]
  (= type :text))

(defn- get-image-data [shape]
  (cond
    (= :image (:type shape))
    [(:metadata shape)]

    (some? (:fill-image shape))
    [(:fill-image shape)]

    :else
    []))

(defn populate-images-cache
  ([data]
   (populate-images-cache data nil))

  ([data {:keys [resolve-media?] :or {resolve-media? false}}]
   (let [images (->> (:objects data)
                     (vals)
                     (mapcat get-image-data))]
     (->> (rx/from images)
          (rx/map #(cfg/resolve-file-media %))
          (rx/flat-map http/fetch-data-uri)))))

(defn populate-fonts-cache [data]
  (let [texts (->> (:objects data)
                   (vals)
                   (filterv text?)
                   (mapv :content)) ]

    (->> (rx/from texts)
         (rx/map fonts/get-content-fonts)
         (rx/reduce set/union #{})
         (rx/flat-map identity)
         (rx/flat-map fonts/fetch-font-css)
         (rx/flat-map fonts/extract-fontface-urls)
         (rx/flat-map http/fetch-data-uri))))

(defn render-page
  [data]
  (rx/concat
   (->> (rx/merge
         (populate-images-cache data)
         (populate-fonts-cache data))
        (rx/ignore))

   (->> (rx/of data)
        (rx/map
         (fn [data]
           (let [elem (mf/element exports/page-svg #js {:data data :embed? true})]
             (rds/renderToStaticMarkup elem)))))))



