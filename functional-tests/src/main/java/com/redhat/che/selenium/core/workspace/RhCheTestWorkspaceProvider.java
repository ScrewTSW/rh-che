package com.redhat.che.selenium.core.workspace;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.redhat.che.selenium.core.client.RhCheTestWorkspaceServiceClient;
import com.redhat.che.selenium.core.client.RhCheTestWorkspaceServiceClientFactory;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.inject.Named;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.utils.WorkspaceDtoDeserializer;
import org.eclipse.che.selenium.core.workspace.AbstractTestWorkspaceProvider;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;

public class RhCheTestWorkspaceProvider extends AbstractTestWorkspaceProvider {

  private CheStarterWrapper cheStarterWrapper;

  @Inject
  protected RhCheTestWorkspaceProvider(
      @Named("che.workspace_pool_size") String poolSize,
      @Named("che.threads") int threads,
      @Named("workspace.default_memory_gb") int defaultMemoryGb,
      DefaultTestUser defaultUser,
      WorkspaceDtoDeserializer workspaceDtoDeserializer,
      RhCheTestWorkspaceServiceClient testWorkspaceServiceClient,
      RhCheTestWorkspaceServiceClientFactory testWorkspaceServiceClientFactory,
      CheStarterWrapper cheStarterWrapper) {
    super(poolSize, threads, defaultMemoryGb, defaultUser, workspaceDtoDeserializer,
        testWorkspaceServiceClient, testWorkspaceServiceClientFactory);
    this.cheStarterWrapper = cheStarterWrapper;
  }

  @Override
  public TestWorkspace createWorkspace(TestUser owner, int memoryGB, String template) {
    this.cheStarterWrapper.start();
    return new RhCheTestWorkspaceImpl(
        owner,
        testWorkspaceServiceClient instanceof RhCheTestWorkspaceServiceClient
            ? (RhCheTestWorkspaceServiceClient) testWorkspaceServiceClient
            : null
    );
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
                      testWorkspaceServiceClient instanceof RhCheTestWorkspaceServiceClient
                          ? (RhCheTestWorkspaceServiceClient) testWorkspaceServiceClient
                          : null
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
