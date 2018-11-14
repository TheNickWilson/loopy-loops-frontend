/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import play.api.libs.json._

package object models {

  implicit class RichJsValue(jsValue: JsValue) {

    def set(path: JsPath, value: JsValue): JsResult[JsValue] =
      (path.path, jsValue) match {

        case (Nil, _) =>
          JsError("path cannot be empty")

        case (IdxPathNode(index) :: _, oldValue: JsArray) if index >= 0 && index <= oldValue.value.length =>
          JsSuccess(oldValue.append(value))

        case (IdxPathNode(index) :: _, oldValue: JsArray) =>
          JsError("array index out of bounds")

        case (IdxPathNode(_) :: _, oldValue) =>
          JsError(s"cannot set an index on $oldValue")

        case (KeyPathNode(key) :: _, oldValue: JsObject) =>
          JsSuccess(oldValue + (key -> value))

        case (KeyPathNode(_) :: _, oldValue) =>
          JsError(s"cannot set a key on $oldValue")
      }
  }
}
