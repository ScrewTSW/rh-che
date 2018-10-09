package com.redhat.che.selenium.core.workspace;

import org.eclipse.che.selenium.core.workspace.WorkspaceTemplate;

public enum RhCheWorkspaceTemplate {

  // Upstream workspaces
  BROKEN,
  DEFAULT,
  DEFAULT_WITH_GITHUB_PROJECTS,
  ECLIPSE_PHP,
  ECLIPSE_NODEJS,
  ECLIPSE_CPP_GCC,
  ECLIPSE_NODEJS_YAML,
  PYTHON,
  NODEJS_WITH_JSON_LS,
  UBUNTU,
  UBUNTU_GO,
  UBUNTU_JDK8,
  UBUNTU_LSP,
  UBUNTU_CAMEL,

  // RH-Che specific workspaces
  RH_DEFAULT,
  RH_NODEJS;

  private String stack;

  RhCheWorkspaceTemplate() {
    switch (this) {
      case BROKEN:
        this.stack = WorkspaceTemplate.BROKEN.getTemplateFileName();
        break;
      case DEFAULT:
        this.stack = WorkspaceTemplate.DEFAULT.getTemplateFileName();
        break;
      case DEFAULT_WITH_GITHUB_PROJECTS:
        this.stack = WorkspaceTemplate.DEFAULT_WITH_GITHUB_PROJECTS.getTemplateFileName();
        break;
      case ECLIPSE_PHP:
        this.stack = WorkspaceTemplate.ECLIPSE_PHP.getTemplateFileName();
        break;
      case ECLIPSE_NODEJS:
        this.stack = WorkspaceTemplate.ECLIPSE_NODEJS.getTemplateFileName();
        break;
      case ECLIPSE_CPP_GCC:
        this.stack = WorkspaceTemplate.ECLIPSE_CPP_GCC.getTemplateFileName();
        break;
      case ECLIPSE_NODEJS_YAML:
        this.stack = WorkspaceTemplate.ECLIPSE_NODEJS_YAML.getTemplateFileName();
        break;
      case PYTHON:
        this.stack = WorkspaceTemplate.PYTHON.getTemplateFileName();
        break;
      case NODEJS_WITH_JSON_LS:
        this.stack = WorkspaceTemplate.NODEJS_WITH_JSON_LS.getTemplateFileName();
        break;
      case UBUNTU:
        this.stack = WorkspaceTemplate.UBUNTU.getTemplateFileName();
        break;
      case UBUNTU_GO:
        this.stack = WorkspaceTemplate.UBUNTU_GO.getTemplateFileName();
        break;
      case UBUNTU_JDK8:
        this.stack = WorkspaceTemplate.UBUNTU_JDK8.getTemplateFileName();
        break;
      case UBUNTU_LSP:
        this.stack = WorkspaceTemplate.UBUNTU_LSP.getTemplateFileName();
        break;
      case UBUNTU_CAMEL:
        this.stack = WorkspaceTemplate.APACHE_CAMEL.getTemplateFileName();
        break;
      case RH_DEFAULT:
        this.stack = "vertx_default.json";
        break;
      case RH_NODEJS:
        this.stack = "nodejs_default.json";
        break;
      default:
        this.stack = "vertx_default.json";
        break;
    }
  }

  public String getStack() {
    return this.stack;
  }
}
