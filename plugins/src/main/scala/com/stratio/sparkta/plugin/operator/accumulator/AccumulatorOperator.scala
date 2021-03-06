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

package com.stratio.sparkta.plugin.operator.accumulator

import java.io.{Serializable => JSerializable}

import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.{TypeOp, _}
import org.apache.spark.sql.types.StructType

import scala.util.Try

class AccumulatorOperator(name: String,
                          schema: StructType,
                          properties: Map[String, JSerializable]) extends Operator(name, schema, properties)
with OperatorProcessMapAsAny with Associative {

  final val Separator = " "

  val inputSchema = schema

  override val defaultTypeOperation = TypeOp.ArrayString

  override val writeOperation = WriteOp.AccSet

  override def processReduce(values: Iterable[Option[Any]]): Option[Any] =
    Try(Option(values.flatten.flatMap(value => {
      value match {
        case value if value.isInstanceOf[Seq[Any]] => value.asInstanceOf[Seq[Any]].map(_.toString)
        case _ => Seq(value.toString)
      }
    }))).getOrElse(None)

  def associativity(values: Iterable[(String, Option[Any])]): Option[Any] = {
    val newValues = getDistinctValues(extractValues(values, None).asInstanceOf[Seq[Seq[String]]].flatten)

    Try(Option(transformValueByTypeOp(returnType, newValues))).getOrElse(Option(Seq()))
  }
}