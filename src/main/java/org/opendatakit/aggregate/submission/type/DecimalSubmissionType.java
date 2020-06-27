/*
 * Copyright (C) 2009 Google Inc.
 * Copyright (C) 2010 University of Washington.
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

package org.opendatakit.aggregate.submission.type;

import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.common.datamodel.DynamicCommonFieldsBase;
import org.opendatakit.common.persistence.WrappedBigDecimal;
import org.opendatakit.common.web.CallingContext;

/**
 * Data Storage Converter for Decimal Type
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public class DecimalSubmissionType extends
    SubmissionSingleValueBase<WrappedBigDecimal> {
  /**
   * Constructor
   */
  public DecimalSubmissionType(DynamicCommonFieldsBase backingObject,
                               FormElementModel element) {
    super(backingObject, element);
  }

  /**
   * Parse the value from string format and convert to Double/Decimal
   *
   * @param value string form of the value
   */
  @Override
  public void setValueFromString(String value) {
    if (value == null) {
      setValue(null);
    } else {
      setValue(new WrappedBigDecimal(value));
    }
  }

  @Override
  public void getValueFromEntity(CallingContext cc) {
    WrappedBigDecimal value = backingObject.getNumericField(element.getFormDataModel().getBackingKey());
    setValue(value);
  }

  /**
   * Format value for output
   *
   * @param elemFormatter the element formatter that will convert the value to the
   *                      proper format for output
   */
  @Override
  public void formatValue(ElementFormatter elemFormatter, Row row, String ordinalValue, CallingContext cc) {
    elemFormatter.formatDecimal(getValue(), element, ordinalValue, row);
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof DecimalSubmissionType)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    return true;
  }

  @Override
  public WrappedBigDecimal getValue() {
    return backingObject.getNumericField(element.getFormDataModel().getBackingKey());
  }

  /**
   * Set the value of submission field
   *
   * @param value value to set
   */
  protected void setValue(WrappedBigDecimal value) {
    backingObject.setNumericField(element.getFormDataModel().getBackingKey(), value);
  }
}
