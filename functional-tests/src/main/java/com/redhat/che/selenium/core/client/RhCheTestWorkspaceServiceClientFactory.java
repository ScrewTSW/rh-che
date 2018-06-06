package com.redhat.che.selenium.core.client;

import com.google.inject.assistedinject.Assisted;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClientFactory;
import org.eclipse.che.selenium.core.user.TestUser;

public interface RhCheTestWorkspaceServiceClientFactory extends TestWorkspaceServiceClientFactory {
  @Override
  RhCheTestWorkspaceServiceClient create(@Assisted TestUser testUser);
}
