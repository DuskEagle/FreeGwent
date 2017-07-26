package com.duskeagle.freegwentserver.controllers

import play.api.mvc._
import play.api.libs.streams.ActorFlow
import javax.inject.Inject
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.duskeagle.freegwentserver.actors.PlayerActor

class Application @Inject() (cc: ControllerComponents) (implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {

  def simpleWebSocket = WebSocket.accept[Array[Byte], Array[Byte]] { request =>
    println("Somebody connected")
    ActorFlow.actorRef { out =>
      PlayerActor.props(out)
    }
  }
}
