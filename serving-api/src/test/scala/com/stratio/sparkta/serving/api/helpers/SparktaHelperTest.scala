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

package com.stratio.sparkta.serving.api.helpers

import com.typesafe.config.{Config, ConfigFactory}
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import com.stratio.sparkta.serving.core.constants.AppConstant
import com.stratio.sparkta.serving.core.{MockConfigFactory, SparktaConfig}

@RunWith(classOf[JUnitRunner])
class SparktaHelperTest extends WordSpec with Matchers with MockFactory {


  "SparktaHelper.getExecutionMode" should {

    "return local execution mode" in {
      val config = ConfigFactory.parseString(
        """
          |sparkta{
          |   config {
          |     executionMode = local
          |     rememberPartitioner = true
          |     stopGracefully = true
          |   }
          |}
        """.stripMargin)

      SparktaConfig.initMainConfig(Some(config), new MockConfigFactory(config))
      SparktaHelper.getExecutionMode should be(AppConstant.ConfigLocal)
    }

    "return exception when no valid config exists" in {
      val config = ConfigFactory.parseString(
        """
          |sparkta{
          |
          |}
        """.stripMargin)

      SparktaConfig.initMainConfig(Some(config), new MockConfigFactory(config))

      intercept[RuntimeException]{
        SparktaHelper.getExecutionMode
      }
    }
  }

  "SparktaHelper.isClusterMode" should {

    "return true when execution mode is local" in {
      val config = ConfigFactory.parseString(
        """
          |sparkta{
          |   config {
          |     executionMode = local
          |     rememberPartitioner = true
          |     stopGracefully = true
          |   }
          |}
        """.stripMargin)
      SparktaConfig.initMainConfig(Option(config),new MockConfigFactory(config))

      SparktaHelper.isClusterMode should be (false)
    }

    "return true when execution mode is standalone" in {
      val config = ConfigFactory.parseString(
        """
          |sparkta{
          |   config {
          |     executionMode = standalone
          |     rememberPartitioner = true
          |     stopGracefully = true
          |   }
          |}
        """.stripMargin)
      SparktaConfig.initMainConfig(Option(config),new MockConfigFactory(config))

      SparktaHelper.isClusterMode should be (false)
    }

    "return false when execution mode is yarn or mesos" in {
      val config = ConfigFactory.parseString(
        """
          |sparkta{
          |   config {
          |     executionMode = yarn
          |     rememberPartitioner = true
          |     stopGracefully = true
          |   }
          |}
        """.stripMargin)
      SparktaConfig.initMainConfig(Option(config),new MockConfigFactory(config))

      SparktaHelper.isClusterMode should be (true)
    }
  }
}
