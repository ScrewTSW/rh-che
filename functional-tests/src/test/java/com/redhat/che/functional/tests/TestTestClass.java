package com.redhat.che.functional.tests;

import com.google.inject.Inject;
import com.sun.istack.internal.logging.Logger;
import org.eclipse.che.api.core.model.factory.Ide;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.DialogAbout;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.Test;

public class TestTestClass {
    private static final Logger LOG = Logger.getLogger(TestTestClass.class);

    @Inject private TestWorkspace testWorkspace;
    @Inject private Ide ide;
    @Inject private ProjectExplorer projectExplorer;
    @Inject private Menu menu;
    @Inject private DialogAbout dialogAbout;

    @Test
    public void dummyTestCase() {
        LOG.info("Test is running.");
    }

}
