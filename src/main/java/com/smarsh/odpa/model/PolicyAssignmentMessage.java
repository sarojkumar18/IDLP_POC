/*
 * Copyright 2024 Smarsh Inc.
 */
package com.smarsh.odpa.model;

import java.io.Serializable;

public final class PolicyAssignmentMessage implements Serializable {
  private String tenantUuid;
  private String requestId;
  private String gcId;
  private String policyId;
  private long startDate;
  private String clusterId;
  private String network;
  private String channel;

  public PolicyAssignmentMessage() {
  }

  public String getTenantUuid() {
    return this.tenantUuid;
  }

  public String getRequestId() {
    return this.requestId;
  }

  public String getGcId() {
    return this.gcId;
  }

  public String getPolicyId() {
    return this.policyId;
  }

  public long getStartDate() {
    return this.startDate;
  }

  public String getClusterId() {
    return this.clusterId;
  }

  public String getNetwork() {
    return this.network;
  }

  public String getChannel() {
    return this.channel;
  }

  public void setTenantUuid(String tenantUuid) {
    this.tenantUuid = tenantUuid;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public void setGcId(String gcId) {
    this.gcId = gcId;
  }

  public void setPolicyId(String policyId) {
    this.policyId = policyId;
  }

  public void setStartDate(long startDate) {
    this.startDate = startDate;
  }

  public void setClusterId(String clusterId) {
    this.clusterId = clusterId;
  }

  public void setNetwork(String network) {
    this.network = network;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }
}
