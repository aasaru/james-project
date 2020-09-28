/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.jmap.model

import com.google.common.hash.Hashing
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.{NonNegative, Positive}
import eu.timepit.refined.refineV
import org.apache.james.mailbox.model.{MailboxId, MessageId}
import reactor.core.scala.publisher.SMono

case class PositionUnparsed(value: Int) extends AnyVal
object Position {
  type Position = Int Refined NonNegative
  val zero: Position = 0

  def validateRequestPosition(requestPosition: Option[PositionUnparsed]): SMono[Position] = {
    val refinedPosition : Option[Either[String, Position]] =  requestPosition.map(position => refineV[NonNegative](position.value))

    refinedPosition match {
      case Some(Left(_))  =>  SMono.raiseError(new IllegalArgumentException(s"Negative position are not supported yet. ${requestPosition.map(_.value).getOrElse("")} was provided."))
      case Some(Right(position)) => SMono.just(position)
      case None => SMono.just(Position.zero)
    }
  }
}

case class LimitUnparsed(value: Long) extends AnyVal

object Limit {
  type Limit = Long Refined Positive
  val default: Limit = 256L

  def validateRequestLimit(requestLimit: Option[LimitUnparsed]): SMono[Limit] = {
    val refinedLimit : Option[Either[String, Limit]] =  requestLimit.map(limit => refineV[Positive](limit.value))

    refinedLimit match {
      case Some(Left(_))  =>  SMono.raiseError(new IllegalArgumentException(s"The limit can not be negative. ${requestLimit.map(_.value).getOrElse("")} was provided."))
      case Some(Right(limit)) if limit.value < default.value => SMono.just(limit)
      case _ => SMono.just(default)
    }
  }
}

case class QueryState(value: String) extends AnyVal
object QueryState {
  def forIds(ids: Seq[MessageId]): QueryState =
    forStrings(ids.map(_.serialize()))

  def forMailboxIds(ids: Seq[MailboxId]): QueryState =
    forStrings(ids.map(_.serialize()))

  def forStrings(strings: Seq[String]): QueryState = QueryState(
    Hashing.murmur3_32()
      .hashUnencodedChars(strings.mkString(" "))
      .toString)
}

case class CanCalculateChanges(value: Boolean) extends AnyVal

object CanCalculateChanges {
  val CANNOT: CanCalculateChanges = CanCalculateChanges(false)
}
