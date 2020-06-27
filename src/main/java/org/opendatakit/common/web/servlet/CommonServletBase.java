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

package org.opendatakit.common.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.opendatakit.common.security.spring.SpringInternals;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;
import org.opendatakit.common.web.constants.HtmlConsts;
import org.opendatakit.common.web.constants.HtmlStrUtil;
import org.springframework.security.web.savedrequest.SavedRequest;

/**
 * Base class for Servlets that contain useful utilities
 */
@SuppressWarnings("serial")
public abstract class CommonServletBase extends HttpServlet {
  public static final String INSUFFIECENT_PARAMS = "Insuffiecent Parameters Received";

  protected static final String HOST_HEADER = "Host";

  private final String applicationName;

  protected CommonServletBase(String applicationName) {
    this.applicationName = applicationName;
  }

  protected String getRedirectUrl(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      SavedRequest savedRequest = (SavedRequest) session.getAttribute(SpringInternals.SAVED_REQUEST);
      if (savedRequest != null) {
        return savedRequest.getRedirectUrl();
      }
    }
    return null;
  }


  protected String getRedirectUrl(HttpServletRequest request, String defaultUrl) {
    String redirectParamString = getRedirectUrl(request);
    if (redirectParamString == null) {
      // use the redirect query parameter if present...
      redirectParamString = request.getParameter("redirect");
      if (redirectParamString == null || redirectParamString.length() == 0) {
        // otherwise, redirect to defaultUrl
        // and preserve query string (for GWT debugging)
        redirectParamString = defaultUrl;
        String query = request.getQueryString();
        if (query != null && query.length() != 0) {
          redirectParamString += "?" + query;
        }
      }
    }
    return redirectParamString;
  }

  /**
   * Generate HTML header string for web responses. NOTE: beginBasicHtmlResponse
   * and finishBasicHtmlResponse are a paired set of functions.
   * beginBasicHtmlResponse should be called first before adding other
   * information to the http response. When response is finished
   * finishBasicHtmlResponse should be called.
   */
  protected void beginBasicHtmlResponse(String pageName, HttpServletResponse resp,
                                        CallingContext cc) throws IOException {
    beginBasicHtmlResponse(pageName, BasicConsts.EMPTY_STRING, resp, cc);
  }

  protected PrintWriter beginBasicHtmlResponsePreamble(String headContent, HttpServletResponse resp, CallingContext cc) throws IOException {
    resp.addHeader(HOST_HEADER, cc.getServerURL());
    resp.addHeader(HtmlConsts.X_FRAME_OPTIONS, HtmlConsts.X_FRAME_SAMEORIGIN);

    resp.setContentType(HtmlConsts.RESP_TYPE_HTML);
    resp.setCharacterEncoding(HtmlConsts.UTF8_ENCODE);
    PrintWriter out = resp.getWriter();
    out.write(HtmlConsts.HTML_OPEN);
    out.write("<link rel=\"icon\" href=\"" + cc.getWebApplicationURL("favicon.ico") + "\">");

    out.write(HtmlStrUtil.wrapWithHtmlTags(HtmlConsts.HEAD, headContent + HtmlStrUtil.wrapWithHtmlTags(
        HtmlConsts.TITLE, applicationName)));
    out.write(HtmlConsts.BODY_OPEN);
    return out;
  }

  /**
   * Generate HTML header string for web responses. NOTE: beginBasicHtmlResponse
   * and finishBasicHtmlResponse are a paired set of functions.
   * beginBasicHtmlResponse should be called first before adding other
   * information to the http response. When response is finished
   * finishBasicHtmlResponse should be called.
   */
  protected void beginBasicHtmlResponse(String pageName, String headContent, HttpServletResponse resp,
                                        CallingContext cc) throws IOException {
    PrintWriter out = beginBasicHtmlResponsePreamble(headContent, resp, cc);
    out.write(HtmlStrUtil.createBeginTag(HtmlConsts.CENTERING_DIV));
    out.write(HtmlStrUtil.wrapWithHtmlTags(HtmlConsts.H1, pageName));
    out.write(HtmlStrUtil.createEndTag(HtmlConsts.DIV));
  }

  /**
   * Generate HTML footer string for web responses
   */
  protected final void finishBasicHtmlResponse(HttpServletResponse resp) throws IOException {
    resp.getWriter().write(HtmlConsts.BODY_CLOSE + HtmlConsts.HTML_CLOSE);
  }

  /**
   * Generate error response for missing parameters in request
   */
  protected final void sendErrorNotEnoughParams(HttpServletResponse resp) throws IOException {
    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, INSUFFIECENT_PARAMS);
  }

  /**
   * Extract the parameter from HTTP request and return the decoded value.
   * Returns null if parameter not present
   */
  protected final String getParameter(HttpServletRequest req, String parameterName) {
    String parameter = req.getParameter(parameterName);

    // TODO: consider if aggregate should really be passing nulls in parameters
    // TODO: FIX!!! as null happens when parameter not present, but what about passing nulls?
    if (parameter != null) {
      if (parameter.equals(BasicConsts.NULL)) {
        return null;
      }
    }
    return parameter;
  }


  protected final String encodeParameter(String parameter) throws UnsupportedEncodingException {
    return URLEncoder.encode(parameter, HtmlConsts.UTF8_ENCODE);
  }
}
