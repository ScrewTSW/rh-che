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

import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http.HttpMethod;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestClient {

  private static final Logger LOG = LoggerFactory.getLogger(RestClient.class);

  private OkHttpClient client;
  private String serverURL;

  public RestClient(String serverURL) {
    this.serverURL = serverURL;
    this.client = new OkHttpClient.Builder().build();
  }

  public RestClient(String serverURL, long timeout, TimeUnit timeUnit) {
    this.serverURL = serverURL;
    this.client = new OkHttpClient.Builder().readTimeout(timeout, timeUnit).build();
  }

  public void close() {
    try {
      client.dispatcher().executorService().shutdown();
      client.connectionPool().evictAll();
      client.cache().close();
    } catch (Exception e) {
      LOG.info("OkHttpClient threw exception when closing connection:" + e.getMessage(), e);
    }
  }

  public Response sendRequest(String relativePath, String method) throws IOException {
    return sendRequest(relativePath, method, null);
  }

  public Response sendRequest(String relativePath, String method, JsonObject body)
      throws IOException {
    return sendRequest(relativePath, method, body, null, null);
  }

  public Response sendRequest(
      String relativePath,
      String method,
      JsonObject body,
      String authorization,
      Set<Entry<String, String>> queryParams
  ) throws IOException {
    RequestBody requestBody = null;
    StringBuilder urlBuilder = new StringBuilder(serverURL);

    urlBuilder.append(relativePath != null ? relativePath : "");

    if (queryParams != null) {
      urlBuilder.append("?");
      queryParams.forEach(
          entry -> urlBuilder.append(entry.getKey())
              .append("=")
              .append(entry.getValue())
              .append("&")
      );
      urlBuilder.deleteCharAt(urlBuilder.lastIndexOf("&"));
    }

    Builder requestBuilder = new Request.Builder().url(urlBuilder.toString());
    requestBuilder.addHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());

    if (body != null) {
      if (!HttpMethod.permitsRequestBody(method.toUpperCase())) {
        throw new RuntimeException("Incorrect request format: Type "
            + method.toUpperCase()
            + " does not allow request body.");
      }
      requestBody = RequestBody
          .create(MediaType.parse(ContentType.APPLICATION_JSON.getMimeType()), body.getAsString());
    }

    if (authorization != null) {
      requestBuilder.addHeader("Authorization", authorization);
    }

    Request request = null;
    switch (method.toUpperCase()) {
      case "GET":
        request = requestBuilder.get().build();
        break;
      case "POST":
        request = requestBuilder.post(requestBody).build();
        break;
      case "DELETE":
        request = requestBuilder.delete(requestBody).build();
        break;
      case "PUT":
        request = requestBuilder.put(requestBody).build();
        break;
      case "PATCH":
        request = requestBuilder.patch(requestBody).build();
        break;
      default:
        throw new RuntimeException("Unsupported HTTP method:" + method);
    }

    return client.newCall(request).execute();
  }

}
