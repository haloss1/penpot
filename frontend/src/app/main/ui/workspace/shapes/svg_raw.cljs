;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) UXBOX Labs SL

(ns app.main.ui.workspace.shapes.svg-raw
  (:require
   [app.main.refs :as refs]
   [app.main.ui.shapes.svg-raw :as svg-raw]
   [app.main.ui.shapes.shape :refer [shape-container]]
   [rumext.alpha :as mf]
   [app.common.geom.shapes :as gsh]
   [app.main.ui.context :as muc]))

(defn svg-raw-wrapper-factory
  [shape-wrapper]
  (let [svg-raw-shape (svg-raw/svg-raw-shape shape-wrapper)]
    (mf/fnc svg-raw-wrapper
      {::mf/wrap [#(mf/memo' % (mf/check-props ["shape" "frame"]))]
       ::mf/wrap-props false}
      [props]
      (let [shape (unchecked-get props "shape")
            frame (unchecked-get props "frame")

            {:keys [id x y width height]} shape

            childs-ref (mf/use-memo (mf/deps shape) #(refs/objects-by-id (:shapes shape)))
            childs     (mf/deref childs-ref)

            {:keys [id x y width height]} shape
            transform (gsh/transform-matrix shape)

            tag (get-in shape [:content :tag])

            def-ctx? (mf/use-ctx muc/def-ctx)]

        (if (or (= (get-in shape [:content :tag]) :svg)
                (and (contains? shape :svg-attrs) (map? (:content shape))))
          [:> shape-container {:shape shape}
           [:& svg-raw-shape {:frame frame
                              :shape shape
                              :childs childs}]]

          [:& svg-raw-shape {:frame frame
                             :shape shape
                             :childs childs}])))))

