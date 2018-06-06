package com.redhat.che.selenium.core.client;

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
  private static final CheStarterWrapper cheStarterWrapper = CheStarterWrapper.getInstance();
  private TestUser owner = null;

  public RhCheTestWorkspaceServiceClient(
      TestApiEndpointUrlProvider apiEndpointProvider,
      HttpJsonRequestFactory requestFactory) {
    super(apiEndpointProvider, requestFactory);
  }

  public RhCheTestWorkspaceServiceClient(
      TestApiEndpointUrlProvider apiEndpointProvider,
      TestUserHttpJsonRequestFactoryCreator userHttpJsonRequestFactoryCreator,
      TestUser testUser) {
    super(apiEndpointProvider, userHttpJsonRequestFactoryCreator, testUser);
    this.owner = testUser;
  }

  @Override
  public Workspace createWorkspace(String workspaceName, int memory, MemoryMeasure memoryUnit,
      WorkspaceConfigDto workspace) throws Exception {
    if (owner == null) {
      throw new IllegalStateException("Workspace does not have an owner.");
    }
    String wkspName = cheStarterWrapper.createWorkspace("create-workspace-request.json");
    return requestFactory.fromUrl(getNameBasedUrl(wkspName, owner.getName())).request()
        .asDto(WorkspaceDto.class);
  }

  //TODO: FIX
  @Override
  public void start(String workspaceId, String workspaceName, DefaultTestUser workspaceOwner)
      throws Exception {
    try {
      cheStarterWrapper.startWorkspace(workspaceId, workspaceName);
      waitStatus(workspaceName, workspaceOwner.getName(), WorkspaceStatus.RUNNING);
      LOG.info("Workspace " + workspaceName + "is running.");
    } catch (Exception e) {
      LOG.error(e.getMessage());
      e.printStackTrace();
    }
  }

}
