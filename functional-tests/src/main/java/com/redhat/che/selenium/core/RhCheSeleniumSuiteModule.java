package com.redhat.che.selenium.core;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.selenium.core.configuration.SeleniumTestConfiguration;
import org.eclipse.che.selenium.core.configuration.TestConfiguration;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.user.MultiUserCheDefaultTestUserProvider;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProvider;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.inject.name.Names.named;

public class RhCheSeleniumSuiteModule extends AbstractModule {
    private Logger LOG = LoggerFactory.getLogger(RhCheSeleniumSuiteModule.class);

    private static final String CHE_MULTIUSER_VARIABLE = "CHE_MULTIUSER";
    private static final String CHE_INFRASTRUCTURE_VARIABLE = "CHE_INFRASTRUCTURE";
    @Inject
    @Named("che.host")
    private String cheHost;

    @Override
    protected void configure() {
        TestConfiguration config = new SeleniumTestConfiguration();
        config.getMap().forEach((key, value) -> bindConstant().annotatedWith(named(key)).to(value));
        bind(DefaultTestUser.class).toProvider(MultiUserCheDefaultTestUserProvider.class);
        bind(TestWorkspaceProvider.class).to(TestWorkspaceProviderImpl.class).asEagerSingleton();
        LOG.info("RH-Che Selenium Suite Module successfully loaded for {}",cheHost);
    }
}
