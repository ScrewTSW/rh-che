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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.redhat.osio.util.HttpMethods;
import com.redhat.osio.util.RestClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.stream.Collectors;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Katerina Kanova (kkanova) */
public class CheStarterWrapper {

  private static final Logger LOG = LoggerFactory.getLogger(CheStarterWrapper.class);
  private static final JsonParser PARSER = new JsonParser();

  private String host;
  private RestClient cheStarterClient;
  private RestClient cheServerClient;

  @Inject(optional = true)
  @Named("sys.cheStarterUrl")
  private String cheStarterURL = "http://localhost:10000";

  @Inject
  public CheStarterWrapper(@Named("che.host") String cheHost) {
    this.host = cheHost;
    this.cheStarterClient = new RestClient(this.cheStarterURL);
    this.cheServerClient = new RestClient("https://" + this.host);
  }

  /** Checks whether che-starter is already running. Throws RuntimeException otherwise. */
  public boolean checkIsRunning() throws RuntimeException {
    Response livelinessResponse;
    try {
      livelinessResponse = cheStarterClient.sendRequest(HttpMethods.GET);
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
    return true;
  }

  public String createWorkspace(String pathToJson, String token) throws Exception {
    BufferedReader buffer;
    try {
      buffer =
          new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(pathToJson)));
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
      return getNameFromResponse(
          cheStarterClient.sendRequest(
              relativePath, HttpMethods.POST, body, authorization, queryParameters.entrySet()));
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
    Response response =
        cheStarterClient.sendRequest(
            relativePath, HttpMethods.DELETE, null, authorization, queryParameters.entrySet());
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

  public void startWorkspace(String WorkspaceID, String workspaceName, String token)
      throws Exception {
    String relativePath = "/workspace/" + workspaceName;
    HashMap<String, String> queryParameters = new HashMap<>();
    queryParameters.put("masterUrl", this.host);
    queryParameters.put("namespace", "sth");
    String authorization = "Bearer " + token;
    Response response =
        cheStarterClient.sendRequest(
            relativePath, HttpMethods.PATCH, null, authorization, queryParameters.entrySet());
    if (response.isSuccessful()) {
      relativePath = "/api/workspace/" + WorkspaceID + "/runtime";
      response =
          cheServerClient.sendRequest(relativePath, HttpMethods.POST, null, authorization, null);
      if (response.isSuccessful()) {
        LOG.info("Workspace was started successfully. Waiting for state RUNNING.");
        return;
      }
    }
    LOG.error("Workspace failed to start:", response.body().string());
  }

  private String getNameFromResponse(Response response) throws IOException {
    if (response.isSuccessful() && response.body() != null) {
      String responseBody = response.body().string();
      //      try {
      //        WorkspaceDto workspaceDto = DtoFactory.getInstance().createDtoFromJson(responseBody,
      // WorkspaceDto.class);
      //        String workspaceID = workspaceDto.getId();
      //        String workspaceName = workspaceDto.getConfig().getName();
      //        LOG.info("Workspace successfully created:"+workspaceID+":"+workspaceName);
      //      } catch (Exception e) {
      //        LOG.warn("Failed to parse WorkspaceDto from response JSON:"+e.getMessage(),e);
      //      }
      JsonObject workspaceJsonObject = PARSER.parse(responseBody).getAsJsonObject();
      JsonObject workspaceConfigJsonObject = workspaceJsonObject.get("config").getAsJsonObject();
      String workspaceName = workspaceConfigJsonObject.get("name").getAsString();
      return workspaceName;
    } else {
      String error = "Could not get name from response. Request failed or the response is empty.";
      LOG.error(error);
      throw new RuntimeException(error);
    }
  }
}
