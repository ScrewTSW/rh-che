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
import com.redhat.che.selenium.core.workspace.RhCheTestWorkspaceImpl;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.NavigateToFile;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class BayesianPomJson {

  private static final Logger LOG = LoggerFactory.getLogger(BayesianPomJson.class);

  @Inject private RhCheTestWorkspaceImpl workspace;
  @Inject private NavigateToFile navigateToFile;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Ide ide;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private ProjectExplorer projectExplorer;

  private static final Integer JSON_EXPECTED_ERROR_LINE = 40;
  private static final Integer JSON_INJECTION_ENTRY_POINT = 37;
  private static final String PROJECT_FILE = "package.json";
  private static final String PROJECT_NAME = "nodejs-hello-world";
  private static final String PROJECT_DEPENDENCY =
      "<dependency>\n"
          + "<groupId>ch.qos.logback</groupId>\n"
          + "<artifactId>logback-core</artifactId>\n"
          + "<version>1.1.10</version>\n"
          + "</dependency>\n";
  private static final String ERROR_MESSAGE =
      "Package ch.qos.logback:logback-core-1.1.10 is vulnerable: CVE-2017-5929";

  @Test(priority = 1)
  public void openClass() throws Exception {
    ide.open(workspace);

    // this block of code ensures that workspace and project is ready and pop-ups are gone
    // still doesn't seem to be
    ide.waitOpenedWorkspaceIsReadyToUse();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    notificationsPopupPanel.waitProgressPopupPanelClose();

    openDefinedClass();
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
