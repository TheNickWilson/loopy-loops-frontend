#!/bin/bash

echo "Applying migration Goods"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /goods                        controllers.GoodsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /goods                        controllers.GoodsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeGoods                  controllers.GoodsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeGoods                  controllers.GoodsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "goods.title = goods" >> ../conf/messages.en
echo "goods.heading = goods" >> ../conf/messages.en
echo "goods.checkYourAnswersLabel = goods" >> ../conf/messages.en
echo "goods.error.required = Enter goods" >> ../conf/messages.en
echo "goods.error.length = Goods must be 100 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryGoodsUserAnswersEntry: Arbitrary[(GoodsPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[GoodsPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryGoodsPage: Arbitrary[GoodsPage.type] =";\
    print "    Arbitrary(GoodsPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserDataGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(GoodsPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserDataGenerator.scala > tmp && mv tmp ../test/generators/UserDataGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def goods: Option[AnswerRow] = userAnswers.get(GoodsPage) map {";\
     print "    x => AnswerRow(\"goods.checkYourAnswersLabel\", s\"$x\", false, routes.GoodsController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration Goods completed"
