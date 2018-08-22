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
package com.redhat.che.selenium.core.workspace;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.redhat.osio.util.RestClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.server.ExportException;
import java.util.HashMap;
import java.util.stream.Collectors;
import javax.ws.rs.HttpMethod;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Katerina Kanova (kkanova)
 */
public class CheStarterWrapper {

  private static final Logger LOG = LoggerFactory.getLogger(CheStarterWrapper.class);
  private static final Gson GSON = new Gson();
  private static final JsonParser PARSER = new JsonParser();

  private String host;
  private RestClient cheStarterClient;

  @Inject(optional = true)
  @Named("sys.cheStarterUrl")
  private String cheStarterURL = "http://localhost:10000";

  @Inject
  public CheStarterWrapper(@Named("che.host") String cheHost) {
    this.host = cheHost;
    this.cheStarterClient = new RestClient(this.cheStarterURL);
  }

  /**
   * Checks whether che-starter is already running. Throws RuntimeException otherwise.
   */
  public void checkIsRunning() {
    Response livelinessResponse;
    try {
      livelinessResponse = cheStarterClient.sendRequest("", HttpMethod.GET);
    } catch (Exception e) {
      String errMsg = "Liveliness probe for che-starter failed with exception.";
      LOG.error(errMsg, e);
      throw new RuntimeException(errMsg, e);
    }
    if (livelinessResponse == null) {
      String errMsg = "Liveliness probe for che-starter failed. Response is empty";
      LOG.error(errMsg);
      throw new RuntimeException(errMsg);
    }
    if (livelinessResponse.code() != 200) {
      String errMsg =
          "Liveliness probe for che-starter failed with HTTP code: " + livelinessResponse.code();
      LOG.error(errMsg);
      throw new RuntimeException(errMsg);
    }
  }

  public String createWorkspace(String pathToJson, String token) throws Exception {
    BufferedReader buffer;
    try {
      buffer = new BufferedReader(
          new InputStreamReader(getClass().getResourceAsStream(pathToJson))
      );
    } catch (Exception e) {
      LOG.error("File with json was not found on address: " + pathToJson, e);
      throw e;
    }
    String json = buffer.lines().collect(Collectors.joining("\n"));
    JsonObject body = PARSER.parse(json).getAsJsonObject();
    String relativePath = "/workspace";
    String authorization = "Bearer " + token;
    HashMap<String, String> queryParameters = new HashMap<>();
    queryParameters.put("masterUrl", this.host);
    queryParameters.put("namespace", "sth");

    try {
      return getNameFromResponse(cheStarterClient
          .sendRequest(relativePath, HttpMethod.POST, body, authorization,
              queryParameters.entrySet()));
    } catch (Exception e) {
      LOG.error("Get name from response failed with exception:" + e.getMessage(), e);
      throw new RuntimeException("Failed to parse createWorkspace response.", e);
    }
  }

  public boolean deleteWorkspace(String workspaceName, String token) throws IOException {
    String relativePath = "/workspace/" + workspaceName;
    HashMap<String, String> queryParameters = new HashMap<>();
    queryParameters.put("masterUrl", this.host);
    queryParameters.put("namespace", "sth");
    String authorization = "Bearer " + token;
    Response response = cheStarterClient.sendRequest(relativePath, HttpMethod.DELETE, null, authorization, queryParameters.entrySet());
    if (response.isSuccessful()) {
      if (response.message().equals("Workspace not found")) {
        LOG.warn("\"Workspace could not be deleted because workspace was not found.");
      }
      return true;
    } else {
      LOG.error("Workspace could not be deleted: " + response.message());
      return false;
    }
  }

  public void startWorkspace(String WorkspaceID, String name, String token) throws Exception {
    OkHttpClient client = new OkHttpClient();
    String path = "/workspace/" + name;
    StringBuilder sb = new StringBuilder(this.cheStarterURL);
    sb.append(path);
    sb.append("?");
    sb.append("masterUrl=").append(this.host).append("&").append("namespace=sthf");
    Builder requestBuilder = new Request.Builder().url(sb.toString());
    requestBuilder.addHeader("Authorization", "Bearer " + token);
    RequestBody body = RequestBody.create(null, new byte[0]);
    Request request = requestBuilder.patch(body).build();
    try {
      Response response = client.newCall(request).execute();
      if (response.isSuccessful()) {
        LOG.info("Prepare workspace request send. Starting workspace.");
        sb = new StringBuilder("https://" + this.host);
        sb.append("/api/workspace/");
        sb.append(WorkspaceID);
        sb.append("/runtime");
        requestBuilder = new Request.Builder().url(sb.toString());
        requestBuilder.addHeader("Authorization", "Bearer " + token);
        request = requestBuilder.post(body).build();
        response = client.newCall(request).execute();
        if (response.isSuccessful()) {
          LOG.info("Workspace was started. Waiting until workspace is running.");
        }
      }
    } catch (IOException e) {
      LOG.error("Workspace start failed : " + e.getMessage(), e);
      throw e;
    }
  }

  private String getNameFromResponse(Response response) throws IOException {
    if (response.isSuccessful() && response.body() != null) {
      JsonObject responseObject = PARSER.parse(response.body().string()).getAsJsonObject();
      return JsonPath.read(responseObject.getAsString(), "$.config.name");
    } else {
      String error = "Could not get name from response. Request failed or the response is empty.";
      LOG.error(error);
      throw new RuntimeException(error);
    }
  }

}
