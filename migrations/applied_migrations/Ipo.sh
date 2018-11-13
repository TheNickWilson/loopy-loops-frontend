#!/bin/bash

echo "Applying migration Ipo"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /ipo                        controllers.IpoController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /ipo                        controllers.IpoController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeIpo                  controllers.IpoController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeIpo                  controllers.IpoController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "ipo.title = ipo" >> ../conf/messages.en
echo "ipo.heading = ipo" >> ../conf/messages.en
echo "ipo.checkYourAnswersLabel = ipo" >> ../conf/messages.en
echo "ipo.error.required = Enter ipo" >> ../conf/messages.en
echo "ipo.error.length = Ipo must be 100 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryIpoUserAnswersEntry: Arbitrary[(IpoPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[IpoPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryIpoPage: Arbitrary[IpoPage.type] =";\
    print "    Arbitrary(IpoPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserDataGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(IpoPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserDataGenerator.scala > tmp && mv tmp ../test/generators/UserDataGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def ipo: Option[AnswerRow] = userAnswers.get(IpoPage) map {";\
     print "    x => AnswerRow(\"ipo.checkYourAnswersLabel\", s\"$x\", false, routes.IpoController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration Ipo completed"
