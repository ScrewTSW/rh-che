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

  private final CompletableFuture<Void> future;
  private final DefaultTestUser owner;
  private final AtomicReference<String> id;
  private final AtomicReference<String> workspaceName;
  private final RhCheTestWorkspaceServiceClient workspaceServiceClient;

  RhCheTestWorkspaceImpl(
      String workspaceName,
      DefaultTestUser owner,
      int memoryInGB,
      WorkspaceConfigDto template,
      RhCheTestWorkspaceServiceClient testWorkspaceServiceClient) {
    if (template == null) {
      throw new IllegalStateException("Workspace template cannot be null");
    }
    this.owner = owner;
    this.id = new AtomicReference<>();
    if (testWorkspaceServiceClient == null) {
      throw new IllegalArgumentException(
          "RhCheTestWorkspaceServiceClient is null. Probably couldn't be instantiated?");
    }
    this.workspaceServiceClient = testWorkspaceServiceClient;
    this.workspaceName = new AtomicReference<>(workspaceName);

    this.future = CompletableFuture.runAsync(
        () -> {
          try {
            final Workspace ws = workspaceServiceClient
                .createWorkspace(this.workspaceName.get(), memoryInGB, MemoryMeasure.GB, template);
            id.set(ws.getId());
            long start = System.currentTimeMillis();
            workspaceServiceClient.start(this.id.get(), this.workspaceName.get(), owner);
            LOG.info(
                "Workspace name='{}' id='{}' started in {} sec.",
                this.workspaceName.get(),
                ws.getId(),
                (System.currentTimeMillis() - start) / 1000);
          } catch (Exception e) {
            String errorMessage = format("Workspace name='%s' start failed.",
                this.workspaceName.get());
            LOG.error(errorMessage, e);

            try {
              workspaceServiceClient.delete(this.workspaceName.get(), owner.getName());
            } catch (Exception e1) {
              LOG.error("Failed to remove workspace name='{}' when start is failed.",
                  this.workspaceName.get());
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
    return future.thenApply(aVoid -> workspaceName.get()).get();
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
            workspaceServiceClient.delete(workspaceName.get(), owner.getName());
          } catch (Exception e) {
            throw new RuntimeException(format("Failed to remove workspace '%s'", this), e);
          }
        });
  }

}
