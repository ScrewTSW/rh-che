package com.redhat.che.functional.tests;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.inject.Named;

public class TestTestClass {
    private static final Logger LOG = LoggerFactory.getLogger(TestTestClass.class);

//    @Inject private TestWorkspace testWorkspace;
    @Inject private ProjectExplorer projectExplorer;
    @Inject private Menu menu;

    @Inject
    @Named("che.host")
    private String cheHost;

    @Test
    public void dummyTestCase() {
        LOG.info("Test is running against:"+cheHost);
    }

}
