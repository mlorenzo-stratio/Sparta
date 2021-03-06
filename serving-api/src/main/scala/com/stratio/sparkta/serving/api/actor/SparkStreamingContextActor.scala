/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.serving.api.actor

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.util.Try

import akka.actor.SupervisorStrategy.Escalate
import akka.actor._
import akka.event.slf4j.SLF4JLogging
import akka.pattern.{ask, pipe}
import org.apache.curator.framework.CuratorFramework

import com.stratio.sparkta.driver.service.StreamingContextService
import com.stratio.sparkta.serving.api.actor.SparkStreamingContextActor._
import com.stratio.sparkta.serving.api.utils.SparkStreamingContextUtils
import com.stratio.sparkta.serving.core.exception.ServingCoreException
import com.stratio.sparkta.serving.core.models.{AggregationPoliciesModel, SparktaSerializer}

class SparkStreamingContextActor(streamingContextService: StreamingContextService,
                                 policyStatusActor: ActorRef, curatorFramework: CuratorFramework) extends Actor
  with SparkStreamingContextUtils
  with SLF4JLogging
  with SparktaSerializer {

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _: ServingCoreException => Escalate
      case t =>
        super.supervisorStrategy.decider.applyOrElse(t, (_: Any) => Escalate)
    }

  override def receive: PartialFunction[Any, Unit] = {
    case Create(policy) => create(policy) pipeTo sender
  }

  /**
   * Tries to create a spark streaming context with a given configuration.
   *
   * @param policy that contains the configuration to run.
   */
  def create(policy: AggregationPoliciesModel): Future[Try[AggregationPoliciesModel]] =
    if (policy.id.isDefined)
      launch(policy, policyStatusActor, streamingContextService, context)
    else Future {
      Try(createNewPolicy(policy, policyStatusActor, curatorFramework, streamingContextService,context))
    }
}

object SparkStreamingContextActor {

  case class Create(policy: AggregationPoliciesModel)

  case object Start

}