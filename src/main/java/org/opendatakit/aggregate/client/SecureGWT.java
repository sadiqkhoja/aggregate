/*
 * Copyright (C) 2011 University of Washington
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

package org.opendatakit.aggregate.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.rpc.RpcRequestBuilder;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.opendatakit.aggregate.client.externalserv.ServicesAdminService;
import org.opendatakit.aggregate.client.externalserv.ServicesAdminServiceAsync;
import org.opendatakit.aggregate.client.filter.FilterService;
import org.opendatakit.aggregate.client.filter.FilterServiceAsync;
import org.opendatakit.aggregate.client.form.FormAdminService;
import org.opendatakit.aggregate.client.form.FormAdminServiceAsync;
import org.opendatakit.aggregate.client.form.FormService;
import org.opendatakit.aggregate.client.form.FormServiceAsync;
import org.opendatakit.aggregate.client.preferences.PreferenceService;
import org.opendatakit.aggregate.client.preferences.PreferenceServiceAsync;
import org.opendatakit.aggregate.client.submission.SubmissionService;
import org.opendatakit.aggregate.client.submission.SubmissionServiceAsync;
import org.opendatakit.common.security.client.security.SecurityService;
import org.opendatakit.common.security.client.security.SecurityServiceAsync;
import org.opendatakit.common.security.client.security.admin.SecurityAdminService;
import org.opendatakit.common.security.client.security.admin.SecurityAdminServiceAsync;

/**
 * This class wraps GWT.create() so that a ODK-specific header can be set on all
 * GWT requests to identify those requests as gwt requests. This allows ODK's
 * GWTAccessDeniedHandlerImpl to detect requests failing the declarative
 * security model in WEB-INF/applicationContext-security.xml and throw an
 * AccessDeniedException back up through the GWT RPC mechanism. Without the
 * header, the failed requests would be redirected to an access-denied.html
 * static page.
 *
 * @author mitchellsundt@gmail.com
 */
public class SecureGWT {
  private static final RpcRequestBuilder reqBuilder = new RpcRequestBuilder() {

    @Override
    protected RequestBuilder doCreate(String serviceEntryPoint) {
      RequestBuilder rb = super.doCreate(serviceEntryPoint);
      rb.setHeader("X-opendatakit-gwt", "yes");
      return rb;
    }

  };
  private static SecureGWT singleton = null;
  /**
   * any user...
   */
  private PreferenceServiceAsync preferenceServiceAsync = null;
  private SecurityServiceAsync securityServiceAsync = null;
  /**
   * data viewer...
   */
  private FilterServiceAsync filterServiceAsync = null;
  private SubmissionServiceAsync submissionServiceAsync = null;
  private FormServiceAsync formServiceAsync = null;
  /**
   * data manager...
   */
  private FormAdminServiceAsync formAdminServiceAsync = null;
  private ServicesAdminServiceAsync servicesAdminServiceAsync = null;
  /**
   * site admin...
   */
  private SecurityAdminServiceAsync securityAdminServiceAsync = null;

  private SecureGWT() {
    preferenceServiceAsync = (PreferenceServiceAsync) create(ServiceType.PREFERENCE);
    securityServiceAsync = (SecurityServiceAsync) create(ServiceType.SECURITY);
    filterServiceAsync = (FilterServiceAsync) create(ServiceType.FILTER);
    submissionServiceAsync = (SubmissionServiceAsync) create(ServiceType.SUBMISSION);
    formServiceAsync = (FormServiceAsync) create(ServiceType.FORM);
    formAdminServiceAsync = (FormAdminServiceAsync) create(ServiceType.FORM_ADMIN);
    servicesAdminServiceAsync = (ServicesAdminServiceAsync) create(ServiceType.SERVICES_ADMIN);
    securityAdminServiceAsync = (SecurityAdminServiceAsync) create(ServiceType.SECURITY_ADMIN);
  }

  public static synchronized final SecureGWT get() {
    if (singleton == null) {
      singleton = new SecureGWT();
    }
    return singleton;
  }

  ;

  /**
   * any user...
   */
  public static PreferenceServiceAsync getPreferenceService() {
    return get().getPreferenceServiceAsync();
  }

  /**
   * any user...
   */
  public static SecurityServiceAsync getSecurityService() {
    return get().getSecurityServiceAsync();
  }

  /**
   * data viewer...
   */
  public static FilterServiceAsync getFilterService() {
    return get().getFilterServiceAsync();
  }

  /**
   * data viewer...
   */
  public static SubmissionServiceAsync getSubmissionService() {
    return get().getSubmissionServiceAsync();
  }

  /**
   * data viewer...
   */
  public static FormServiceAsync getFormService() {
    return get().getFormServiceAsync();
  }

  /**
   * data manager...
   */
  public static FormAdminServiceAsync getFormAdminService() {
    return get().getFormAdminServiceAsync();
  }

  /**
   * data manager...
   */
  public static ServicesAdminServiceAsync getServicesAdminService() {
    return get().getServicesAdminServiceAsync();
  }

  /**
   * site admin...
   */
  public static SecurityAdminServiceAsync getSecurityAdminService() {
    return get().getSecurityAdminServiceAsync();
  }

  public PreferenceServiceAsync getPreferenceServiceAsync() {
    return preferenceServiceAsync;
  }

  public SecurityServiceAsync getSecurityServiceAsync() {
    return securityServiceAsync;
  }

  public FilterServiceAsync getFilterServiceAsync() {
    return filterServiceAsync;
  }

  public SubmissionServiceAsync getSubmissionServiceAsync() {
    return submissionServiceAsync;
  }

  public FormServiceAsync getFormServiceAsync() {
    return formServiceAsync;
  }

  public FormAdminServiceAsync getFormAdminServiceAsync() {
    return formAdminServiceAsync;
  }

  public ServicesAdminServiceAsync getServicesAdminServiceAsync() {
    return servicesAdminServiceAsync;
  }

  public SecurityAdminServiceAsync getSecurityAdminServiceAsync() {
    return securityAdminServiceAsync;
  }

  private Object create(ServiceType type) {
    Object obj = null;
    switch (type) {
      case FILTER:
        obj = GWT.create(FilterService.class);
        break;
      case FORM:
        obj = GWT.create(FormService.class);
        break;
      case FORM_ADMIN:
        obj = GWT.create(FormAdminService.class);
        break;
      case PREFERENCE:
        obj = GWT.create(PreferenceService.class);
        break;
      case SECURITY:
        obj = GWT.create(SecurityService.class);
        break;
      case SECURITY_ADMIN:
        obj = GWT.create(SecurityAdminService.class);
        break;
      case SERVICES_ADMIN:
        obj = GWT.create(ServicesAdminService.class);
        break;
      case SUBMISSION:
        obj = GWT.create(SubmissionService.class);
        break;
      default:
        throw new IllegalStateException("Unrecognized type " + type.toString());
    }
    ServiceDefTarget sd = (ServiceDefTarget) obj;
    sd.setRpcRequestBuilder(reqBuilder);
    return obj;
  }

  public enum ServiceType {
    FILTER, FORM, FORM_ADMIN, PREFERENCE, SECURITY, SECURITY_ADMIN, SERVICES_ADMIN, SUBMISSION
  }
}
