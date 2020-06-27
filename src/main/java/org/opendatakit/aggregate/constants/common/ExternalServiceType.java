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
package org.opendatakit.aggregate.constants.common;

import java.io.Serializable;

public enum ExternalServiceType implements Serializable {
  GOOGLE_SPREADSHEET("Google Spreadsheet", false),
  JSON_SERVER("JSON Server", false),
  // Obsolete external service type as of v2.0
  OHMAGE_JSON_SERVER("OBSOLETE - Ohmage JSON Server", true),
  GOOGLE_FUSIONTABLES("OBSOLETE - Google FusionTables", true),
  REDCAP_SERVER("OBSOLETE - REDCap Server", true),
  GOOGLE_MAPS_ENGINE("OBSOLETE Google Maps Engine", true);

  private final String name;
  private final boolean obsolete;

  ExternalServiceType(String name, boolean obsolete) {
    this.name = name;
    this.obsolete = obsolete;
  }

  public String getName() {
    return name;
  }

  public String toString() {
    return name;
  }

  public boolean isObsolete() {
    return obsolete;
  }
}
