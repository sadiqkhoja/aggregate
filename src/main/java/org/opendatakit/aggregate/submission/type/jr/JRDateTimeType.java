/*
 * Copyright (C) 2009 Google Inc.
 * Copyright (C) 2010 University of Washington.
 * Copyright (C) 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.aggregate.submission.type.jr;

import static org.opendatakit.aggregate.OptionalProduct.all;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.submission.type.SubmissionSingleValueBase;
import org.opendatakit.common.datamodel.DynamicCommonFieldsBase;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.web.CallingContext;

public class JRDateTimeType extends SubmissionSingleValueBase<JRTemporal> {
  private Optional<JRTemporal> value = Optional.empty();

  public JRDateTimeType(DynamicCommonFieldsBase backingObject, FormElementModel element) {
    super(backingObject, element);
  }

  @Override
  public void setValueFromString(String value) {
    this.value = Optional.ofNullable(value).map(JRTemporal::dateTime);
    updateBackingObject(this.value);
  }

  @Override
  public JRTemporal getValue() {
    return value.orElse(null);
  }

  @Override
  public void formatValue(ElementFormatter elemFormatter, Row row, String ordinalValue, CallingContext cc) {
    elemFormatter.formatJRDateTime(value.orElse(null), element, ordinalValue, row);
  }

  @Override
  public void getValueFromEntity(CallingContext cc) {
    Optional<Date> parsed = Optional.empty();
    Optional<String> raw = Optional.empty();
    if (element.getFormDataModel().getChildren().isEmpty()) {
      // This is an old date field
      parsed = Optional.ofNullable(backingObject.getDateField(element.getFormDataModel().getBackingKey()));
      raw = parsed
          .map(Date::toInstant)
          .map(i -> OffsetDateTime.ofInstant(i, ZoneId.systemDefault()))
          .map(odt -> odt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    } else
      for (FormDataModel m : element.getFormDataModel().getChildren()) {
        switch (m.getOrdinalNumber().intValue()) {
          case 1:
            parsed = Optional.ofNullable(backingObject.getDateField(m.getBackingKey()));
            break;
          case 2:
            raw = Optional.ofNullable(backingObject.getStringField(m.getBackingKey()));
            break;
        }
      }
    value = all(parsed, raw).map(JRTemporal::dateTime);
    updateBackingObject(value);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof JRDateTimeType)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    JRDateTimeType other = (JRDateTimeType) obj;
    return Objects.equals(this.value, other.value);
  }

  @Override
  public int hashCode() {
    return super.hashCode() + Objects.hashCode(this.value);
  }

  @Override
  public String toString() {
    return "JRDateTimeType{" + this.value.map(JRTemporal::getRaw).orElse("null") + "}";
  }

  @Override
  public void recursivelyAddEntityKeysForDeletion(List<EntityKey> keyList, CallingContext cc) {
    // JRDateTime storage is handled by SubmissionSet
  }

  @Override
  public void persist(CallingContext cc) {
    // JRDateTime persistence is handled by SubmissionSet
  }

  private void updateBackingObject(Optional<JRTemporal> value) {
    if (element.getFormDataModel().getChildren().isEmpty())
      backingObject.setDateField(element.getFormDataModel().getBackingKey(), value.map(JRTemporal::getParsed).orElse(null));
    else
      for (FormDataModel m : element.getFormDataModel().getChildren()) {
        switch (m.getOrdinalNumber().intValue()) {
          case 1:
            backingObject.setDateField(m.getBackingKey(), value.map(JRTemporal::getParsed).orElse(null));
            break;
          case 2:
            backingObject.setStringField(m.getBackingKey(), value.map(JRTemporal::getRaw).orElse(null));
            break;
        }
      }
  }

}
