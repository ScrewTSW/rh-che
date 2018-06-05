package com.redhat.che.selenium.core.workspace;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.redhat.che.selenium.core.client.RhCheTestWorkspaceServiceClient;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClientFactory;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.utils.WorkspaceDtoDeserializer;
import org.eclipse.che.selenium.core.workspace.AbstractTestWorkspaceProvider;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.WorkspaceTemplate;

public class RhCheTestWorkspaceProvider extends AbstractTestWorkspaceProvider {

  protected RhCheTestWorkspaceProvider(String poolSize, int threads, int defaultMemoryGb,
      DefaultTestUser defaultUser, WorkspaceDtoDeserializer workspaceDtoDeserializer,
      TestWorkspaceServiceClient testWorkspaceServiceClient,
      TestWorkspaceServiceClientFactory testWorkspaceServiceClientFactory) {
    super(poolSize, threads, defaultMemoryGb, defaultUser, workspaceDtoDeserializer,
        testWorkspaceServiceClient, testWorkspaceServiceClientFactory);
  }

  @Override
  protected void initializePool() {
    LOG.info("Initialize workspace pool with {} entries.", poolSize);
    testWorkspaceQueue = new ArrayBlockingQueue<>(poolSize);
    executor =
        Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder()
                .setNameFormat("WorkspaceInitializer-%d")
                .setDaemon(true)
                .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
                .build());

    executor.scheduleWithFixedDelay(
        () -> {
          while (testWorkspaceQueue.remainingCapacity() != 0) {
            String name = generateName();
            TestWorkspace testWorkspace;
            try {
              testWorkspace =
                  new RhCheTestWorkspaceImpl(
                      defaultUser,
                      defaultMemoryGb,
                      workspaceDtoDeserializer
                          .deserializeWorkspaceTemplate(WorkspaceTemplate.DEFAULT),
                      testWorkspaceServiceClient instanceof RhCheTestWorkspaceServiceClient
                          ? (RhCheTestWorkspaceServiceClient) testWorkspaceServiceClient
                          : null //TODO: IMPLEMENT ME!
                  );
            } catch (Exception e) {
              // scheduled executor service doesn't log any exceptions, so log possible exception
              // here
              LOG.error(e.getLocalizedMessage(), e);
              throw e;
            }
            try {
              if (!testWorkspaceQueue.offer(testWorkspace)) {
                LOG.warn("Workspace {} can't be added into the pool and will be destroyed.", name);
                testWorkspace.delete();
              }
            } catch (Exception e) {
              LOG.warn(
                  "Workspace {} can't be added into the pool and will be destroyed because of: {}",
                  name,
                  e.getMessage());
              testWorkspace.delete();
            }
          }
        },
        0,
        100,
        TimeUnit.MILLISECONDS);
  }

}
