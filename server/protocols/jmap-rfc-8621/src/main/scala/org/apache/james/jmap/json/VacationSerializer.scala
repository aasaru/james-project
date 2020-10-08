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

package org.apache.james.jmap.json

import java.time.format.DateTimeFormatter

import org.apache.james.jmap.mail.VacationResponse.{UnparsedVacationResponseId, VACATION_RESPONSE_ID}
import org.apache.james.jmap.mail.{FromDate, HtmlBody, IsEnabled, Subject, TextBody, ToDate, VacationResponse, VacationResponseGetRequest, VacationResponseGetResponse, VacationResponseId, VacationResponseIds, VacationResponseNotFound}
import org.apache.james.jmap.model._
import org.apache.james.jmap.vacation.{VacationResponsePatchObject, VacationResponseSetError, VacationResponseSetRequest, VacationResponseSetResponse, VacationResponseUpdateResponse}
import play.api.libs.json._

import scala.language.implicitConversions

object VacationSerializer {
  private implicit val isEnabledReads: Reads[IsEnabled] = Json.valueReads[IsEnabled]
  private implicit val vacationResponsePatchObjectReads: Reads[VacationResponsePatchObject] = {
    case jsObject: JsObject => JsSuccess(VacationResponsePatchObject(jsObject))
    case _ => JsError("VacationResponsePatchObject needs to be represented by a JsObject")
  }
  private implicit val vacationResponseSetRequestReads: Reads[VacationResponseSetRequest] = Json.reads[VacationResponseSetRequest]

  private implicit val vacationResponseSetUpdateResponseWrites: Writes[VacationResponseUpdateResponse] = Json.valueWrites[VacationResponseUpdateResponse]

  private implicit val vacationResponseSetErrorWrites: Writes[VacationResponseSetError] = Json.writes[VacationResponseSetError]

  private implicit val vacationResponseSetResponseWrites: Writes[VacationResponseSetResponse] = Json.writes[VacationResponseSetResponse]

  private implicit val utcDateWrites: Writes[UTCDate] =
    utcDate => JsString(utcDate.asUTC.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX")))

  private implicit val vacationResponseIdWrites: Writes[VacationResponseId] = _ => JsString(VACATION_RESPONSE_ID.value)
  private implicit val vacationResponseIdReads: Reads[VacationResponseId] = {
    case JsString("singleton") => JsSuccess(VacationResponseId())
    case JsString(_) => JsError("Only singleton is supported as a VacationResponseId")
    case _ => JsError("Expecting JsString(singleton) to represent a VacationResponseId")
  }
  private implicit val isEnabledWrites: Writes[IsEnabled] = Json.valueWrites[IsEnabled]
  private implicit val fromDateWrites: Writes[FromDate] = Json.valueWrites[FromDate]
  private implicit val toDateWrites: Writes[ToDate] = Json.valueWrites[ToDate]
  private implicit val subjectWrites: Writes[Subject] = Json.valueWrites[Subject]
  private implicit val textBodyWrites: Writes[TextBody] = Json.valueWrites[TextBody]
  private implicit val htmlBodyWrites: Writes[HtmlBody] = Json.valueWrites[HtmlBody]

  implicit def vacationResponseWrites(properties: Properties): Writes[VacationResponse] = Json.writes[VacationResponse]
    .transform(properties.filter(_))

  private implicit val vacationResponseIdsReads: Reads[VacationResponseIds] = Json.valueReads[VacationResponseIds]

  private implicit val vacationResponseGetRequest: Reads[VacationResponseGetRequest] = Json.reads[VacationResponseGetRequest]

  private implicit def vacationResponseNotFoundWrites(implicit idWrites: Writes[UnparsedVacationResponseId]): Writes[VacationResponseNotFound] =
    notFound => JsArray(notFound.value.toList.map(idWrites.writes))

  private implicit def vacationResponseGetResponseWrites(implicit vacationResponseWrites: Writes[VacationResponse]): Writes[VacationResponseGetResponse] =
    Json.writes[VacationResponseGetResponse]

  private def vacationResponseWritesWithFilteredProperties(properties: Properties): Writes[VacationResponse] =
    vacationResponseWrites(VacationResponse.propertiesFiltered(properties))

  def serialize(vacationResponse: VacationResponse)(implicit vacationResponseWrites: Writes[VacationResponse]): JsValue = Json.toJson(vacationResponse)

  def serialize(vacationResponseGetResponse: VacationResponseGetResponse)(implicit vacationResponseWrites: Writes[VacationResponse]): JsValue =
    Json.toJson(vacationResponseGetResponse)

  def serialize(vacationResponseGetResponse: VacationResponseGetResponse, properties: Properties): JsValue =
    serialize(vacationResponseGetResponse)(vacationResponseWritesWithFilteredProperties(properties))

  def serialize(vacationResponseSetResponse: VacationResponseSetResponse): JsValue = Json.toJson(vacationResponseSetResponse)

  def deserializeVacationResponseGetRequest(input: String): JsResult[VacationResponseGetRequest] = Json.parse(input).validate[VacationResponseGetRequest]

  def deserializeVacationResponseGetRequest(input: JsValue): JsResult[VacationResponseGetRequest] = Json.fromJson[VacationResponseGetRequest](input)

  def deserializeVacationResponseSetRequest(input: JsValue): JsResult[VacationResponseSetRequest] = Json.fromJson[VacationResponseSetRequest](input)
}