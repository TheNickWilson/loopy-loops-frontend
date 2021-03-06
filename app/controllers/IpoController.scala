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

package controllers

import controllers.actions._
import forms.IpoFormProvider
import javax.inject.Inject
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.IpoPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.IpoView

import scala.concurrent.{ExecutionContext, Future}

class IpoController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        formProvider: IpoFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: IpoView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(ipoIndex: Int, mode: Mode): Action[AnyContent] = (identify andThen getData) {
    implicit request =>

      val userAnswers =
        request.userAnswers.getOrElse(UserAnswers(request.internalId))

      val preparedForm = userAnswers.get(IpoPage(ipoIndex)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(ipoIndex, preparedForm, mode))
  }

  def onSubmit(ipoIndex: Int, mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>

      val userAnswers =
        request.userAnswers.getOrElse(UserAnswers(request.internalId))

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(ipoIndex, formWithErrors, mode))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(userAnswers.set(IpoPage(ipoIndex), value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(IpoPage(ipoIndex), mode)(updatedAnswers))
        }
      )
  }
}
