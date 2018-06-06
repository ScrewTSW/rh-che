/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.redhat.che.selenium.core.workspace;

import static java.lang.String.format;

import com.redhat.che.selenium.core.client.RhCheTestWorkspaceServiceClient;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PreDestroy;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.MemoryMeasure;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

/**
 * @author Anatolii Bazko
 */
public class RhCheTestWorkspaceImpl implements TestWorkspace {

  private static final Logger LOG = LoggerFactory.getLogger(RhCheTestWorkspaceImpl.class);

  private String name;
  private final CompletableFuture<Void> future;
  private final DefaultTestUser owner;
  private final AtomicReference<String> id;
  private final RhCheTestWorkspaceServiceClient workspaceServiceClient;

  public RhCheTestWorkspaceImpl(
      DefaultTestUser owner,
      RhCheTestWorkspaceServiceClient testWorkspaceServiceClient) {
    this.owner = owner;
    this.id = new AtomicReference<>();
    this.workspaceServiceClient = testWorkspaceServiceClient;

    this.future =
        CompletableFuture.runAsync(
            () -> {
              try {

                final Workspace ws = workspaceServiceClient.createWorkspace();
                name = ws.getConfig().getName();
                long start = System.currentTimeMillis();
                workspaceServiceClient.startWithCheStarter(ws, name, owner);
                LOG.info(
                    "Workspace name='{}' id='{}' started in {} sec.",
                    name,
                    ws.getId(),
                    (System.currentTimeMillis() - start) / 1000);
              } catch (Exception e) {
                String errorMessage = format("Workspace name='%s' start failed.", name);
                LOG.error(errorMessage, e);

                try {
                  workspaceServiceClient.delete(name, owner.getName());
                } catch (Exception e1) {
                  LOG.error("Failed to remove workspace name='{}' when start is failed.", name);
                }

                if (e instanceof IllegalStateException) {
                  Assert.fail("Known issue https://github.com/eclipse/che/issues/8856", e);
                } else {
                  throw new IllegalStateException(errorMessage, e);
                }
              }
            });
  }

  @Override
  public void await() throws InterruptedException, ExecutionException {
    future.get();
  }

  @Override
  public String getName() throws ExecutionException, InterruptedException {
    return future.thenApply(aVoid -> name).get();
  }

  @Override
  public String getId() throws ExecutionException, InterruptedException {
    return future.thenApply(aVoid -> id.get()).get();
  }

  @Override
  public DefaultTestUser getOwner() {
    return owner;
  }

  @PreDestroy
  @Override
  @SuppressWarnings("FutureReturnValueIgnored")
  public void delete() {
    future.thenAccept(
        aVoid -> {
          try {
            workspaceServiceClient.delete(name, owner.getName());
          } catch (Exception e) {
            throw new RuntimeException(format("Failed to remove workspace '%s'", this), e);
          }
        });
  }

}
