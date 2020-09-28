/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 * http://www.apache.org/licenses/LICENSE-2.0                   *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 * ***************************************************************/

package org.apache.james.jmap.mail

import java.nio.charset.StandardCharsets.US_ASCII
import java.time.ZoneId

import org.apache.commons.lang3.StringUtils
import org.apache.james.jmap.model.UTCDate
import org.apache.james.mime4j.codec.{DecodeMonitor, DecoderUtil}
import org.apache.james.mime4j.dom.address.{AddressList, Group, Address => Mime4jAddress, Mailbox => Mime4jMailbox}
import org.apache.james.mime4j.field.{AddressListFieldImpl, ContentLocationFieldImpl, DateTimeFieldImpl}
import org.apache.james.mime4j.stream.Field
import org.apache.james.mime4j.util.MimeUtil

import scala.jdk.CollectionConverters._

object EmailHeader {
  def apply(field: Field): EmailHeader = EmailHeader(EmailHeaderName(field.getName), RawHeaderValue.from(field))
}

object RawHeaderValue extends EmailHeaderValue {
  def from(field: Field): RawHeaderValue = RawHeaderValue(new String(field.getRaw.toByteArray, US_ASCII).substring(field.getName.length + 1))
}

object TextHeaderValue extends EmailHeaderValue {
  def from(field: Field): TextHeaderValue = TextHeaderValue(MimeUtil.unfold(DecoderUtil.decodeEncodedWords(field.getBody, DecodeMonitor.SILENT)).stripLeading())
}

object AddressesHeaderValue extends EmailHeaderValue {
  def from(field: Field): AddressesHeaderValue = AddressesHeaderValue(EmailAddress.from(AddressListFieldImpl.PARSER.parse(field, DecodeMonitor.SILENT).getAddressList))
}

object GroupedAddressesHeaderValue extends EmailHeaderValue {
  def from(field: Field): GroupedAddressesHeaderValue = {
    val addresses: List[Mime4jAddress] =
      Option(AddressListFieldImpl.PARSER.parse(field, DecodeMonitor.SILENT).getAddressList)
      .getOrElse(new AddressList())
      .asScala
      .toList

    if (addresses.isEmpty) {
      GroupedAddressesHeaderValue(List())
    } else {
      val groups: List[EmailAddressGroup] = addresses
        .flatMap({
          case group: Group => Some(group)
          case _ => None
        })
        .map(group => EmailAddressGroup(Some(GroupName(group.getName)), EmailAddress.from(group.getMailboxes)))

      val addressesWithoutGroup: List[EmailAddress] = addresses
        .flatMap({
          case mailbox: Mime4jMailbox => Some(mailbox)
          case _ => None
        })
        .map(EmailAddress.from(_))

      GroupedAddressesHeaderValue(List(EmailAddressGroup(None, addressesWithoutGroup)) ++ groups)
    }
  }
}

object MessageIdsHeaderValue {
  def from(field: Field): MessageIdsHeaderValue = {
    val messageIds: List[HeaderMessageId] = MimeUtil.unfold(StringUtils.normalizeSpace(field.getBody))
      .split(' ')
      .flatMap(body => {
        if(body.startsWith("<") && body.endsWith(">") && body.contains("@")) {
          scala.Right(HeaderMessageId.from(body))
        } else {
          Left()
        }
      }.toOption)
      .toList

      MessageIdsHeaderValue(Option(messageIds).filter(_.nonEmpty))
  }
}

object DateHeaderValue extends EmailHeaderValue {
  def from(field: Field, zoneId: ZoneId): DateHeaderValue =
    Option(DateTimeFieldImpl.PARSER.parse(field, DecodeMonitor.SILENT).getDate)
      .map(date => DateHeaderValue(Some(UTCDate.from(date, zoneId))))
      .getOrElse(DateHeaderValue(None))
}

object URLsHeaderValue extends EmailHeaderValue {
  def from(field: Field): URLsHeaderValue = {
    val url: Option[List[HeaderURL]] = Option(ContentLocationFieldImpl.PARSER.parse(field, DecodeMonitor.SILENT).getLocation)
      .map(urls => urls.split(',')
        .toList
        .flatMap(url => {
          if(url.startsWith("<") && url.endsWith(">")) {
            scala.Right(HeaderURL.from(url))
          } else {
            Left()
          }
        }.toOption))

      URLsHeaderValue(url.filter(_.nonEmpty))
  }
}

case class EmailHeaderName(value: String) extends AnyVal

sealed trait EmailHeaderValue
case class RawHeaderValue(value: String) extends EmailHeaderValue
case class TextHeaderValue(value: String) extends EmailHeaderValue
case class AddressesHeaderValue(value: List[EmailAddress]) extends EmailHeaderValue
case class GroupedAddressesHeaderValue(value: List[EmailAddressGroup]) extends EmailHeaderValue
case class MessageIdsHeaderValue(value: Option[List[HeaderMessageId]]) extends EmailHeaderValue
case class DateHeaderValue(value: Option[UTCDate]) extends EmailHeaderValue
case class URLsHeaderValue(value: Option[List[HeaderURL]]) extends EmailHeaderValue

case class EmailHeader(name: EmailHeaderName, value: EmailHeaderValue)
