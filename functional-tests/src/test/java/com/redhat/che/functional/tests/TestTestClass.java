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
package com.redhat.che.functional.tests;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.redhat.che.selenium.core.workspace.CheStarterWrapper;
import com.redhat.osio.util.HttpMethods;
import com.redhat.osio.util.RestClient;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import javax.inject.Named;
import okhttp3.Response;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.DefaultHttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Run;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class TestTestClass {

  private static final Logger LOG = LoggerFactory.getLogger(TestTestClass.class);

//  @Inject private TestWorkspace testWorkspace;
//  @Inject private ProjectExplorer projectExplorer;
//  @Inject private Menu menu;

  @Inject
  @Named("che.host")
  private String cheHost;

  @Inject
  private DefaultTestUser defaultTestUser;

  @Inject
  private DefaultHttpJsonRequestFactory defaultHttpJsonRequestFactory;

  @Inject
  private CheStarterWrapper cheStarterWrapper;

  private String serverBaseURL = "https://" + cheHost + "/";
  private JsonParser PARSER = new JsonParser();

  @Test
  public void dummyTestCase() throws Exception {
    // Get required resources
    String createWorkspaceRequestJson = "/requests/workspace/osio/create-workspace-request.json";
    String token = defaultTestUser.obtainAuthToken();

    // Verify che-starter running
    boolean cheStarterStatus;
    try {
      cheStarterStatus = cheStarterWrapper.checkIsRunning();
    } catch (RuntimeException e) {
      LOG.error("Che starter communication failed:" + e.getMessage(), e);
      throw e;
    }
    if (!cheStarterStatus) {
      String errorMsg = "Che starter liveliness probe failed.";
      LOG.error(errorMsg);
      throw new RuntimeException("Che starter liveliness probe failed.");
    }

    // Create workspace
    String workspaceName;
    try {
      workspaceName = cheStarterWrapper.createWorkspace(createWorkspaceRequestJson, token);
    } catch (Exception e) {
      LOG.error("Workspace creation failed:"+e.getMessage(),e);
      throw e;
    }

//    // Start workspace
//    try {
//      cheStarterWrapper.startWorkspace("id", workspaceName, token);
//    } catch (Exception e) {
//      LOG.error("Starting workspace failed:"+e.getMessage(), e);
//      throw e;
//    }
  }

}
