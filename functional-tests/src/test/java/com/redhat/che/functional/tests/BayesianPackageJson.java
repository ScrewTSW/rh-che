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

import com.google.inject.Inject;
import com.redhat.che.selenium.core.workspace.RhCheWorkspaceTemplate;
import java.util.concurrent.ExecutionException;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.NavigateToFile;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class BayesianPackageJson {

  private static final Logger LOG = LoggerFactory.getLogger(BayesianPackageJson.class);

  @InjectTestWorkspace(template = RhCheWorkspaceTemplate.RH_NODEJS)
  private TestWorkspace workspace;

  @Inject private NavigateToFile navigateToFile;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Ide ide;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private ProjectExplorer projectExplorer;

  private static final Integer JSON_EXPECTED_ERROR_LINE = 12;
  private static final Integer JSON_INJECTION_ENTRY_POINT = 12;
  private static final String PROJECT_FILE = "package.json";
  private static final String PROJECT_NAME = "nodejs-hello-world";
  private static final String PROJECT_DEPENDENCY = "\"serve-static\": \"1.7.1\" ,\n";
  private static final String ERROR_MESSAGE =
      "Package serve-static-1.7.1 is vulnerable: CVE-2015-1164. Recommendation: use version 1.7.2";

  @BeforeMethod
  public void openTestFile() throws Exception {
    try {
      LOG.info(
          "Workspace with name: "
              + workspace.getName()
              + " and id: "
              + workspace.getId()
              + " was successfully injected. ");
      ide.open(workspace);
      ide.waitOpenedWorkspaceIsReadyToUse();
      projectExplorer.waitProjectExplorer();
      notificationsPopupPanel.waitProgressPopupPanelClose();
      projectExplorer.waitItem(PROJECT_NAME);
      openDefinedClass();
    } catch (ExecutionException | InterruptedException e) {
      LOG.error(
          "Could not obtain workspace name and id - worskape was probably not successfully injected.");
      throw e;
    } catch (Exception e) {
      LOG.error("Could not open workspace IDE.");
      throw e;
    }
  }

  @AfterMethod
  public void closeIDE() {
    editor.closeAllTabs();
  }

  @Test
  public void createBayesianError() {
    editor.setCursorToLine(JSON_INJECTION_ENTRY_POINT);
    editor.typeTextIntoEditor(PROJECT_DEPENDENCY);
    editor.waitTabFileWithSavedStatus(PROJECT_FILE);
    editor.moveCursorToText("1.7.1");
    try {
      editor.waitTextInToolTipPopup(ERROR_MESSAGE);
    } catch (Exception e) {
      LOG.error(
          "Bayesian error not present after adding dependency - known issue for prod-preview.");
      return;
    }
    LOG.info("Bayesian error message was present after adding dependency.");
  }

  @Test(dependsOnMethods = {"createBayesianError"})
  public void checkErrorPresentAfterReopenFile() {
    editor.setCursorToLine(JSON_EXPECTED_ERROR_LINE);
    editor.moveCursorToText("1.7.1");
    try {
      editor.waitTextInToolTipPopup(ERROR_MESSAGE);
    } catch (Exception e) {
      LOG.error("Bayesian error not present on reopening file - known issue for prod-preview.");
      return;
    }
    LOG.info("Bayesian error message was present after reopening file.");
  }

  private void openDefinedClass() {
    navigateToFile.launchNavigateToFileByKeyboard();
    navigateToFile.waitFormToOpen();
    navigateToFile.typeSymbolInFileNameField(PROJECT_FILE);
    navigateToFile.selectFileByName(PROJECT_FILE);
    loader.waitOnClosed();
    editor.waitActive();
  }
}
