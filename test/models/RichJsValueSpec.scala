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

package models

import org.scalacheck.{Gen, Shrink}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FreeSpec, MustMatchers}
import play.api.libs.json._

class RichJsValueSpec extends FreeSpec with MustMatchers with PropertyChecks {

  implicit val dontShrink: Shrink[String] = Shrink.shrinkAny

  "set" - {

    "must return an error if the path is empty" in {

      val value = Json.obj()

      value.set(JsPath, Json.obj()) mustEqual JsError("path cannot be empty")
    }

    "must set a value on a JsObject" in {

      val gen = for {
        originalKey   <- Gen.alphaStr suchThat (_.nonEmpty)
        originalValue <- Gen.alphaStr suchThat (_.nonEmpty)
        pathKey       <- Gen.alphaStr suchThat (_.nonEmpty) suchThat (_ != originalKey)
        newValue      <- Gen.alphaStr suchThat (_.nonEmpty)
      } yield (originalKey, originalValue, pathKey, newValue)

      forAll(gen) {
        case (originalKey, originalValue, pathKey, newValue) =>

          val value = Json.obj(originalKey -> originalValue)

          val path = JsPath \ pathKey

          value.set(path, JsString(newValue)) mustEqual JsSuccess(Json.obj(originalKey -> originalValue, pathKey -> newValue))
      }
    }

    "must add a value to an empty JsArray" in {

      forAll(Gen.alphaStr suchThat (_.nonEmpty)) {
        newValue =>

          val value = Json.arr()

          val path = JsPath \ 0

          value.set(path, JsString(newValue)) mustEqual JsSuccess(Json.arr(newValue))
      }
    }

    "must add a value to the end of a JsArray" ignore {

    }

    "must change a value in an existing JsArray" ignore {

    }

    "must change the value of an existing key" in {

      val gen = for {
        originalKey   <- Gen.alphaStr suchThat (_.nonEmpty)
        originalValue <- Gen.alphaStr suchThat (_.nonEmpty)
        newValue      <- Gen.alphaStr suchThat (_.nonEmpty)
      } yield (originalKey, originalValue, newValue)

      forAll(gen) {
        case (pathKey, originalValue, newValue) =>

          val value = Json.obj(pathKey -> originalValue)

          val path = JsPath \ pathKey

          value.set(path, JsString(newValue)) mustEqual JsSuccess(Json.obj(pathKey -> newValue))
      }
    }

    "must return an error when trying to set a key on a non-JsObject" in {

      val value = Json.arr()

      val path = JsPath \ "foo"

      value.set(path, JsString("bar")) mustEqual JsError(s"cannot set a key on $value")
    }

    "must return an error when trying to set an index on a non-JsArray" in {

      val value = Json.obj()

      val path = JsPath \ 0

      value.set(path, JsString("bar")) mustEqual JsError(s"cannot set an index on $value")
    }

    "must return an error when trying to set an index other than zero on an empty array" in {

      val value = Json.arr()

      val path = JsPath \ 1

      value.set(path, JsString("bar")) mustEqual JsError("array index out of bounds")
    }

    "must return an error when trying to set an index out of bounds" in {

      val value = Json.arr("bar", "baz")

      val path = JsPath \ 3

      value.set(path, JsString("fork")) mustEqual JsError("array index out of bounds")
    }
  }
}
