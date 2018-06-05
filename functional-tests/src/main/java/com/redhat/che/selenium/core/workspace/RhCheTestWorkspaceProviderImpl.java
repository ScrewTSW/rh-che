package com.redhat.che.selenium.core.workspace;

import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClientFactory;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.utils.WorkspaceDtoDeserializer;
import org.eclipse.che.selenium.core.workspace.AbstractTestWorkspaceProvider;

public class RhCheTestWorkspaceProviderImpl extends AbstractTestWorkspaceProvider {

  protected RhCheTestWorkspaceProviderImpl(String poolSize, int threads, int defaultMemoryGb,
      DefaultTestUser defaultUser, WorkspaceDtoDeserializer workspaceDtoDeserializer,
      TestWorkspaceServiceClient testWorkspaceServiceClient,
      TestWorkspaceServiceClientFactory testWorkspaceServiceClientFactory) {
    super(poolSize, threads, defaultMemoryGb, defaultUser, workspaceDtoDeserializer,
        testWorkspaceServiceClient, testWorkspaceServiceClientFactory);
  }

  @Override
  protected void initializePool() {

  }


}
