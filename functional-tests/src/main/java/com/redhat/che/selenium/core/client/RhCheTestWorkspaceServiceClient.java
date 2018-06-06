package com.redhat.che.selenium.core.client;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.redhat.che.selenium.core.workspace.CheStarterWrapper;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.selenium.core.client.AbstractTestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.requestfactory.TestUserHttpJsonRequestFactoryCreator;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.MemoryMeasure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RhCheTestWorkspaceServiceClient extends AbstractTestWorkspaceServiceClient {

  private static final Logger LOG = LoggerFactory.getLogger(RhCheTestWorkspaceServiceClient.class);
  private TestUser owner = null;
  private String token = null;

  @Inject
  private DefaultTestUser defaultTestUser;
  @Inject
  private CheStarterWrapper cheStarterWrapper;

  @Inject
  public RhCheTestWorkspaceServiceClient(
      TestApiEndpointUrlProvider apiEndpointProvider,
      HttpJsonRequestFactory requestFactory) {
    super(apiEndpointProvider, requestFactory);
    LOG.warn("TestWorkspaceServiceClient instantiated with request factory - no owner set.");
    this.owner = this.defaultTestUser;
    this.token = this.owner.obtainAuthToken();
  }

  @AssistedInject
  public RhCheTestWorkspaceServiceClient(
      TestApiEndpointUrlProvider apiEndpointProvider,
      TestUserHttpJsonRequestFactoryCreator userHttpJsonRequestFactoryCreator,
      @Assisted TestUser testUser) {
    super(apiEndpointProvider, userHttpJsonRequestFactoryCreator, testUser);
    LOG.info("TestWorkspaceServiceClient instantiated with RequestFactoryCreator - owner set.");
    this.owner = testUser;
    this.token = this.owner.obtainAuthToken();
  }

  @Override
  public Workspace createWorkspace(String workspaceName, int memory, MemoryMeasure memoryUnit,
      WorkspaceConfigDto workspace) throws Exception {
    if (owner == null) {
      throw new IllegalStateException("Workspace does not have an owner.");
    }
    String name = cheStarterWrapper.createWorkspace("create-workspace-request.json", token);
    return requestFactory.fromUrl(getNameBasedUrl(name, owner.getName())).request()
        .asDto(WorkspaceDto.class);
  }

  @Override
  public void start(String workspaceId, String workspaceName, DefaultTestUser workspaceOwner)
      throws Exception {
    cheStarterWrapper.startWorkspace(workspaceId, workspaceName, token);
    try {
      cheStarterWrapper.startWorkspace(workspaceId, workspaceName, token);
      waitStatus(workspaceName, owner.getName(), WorkspaceStatus.RUNNING);
      LOG.info("Workspace " + workspaceName + "is running.");
    } catch (Exception e) {
      LOG.error(e.getMessage());
      e.printStackTrace();
    }
  }
}
