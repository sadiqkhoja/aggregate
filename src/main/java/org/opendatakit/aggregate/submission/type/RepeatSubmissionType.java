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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opendatakit.aggregate.constants.format.FormatConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.submission.SubmissionElement;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.aggregate.submission.SubmissionRepeat;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.aggregate.submission.SubmissionValue;
import org.opendatakit.aggregate.submission.SubmissionVisitor;
import org.opendatakit.common.datamodel.DynamicBase;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.Direction;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data Storage type for a repeat type. Store a list of datastore keys to
 * submission sets in an entity
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public class RepeatSubmissionType implements SubmissionRepeat {
  private static final Logger LOGGER = LoggerFactory.getLogger(RepeatSubmissionType.class);
  /**
   * ODK identifier that uniquely identifies the form
   */
  private final IForm form;

  /**
   * Enclosing submission set
   */
  private final SubmissionSet enclosingSet;

  /**
   * Identifier for repeat
   */
  private final FormElementModel repeatGroup;

  private final String uriAssociatedRow;
  /**
   * List of submission sets that are a part of this submission set Ordered by
   * OrdinalNumber...
   */
  private List<SubmissionSet> submissionSets = new ArrayList<>();
  private Map<Long, SubmissionSet> submissionSetIndex = new HashMap<>();

  public RepeatSubmissionType(SubmissionSet enclosingSet, FormElementModel repeatGroup,
                              String uriAssociatedRow, IForm form) {
    this.enclosingSet = enclosingSet;
    this.form = form;
    this.repeatGroup = repeatGroup;
    this.uriAssociatedRow = uriAssociatedRow;
  }

  @Override
  public final FormElementModel getFormElementModel() {
    return repeatGroup;
  }

  public String getUniqueKeyStr() {
    EntityKey key = enclosingSet.getKey();
    return key.getKey();
  }

  public void addSubmissionSet(SubmissionSet submissionSet) {
    submissionSets.add(submissionSet);
    submissionSetIndex.put(submissionSet.getOrdinalNumber(), submissionSet);
  }

  public List<SubmissionSet> getSubmissionSets() {
    return submissionSets;
  }

  public int getNumberRepeats() {
    return submissionSets.size();
  }

  /**
   * @return submissionKey that defines all the repeats for this particular
   *     repeat group.
   */
  public SubmissionKey constructSubmissionKey() {
    return enclosingSet.constructSubmissionKey(repeatGroup);
  }

  /**
   * Format value for output
   *
   * @param elemFormatter the element formatter that will convert the value to the proper
   *                      format for output
   */
  @Override
  public void formatValue(ElementFormatter elemFormatter, Row row, String ordinalValue,
                          CallingContext cc) throws ODKDatastoreException {
    elemFormatter.formatRepeats(this, repeatGroup, row, cc);
  }

  @Override
  public void getValueFromEntity(CallingContext cc) throws ODKDatastoreException {
    DynamicBase submission = (DynamicBase) repeatGroup.getFormDataModel().getBackingObjectPrototype();

    List<CommonFieldsBase> repeatRows = getRepeatRows(cc, submission);

    for (List<DynamicBase> groupOfRepeatRows : groupPerOrdinalNumber(submission, repeatRows)) {
      DynamicBase row = chooseOneFrom(groupOfRepeatRows);
      SubmissionSet submissionSet = new SubmissionSet(enclosingSet, row, repeatGroup, form, cc);
      submissionSets.add(submissionSet);
      submissionSetIndex.put(row.getOrdinalNumber(), submissionSet);
    }

  }

  private Collection<List<DynamicBase>> groupPerOrdinalNumber(DynamicBase submission, List<CommonFieldsBase> repeatRows) {
    // We don't have the logic to handle fractional returns of rows.
    long maxOrdinalNumber = 0;
    Map<Long, List<DynamicBase>> repeatRowsPerOrdinalNumber = new HashMap<>();
    for (CommonFieldsBase _repeatRow : repeatRows) {
      DynamicBase repeatRow = (DynamicBase) _repeatRow;
      Long ordinalNumber = repeatRow.getOrdinalNumber();
      maxOrdinalNumber = Math.max(maxOrdinalNumber, ordinalNumber);
      if (!repeatRowsPerOrdinalNumber.containsKey(ordinalNumber))
        repeatRowsPerOrdinalNumber.put(ordinalNumber, new ArrayList<DynamicBase>());
      repeatRowsPerOrdinalNumber.get(ordinalNumber).add(repeatRow);
    }
    if (repeatRowsPerOrdinalNumber.size() != maxOrdinalNumber)
      LOGGER.error("Repeat table " + submission.getTableName() + " has dupes or missing rows for top level auri " + submission.getTopLevelAuri() + " and parent auri " + submission.getParentAuri());
    return repeatRowsPerOrdinalNumber.values();
  }

  @SuppressWarnings("unchecked")
  private List<CommonFieldsBase> getRepeatRows(CallingContext cc, DynamicBase submission) throws ODKDatastoreException {
    Query q = cc.getDatastore().createQuery(submission, "RepeatSubmissionType.getRepeatRows", cc.getCurrentUser());
    q.addFilter(submission.parentAuri, FilterOperation.EQUAL, uriAssociatedRow);
    q.addSort(submission.parentAuri, Direction.ASCENDING); // for GAE work-around
    q.addSort(submission.ordinalNumber, Direction.ASCENDING);
    return (List<CommonFieldsBase>) q.executeQuery();
  }

  private DynamicBase chooseOneFrom(List<DynamicBase> repeatRows) {
    if (repeatRows.size() == 1)
      return repeatRows.get(0);
    Collections.sort(repeatRows, new Comparator<DynamicBase>() {
      @Override
      public int compare(DynamicBase o1, DynamicBase o2) {
        return o2.getCreationDate().compareTo(o1.getCreationDate());
      }
    });
    return repeatRows.get(0);
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof RepeatSubmissionType)) {
      return false;
    }

    RepeatSubmissionType other = (RepeatSubmissionType) obj;
    return form.equals(other.form) && repeatGroup.equals(other.repeatGroup)
        && submissionSets.equals(other.submissionSets);
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int hashCode = 13;

    hashCode += form.hashCode();
    hashCode += repeatGroup.hashCode();
    hashCode += submissionSets.hashCode();

    return hashCode;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = enclosingSet.constructSubmissionKey(repeatGroup) + "\n";
    for (SubmissionSet set : submissionSets) {
      str += FormatConsts.TO_STRING_DELIMITER + set.toString();
    }
    return str;
  }

  @Override
  public void recursivelyAddEntityKeysForDeletion(List<EntityKey> keyList, CallingContext cc)
      throws ODKDatastoreException {
    // the keyList will be deleted in reverse order.
    // so by adding the repeats in-order, we ensure
    // that the last repeat to delete is ordinal 1.
    // i.e., the submission remains always remains
    // well-formed w.r.t. its repeat groups.
    for (SubmissionSet s : submissionSets) {
      s.recursivelyAddEntityKeysForDeletion(keyList, cc);
    }
  }

  @Override
  public void persist(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException {
    for (SubmissionSet s : submissionSets) {
      s.persist(cc);
    }
  }

  @Override
  public FormElementModel getElement() {
    return repeatGroup;
  }

  @Override
  public String getPropertyName() {
    return repeatGroup.getElementName();
  }

  @Override
  public boolean depthFirstTraversal(SubmissionVisitor visitor) {
    if (visitor.traverse(this))
      return true;

    for (SubmissionSet s : submissionSets) {
      if (s.depthFirstTraversal(visitor))
        return true;
    }
    return false;
  }

  public List<SubmissionValue> findElementValue(FormElementModel element) {
    List<SubmissionValue> values = new ArrayList<SubmissionValue>();

    for (SubmissionSet s : submissionSets) {
      values.addAll(s.findElementValue(element));
    }
    return values;
  }

  @Override
  public SubmissionElement resolveSubmissionKeyBeginningAt(int i, List<SubmissionKeyPart> parts) {
    SubmissionKeyPart p = parts.get(i);

    Long ordinalNumber = p.getOrdinalNumber();
    if (ordinalNumber != null) {
      return submissionSetIndex.get(ordinalNumber).resolveSubmissionKeyBeginningAt(i, parts);
    }

    String auri = p.getAuri();
    if (auri == null) {
      return this; // they want the repeat group...
    }

    for (SubmissionSet s : submissionSets) {
      if (s.getKey().getKey().equals(auri)) {
        return s.resolveSubmissionKeyBeginningAt(i, parts);
      }
    }
    return null;
  }

}
