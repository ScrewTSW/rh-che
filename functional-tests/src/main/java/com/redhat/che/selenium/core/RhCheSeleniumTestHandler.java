package com.redhat.che.selenium.core;

import com.google.inject.Module;
import org.eclipse.che.selenium.core.CheSeleniumSuiteModule;
import org.eclipse.che.selenium.core.CheSeleniumWebDriverRelatedModule;
import org.eclipse.che.selenium.core.inject.SeleniumTestHandler;

import java.util.ArrayList;
import java.util.List;

public class RhCheSeleniumTestHandler extends SeleniumTestHandler {
    @Override
    public List<Module> getParentModules() {
        List<Module> modules = new ArrayList<>();
        modules.add(new CheSeleniumSuiteModule());
        modules.add(new RhCheSeleniumSuiteModule());
        return modules;
    }

    @Override
    public List<Module> getChildModules() {
        List<Module> modules = new ArrayList<>();
        modules.add(new CheSeleniumWebDriverRelatedModule());
        return modules;
    }
}
