package com.redhat.che.selenium.core.client;

import com.redhat.che.selenium.core.workspace.CheStarterWrapper;
import java.io.IOException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.requestfactory.TestUserHttpJsonRequestFactoryCreator;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.user.TestUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RhCheTestWorkspaceServiceClient extends TestWorkspaceServiceClient {

  private static final Logger LOG = LoggerFactory.getLogger(RhCheTestWorkspaceServiceClient.class);
  private static final CheStarterWrapper cheStarterWrapper = CheStarterWrapper.getInstance();

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
  }

  public Workspace createWorkspaceWithCheStarter(String username)
      throws ForbiddenException, BadRequestException, IOException, ConflictException, NotFoundException, ServerException, UnauthorizedException {
    String name = cheStarterWrapper.createWorkspace("create-workspace-request.json");
    return requestFactory.fromUrl(getNameBasedUrl(name, username)).request()
        .asDto(WorkspaceDto.class);
  }

  public void startWithCheStarter(Workspace ws, String name, DefaultTestUser owner) {
    try {
      cheStarterWrapper.startWorkspace(ws, name);
      waitStatus(name, owner.getName(), WorkspaceStatus.RUNNING);
      LOG.info("Workspace " + name + "is running.");
    } catch (Exception e) {
      LOG.error(e.getMessage());
      e.printStackTrace();
    }
  }

}
