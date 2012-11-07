package com.github.brianmartin

import java.net.InetSocketAddress
import java.util.{NoSuchElementException => NoSuchElement}
import org.jboss.netty.handler.codec.http.HttpMethod
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.util.CharsetUtil.UTF_8
import com.twitter.util.Future
import com.twitter.finagle.http.{Http, RichHttp, Request, Response}
import com.twitter.finagle.http.{Http, RichHttp, Request, Response}
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.Version.Http11
import com.twitter.finagle.http.path._
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finagle.builder.{Server, ServerBuilder}
import net.liftweb.common.Logger

object RestUtils {

    def getJSON(str: String): String = "[%s]" format str

    def constructJSONResponse(json: String): Response = {
      val response = Response()
      response.setContentTypeJson
      response.content = copiedBuffer(json, UTF_8)
      response
    }

    def constructJSONErrorResponse(json: String): Response = {
      val response = Response(Http11, InternalServerError)
      response.mediaType = "text/plain"
      response.content = copiedBuffer(json, UTF_8)
      response
    }

}

object RestServer extends Logger {

  import RestUtils._

  val posMatcher: PartialFunction[(HttpMethod, Path), Future[Response]] = {
    case GET -> Root / "pos" => Future.value {
      val data = getJSON("hello")
      debug("data: %s" format data)
      constructJSONResponse(data)
    }
  }

  val todosMatcher: PartialFunction[(HttpMethod, Path), Future[Response]] = {
    case GET -> Root / "todos" => Future.value {
      val data = getJSON("hello")
      debug("data: %s" format data)
      constructJSONResponse(data)
    }
  }

  val notFoundMatcher: PartialFunction[(HttpMethod, Path), Future[Response]] = {
    case _ => Future value Response(Http11, NotFound)
  }

  class Respond(fns: PartialFunction[(HttpMethod, Path), Future[Response]]*) extends Service[Request, Response] with Logger {

    val matches = fns.drop(1).foldLeft(fns.head)(_ orElse _)

    def apply(request: Request) = {
      try {
        matches(request.method -> Path(request.path))
      } catch {
        case e: NoSuchElement => Future value Response(Http11, NotFound)
        case e: Exception => Future.value {
          val message = Option(e.getMessage) getOrElse "Something went wrong."
          error("\nMessage: %s\nStack trace:\n%s"
            .format(message, e.getStackTraceString))
          constructJSONErrorResponse(message)
        }
      }
    }
  }

  def main(args: Array[String]) {

    val service = new Respond(posMatcher, todosMatcher, notFoundMatcher)
    val port    = 8888
    val server  = ServerBuilder()
      .codec(RichHttp[Request](Http()))
      .bindTo(new InetSocketAddress(port))
      .name("restserver")
      .build(service)

    info("Server started on port: %s" format port)
  }
}
