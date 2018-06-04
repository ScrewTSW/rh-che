package com.redhat.che.selenium.core;

import com.google.inject.AbstractModule;
import org.eclipse.che.selenium.core.configuration.SeleniumTestConfiguration;
import org.eclipse.che.selenium.core.configuration.TestConfiguration;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.user.MultiUserCheDefaultTestUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.inject.name.Names.named;

public class RhCheSeleniumSuiteModule extends AbstractModule {
    private Logger LOG = LoggerFactory.getLogger(RhCheSeleniumSuiteModule.class);

    private static final String CHE_MULTIUSER_VARIABLE = "CHE_MULTIUSER";
    private static final String CHE_INFRASTRUCTURE_VARIABLE = "CHE_INFRASTRUCTURE";

    @Override
    protected void configure() {
        TestConfiguration config = new SeleniumTestConfiguration();
        config.getMap().forEach((key, value) -> bindConstant().annotatedWith(named(key)).to(value));
        bind(DefaultTestUser.class).toProvider(MultiUserCheDefaultTestUserProvider.class);
        LOG.info("RH-Che Selenium Suite Module successfully loaded.");
    }
}
