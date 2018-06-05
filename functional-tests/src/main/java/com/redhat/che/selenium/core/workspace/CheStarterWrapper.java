/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.redhat.che.selenium.core.workspace;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import okhttp3.*;
import okhttp3.Request.Builder;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.selenium.core.requestfactory.TestUserHttpJsonRequestFactory;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.EmbeddedMaven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * @author Anatolii Bazko
 */
public class CheStarterWrapper {

  private static final Logger LOG = LoggerFactory.getLogger(CheStarterWrapper.class);

  @Inject
  @Named("che.host")
  private String host = "rhche.openshift.io";

  @Inject
  TestUserHttpJsonRequestFactory requestFactory;

  String cheStarterURL = "http://localhost:10000";
  String token = "active_token";

  private static CheStarterWrapper cheStarterWrapper;

  private CheStarterWrapper() {
  }

  public static CheStarterWrapper getInstance() {
    if (cheStarterWrapper == null) {
      cheStarterWrapper = new CheStarterWrapper();
    }
    return cheStarterWrapper;
  }

  public void start() {
    try {
      File cheStarterDir =
          new File(System.getProperty("user.dir"), "target" + File.separator + "che-starter");

      cloneGitDirectory(cheStarterDir);

      String osioUrlPart = "openshift.io"; // TODO - get url!
      LOG.info("Running che starter.");
      Properties props = new Properties();
      props.setProperty(
          "OPENSHIFT_TOKEN_URL",
          "https://sso." + osioUrlPart + "/auth/realms/fabric8/broker/openshift-v3/token");
      props.setProperty(
          "GITHUB_TOKEN_URL", "https://auth." + osioUrlPart + "/api/token?for=https://github.com");
      props.setProperty(
          "CHE_SERVER_URL", "https://rhche." + osioUrlPart);
      String pom = cheStarterDir.getAbsolutePath() + File.separator + "pom.xml";
      EmbeddedMaven.forProject(pom)
          .useMaven3Version("3.5.2")
          .setGoals("spring-boot:run")
          .setProperties(props)
          .useAsDaemon()
          .withWaitUntilOutputLineMathes(".*Started Application in.*", 10, TimeUnit.MINUTES)
          .build();

    } catch (GitAPIException e) {
      throw new IllegalStateException(
          "There was a problem with getting the git che-starter repository", e);
    } catch (TimeoutException e) {
      throw new IllegalStateException("The che-starter haven't started within 300 seconds.", e);
    }
  }

  private void cloneGitDirectory(File cheStarterDir) throws GitAPIException {
    LOG.info("Cloning che-starter project.");
    try {
      Git.cloneRepository()
          .setURI("https://github.com/redhat-developer/che-starter")
          .setDirectory(cheStarterDir)
          .call();
    } catch (JGitInternalException ex) {
      // repository already cloned. Do nothing.
    }
  }

  public String createWorkspace(String pathToJson) {
    //return "tcy8y";
    BufferedReader buffer = null;
    try {
      buffer = new BufferedReader(new InputStreamReader(new FileInputStream(new File(pathToJson))));
    } catch (FileNotFoundException e) {
      LOG.error("File with json was not found on address: " + pathToJson, e);
    }
    String json = buffer.lines().collect(Collectors.joining("\n"));
    String path = "/workspace";
    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    RequestBody body = RequestBody.create(JSON, json);
    StringBuilder sb = new StringBuilder(cheStarterURL);
    sb.append(path);
    sb.append("?");
    sb.append("masterUrl=").append(host).append("&").append("namespace=kkanova-che");
    Builder requestBuilder = new Request.Builder().url(sb.toString());
    requestBuilder.addHeader("Content-Type", "application/json");
    requestBuilder.addHeader("Authorization", token);
    Request request = requestBuilder.post(body).build();
    OkHttpClient client = new OkHttpClient();
    try {
      Response response = client.newCall(request).execute();
      String name = getNameFromResponse(response);
      return name;
    } catch (IOException e) {
      LOG.error("Could not send request to che-starter correctly.", e);
    }
    return null;
  }

  private String getNameFromResponse(Response response) {
    try {
      String responseString = response.body().string();
      Object jsonDocument = Configuration.defaultConfiguration().jsonProvider()
          .parse(responseString);
      return JsonPath.read(jsonDocument, "$.config.name");

    } catch (IOException e) {
      LOG.error(e.getLocalizedMessage());
      e.printStackTrace();
    }
    return null;
  }

  public void startWorkspace(Workspace ws, String name) {
    OkHttpClient client = new OkHttpClient();
    String path = "/workspace/" + name;
    StringBuilder sb = new StringBuilder(cheStarterURL);
    sb.append(path);
    sb.append("?");
    sb.append("masterUrl=").append(host).append("&").append("namespace=kkanova-che");
    Builder requestBuilder = new Request.Builder().url(sb.toString());
    requestBuilder.addHeader("Authorization", token);
    RequestBody body = RequestBody.create(null, new byte[0]);
    Request request = requestBuilder.patch(body).build();
    Response response = null;
    try {
      response = client.newCall(request).execute();
      if (response.isSuccessful()) {
        LOG.info("Prepare workspace request send. Starting workspace.");
        sb = new StringBuilder("https://" + host);
        sb.append("/api/workspace/");
        sb.append(ws.getId());
        sb.append("/runtime");
        requestBuilder = new Request.Builder().url(sb.toString());
        requestBuilder.addHeader("Authorization", token);
        request = requestBuilder.post(body).build();
        response = client.newCall(request).execute();
        if (response.isSuccessful()) {
          LOG.info("Workspace was started. Waiting until workspace is running.");
        }
      }
    } catch (IOException e) {
      LOG.error(e.getLocalizedMessage());
      e.printStackTrace();
    }
  }

}


