/*
 * Copyright (C) 2010 University of Washington
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
package org.opendatakit.aggregate.format.element;

import static java.lang.String.join;
import static java.time.format.FormatStyle.MEDIUM;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionRepeat;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.aggregate.submission.type.jr.JRTemporal;
import org.opendatakit.common.persistence.WrappedBigDecimal;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

public class BasicElementFormatter implements ElementFormatter {

  private boolean separateCoordinates;
  private boolean includeAltitude;
  private boolean includeAccuracy;

  public BasicElementFormatter(boolean separateCoordinates, boolean includeAltitude, boolean includeAccuracy) {
    this.separateCoordinates = separateCoordinates;
    this.includeAltitude = includeAltitude;
    this.includeAccuracy = includeAccuracy;
  }

  @Override
  public void formatUid(String value, String propertyName, Row row) {
    row.addFormattedValue(Optional.ofNullable(value).orElse(null));
  }

  @Override
  public void formatBinary(BlobSubmissionType value, FormElementModel element, String ordinalValue, Row row, CallingContext cc) throws ODKDatastoreException {
    row.addFormattedValue(Optional.ofNullable(value)
        .map(BlobSubmissionType::getValue)
        .map(SubmissionKey::toString)
        .orElse(null));
  }

  @Override
  public void formatBoolean(Boolean value, FormElementModel element, String ordinalValue, Row row) {
    row.addFormattedValue(Optional.ofNullable(value)
        .map(Object::toString)
        .orElse(null));
  }

  @Override
  public void formatChoices(List<String> values, FormElementModel element, String ordinalValue, Row row) {
    row.addFormattedValue(join(" ", Optional.ofNullable(values).orElse(emptyList())));
  }

  @Override
  public void formatDateTime(Date value, FormElementModel element, String ordinalValue, Row row) {
    row.addFormattedValue(Optional.ofNullable(value)
        .map(JRTemporal::dateTime)
        .map(jrt -> jrt.humanFormat(MEDIUM))
        .orElse(null));
  }

  @Override
  public void formatDecimal(WrappedBigDecimal value, FormElementModel element, String ordinalValue, Row row) {
    row.addFormattedValue(Optional.ofNullable(value)
        .map(WrappedBigDecimal::toString)
        .orElse(null));
  }

  @Override
  public void formatJRDate(JRTemporal value, FormElementModel element, String ordinalValue, Row row) {
    row.addFormattedValue(Optional.ofNullable(value)
        .map(JRTemporal::humanFormat)
        .orElse(null));
  }

  @Override
  public void formatJRTime(JRTemporal value, FormElementModel element, String ordinalValue, Row row) {
    row.addFormattedValue(Optional.ofNullable(value)
        .map(JRTemporal::humanFormat)
        .orElse(null));
  }

  @Override
  public void formatJRDateTime(JRTemporal value, FormElementModel element, String ordinalValue, Row row) {
    row.addFormattedValue(Optional.ofNullable(value)
        .map(JRTemporal::humanFormat)
        .orElse(null));
  }

  @Override
  public void formatGeoPoint(GeoPoint value, FormElementModel element, String ordinalValue, Row row) {
    if (value == null) {
      row.addFormattedValue(null);
      return;
    }

    Optional<String> maybeLatitude = Optional.ofNullable(value).map(GeoPoint::getLatitude).map(WrappedBigDecimal::toString);
    Optional<String> maybeLongitude = Optional.ofNullable(value).map(GeoPoint::getLongitude).map(WrappedBigDecimal::toString);
    Optional<String> maybeAltitude = Optional.ofNullable(value).map(GeoPoint::getAltitude).map(WrappedBigDecimal::toString);
    Optional<String> maybeAccuracy = Optional.ofNullable(value).map(GeoPoint::getAccuracy).map(WrappedBigDecimal::toString);

    if (separateCoordinates) {
      row.addFormattedValue(maybeLatitude.orElse(null));
      row.addFormattedValue(maybeLongitude.orElse(null));
      if (includeAltitude)
        row.addFormattedValue(maybeAltitude.orElse(null));
      if (includeAccuracy)
        row.addFormattedValue(maybeAccuracy.orElse(null));
      return;
    }

    if (maybeLongitude.isPresent() && maybeLatitude.isPresent()) {
      List<String> parts = new ArrayList<>();
      parts.add(maybeLatitude.orElse(""));
      parts.add(maybeLongitude.orElse(""));
      if (includeAltitude && maybeAltitude.isPresent())
        parts.add(maybeAltitude.orElse(""));
      if (includeAccuracy && maybeAccuracy.isPresent())
        parts.add(maybeAccuracy.orElse(""));
      row.addFormattedValue(String.join(", ", parts));
    }

  }

  @Override
  public void formatLong(Long value, FormElementModel element, String ordinalValue, Row row) {
    row.addFormattedValue(Optional.ofNullable(value)
        .map(Object::toString)
        .orElse(null));
  }

  @Override
  public void formatString(String value, FormElementModel element, String ordinalValue, Row row) {
    row.addFormattedValue(Optional.ofNullable(value)
        .map(Object::toString)
        .orElse(null));
  }

  @Override
  public void formatRepeats(SubmissionRepeat repeat, FormElementModel repeatElement, Row row, CallingContext cc) {
    row.addFormattedValue(Optional.ofNullable(repeat)
        .map(SubmissionRepeat::getUniqueKeyStr)
        .orElse(null));
  }
}
