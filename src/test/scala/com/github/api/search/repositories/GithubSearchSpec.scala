
package com.github.api.search.repositories

import org.scalatest.FunSpec
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import scalaj.http._

import scala.util.Random
import scala.util.control.Breaks._

class GithubSearchSpec extends FunSpec {

  val logger: Logger = Logger(this.getClass())

  val githubURL = "https://api.github.com"
  val githubRepoSearch = "/search/repositories"
  val githubUserSearch = "/search/users"
  val userName: String = "fanbot"

  describe("Searching repos for a user") {
    var responseString: HttpResponse[String] = Http(githubURL + githubRepoSearch).param("q", s"user:$userName").asString
    var responseJson: JsValue = Json.parse(responseString.body).as[JsValue]
    val foundItems: List[JsValue] = (responseJson \ "items").as[List[JsValue]]

    it("should find repos by author name") {
      assert(responseString.is2xx)
      assert(foundItems.size == 2)
    }
  }

  describe("Searching repo documents for words") {
    val searchWord = "jquery"
    it("should find repos based on searching README") {
      val randomUser = searchWordInDocGetRandomUser(s"$searchWord", "README")
      val readmeAsString = Http(githubURL + "/repos/" + randomUser + "/contents/README.md").header("Accept", "application/vnd.github.VERSION.raw").asString.body.toLowerCase()

      assert(readmeAsString contains s"$searchWord")
    }
  }

  describe("Searching repo pages") {
    val pages = 1 to 4
    val rateLimit = 1 to 50

    it("should should have 30 repos in page") {
      pages.foreach { page =>
        logger.info(s"Checking page $page")
        var responseString: HttpResponse[String] = Http(githubURL + githubRepoSearch).param("q", "tetris").param("page", s"$page").asString
        var responseJson: JsValue = Json.parse(responseString.body).as[JsValue]
        val itemsInPage: List[JsValue] = (responseJson \ "items").as[List[JsValue]]
        assert(itemsInPage.size == 30)
      }
    }

    it("should have a last page") {
      var responseString: HttpResponse[String] = Http(githubURL + githubRepoSearch).param("q", "tetris").asString
      var lastPage = responseString.headers.get("Link").get(0).split(",")(1).split(";")(0).split("page=")(1).replace(">", "")
      responseString = Http(githubURL + githubRepoSearch).param("q", "tetris").param("page", s"$lastPage").asString
      assert(responseString.is2xx)

      var responseJson: JsValue = Json.parse(responseString.body).as[JsValue]
      val itemsInPage: List[JsValue] = (responseJson \ "items").as[List[JsValue]]

      assert(itemsInPage.size <= 30)
    }

    it("should rate limit") {
      var message = ""
      var recordCount = 0
      var totalCount = 0
      breakable {
        rateLimit.foreach{page =>
          logger.info(s"checking page $page")
          var responseString: HttpResponse[String] = Http(githubURL + githubRepoSearch).param("q", "tetris").param("page", s"$page").asString
          var responseJson: JsValue = Json.parse(responseString.body).as[JsValue]
          var message: String = (responseJson \ "message").validate[String].getOrElse("key not found")
          if ((message != "key not found" ||
              message != "You have triggered an abuse detection mechanism. Please wait a few minutes before you try again.") &&
              message.startsWith("API rate limit exceeded for"))  {
            logger.debug(s"Rate limited on page $page. Maxed out at $recordCount records")
            break()
          }
          totalCount = (responseJson \ "total_count").as[Int]
          val foundItems: List[JsValue] = (responseJson \ "items").as[List[JsValue]]
          recordCount += foundItems.size
        }
      }
      assert(totalCount > recordCount)
    }
  }

  protected def searchWordInDocGetRandomUser(searchWord: String, document: String) = {
    var responseString: HttpResponse[String] = Http(githubURL + githubRepoSearch).param("q", s"$searchWord").param("in", s"$document").asString
    var responseJson = Json.parse(responseString.body).as[JsValue]
    var repos = responseJson \ "items" \\ "full_name"
    val random = new Random
    val randomUser = repos(random.nextInt(repos.size)).toString().replace("\"", "")
  }
  
}