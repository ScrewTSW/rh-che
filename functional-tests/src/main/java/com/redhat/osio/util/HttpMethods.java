/*
 * Copyright (c) 2016-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.redhat.osio.util;

import org.openqa.selenium.InvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum HttpMethods {

  GET("GET"),
  POST("POST"),
  PUT("PUT"),
  PATCH("PATCH"),
  DELETE("DELETE"),
  HEAD("HEAD"),
  OPTIONS("OPTIONS"),
  PRI("PRI"),
  PROXY("PROXY"),
  TRACE("TRACE"),
  CONNECT("CONNECT"),

  // WebDAV
  PROPPATCH("PROPPATCH"),
  PROPFIND("PROPFIND"),
  MKCOL("MKCOL"),
  LOCK("LOCK"),
  MOVE("MOVE"),
  // CalDAV/CardDAV (defined in WebDAV Versioning)
  REPORT("REPORT"),

  // EVERYTHING ELSE
  UNKNOWN("UNKNOWN");

  private static final Logger LOG = LoggerFactory.getLogger(HttpMethods.class);

  private String methodName;
  HttpMethods(String methodName) {
    this.methodName = methodName;
  }

  public String valueOf() {
    return methodName;
  }

  public HttpMethods parse(String methodName) {
    if (methodName == null)
      throw new InvalidArgumentException("Method name cannot be null.");
    switch(methodName.toUpperCase()) {
      case "GET": return GET;
      case "POST": return POST;
      case "PUT": return PUT;
      case "PATCH": return PATCH;
      case "DELETE": return DELETE;
      case "HEAD": return HEAD;
      case "MOVE": return MOVE;
      case "OPTIONS": return OPTIONS;
      case "PRI": return PRI;
      case "PROXY": return PROXY;
      case "TRACE": return TRACE;
      case "PROPPATCH": return PROPPATCH;
      case "PROPFIND": return PROPFIND;
      case "MKCOL": return MKCOL;
      case "LOCK": return LOCK;
      case "REPORT": return REPORT;
      case "CONNECT": return CONNECT;
      default: LOG.warn("Could not parse unknown HTTPMethod [{}]", methodName);return UNKNOWN;
    }
  }

  /**
   * Courtesy of okhttp3.internal.http
   * @param method <HttpMethods> Method enum to return permission for
   * @return true if HttpMethod requires a body
   */
  public static boolean requiresRequestBody(HttpMethods method) {
    return method.equals(POST)
        || method.equals(PUT)
        || method.equals(PATCH)
        || method.equals(PROPPATCH) // WebDAV
        || method.equals(REPORT);   // CalDAV/CardDAV (defined in WebDAV Versioning)
  }

  /**
   * Courtesy of okhttp3.internal.http
   * @param method <HttpMethods> Method enum to return permission for
   * @return true if HttpMethod allows sending a body
   */
  public static boolean permitsRequestBody(HttpMethods method) {
    return requiresRequestBody(method)
        || method.equals(OPTIONS)
        || method.equals(DELETE)    // Permitted as spec is ambiguous.
        || method.equals(PROPFIND)  // (WebDAV) without body: request <allprop/>
        || method.equals(MKCOL)     // (WebDAV) may contain a body, but behaviour is unspecified
        || method.equals(LOCK);     // (WebDAV) body: create lock, without body: refresh lock
  }

  /**
   * Courtesy of okhttp3.internal.http
   * @param method <HttpMethods> Method enum to return permission for
   * @return true if HttpMethod invalidates cache after call
   */
  public static boolean invalidatesCache(HttpMethods method) {
    return method.equals(POST)
        || method.equals(PATCH)
        || method.equals(PUT)
        || method.equals(DELETE)
        || method.equals(MOVE);     // WebDAV
  }

  /**
   * Courtesy of okhttp3.internal.http
   * @param method <HttpMethods> Method enum to return permission for
   * @return true if HttpMethod retains request body after redirect
   */
  public static boolean redirectsWithBody(HttpMethods method) {
    return method.equals(PROPFIND); // (WebDAV) redirects should also maintain the request body
  }

  /**
   * Courtesy of okhttp3.internal.http
   * @param method <HttpMethods> Method enum to return permission for
   * @return true if HttpMethod redirects to a GET request
   */
  public static boolean redirectsToGet(HttpMethods method) {
    // All requests but PROPFIND should redirect to a GET request.
    return !method.equals(PROPFIND);
  }

}
