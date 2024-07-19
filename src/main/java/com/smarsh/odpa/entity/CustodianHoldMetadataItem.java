/*
 * Copyright 2024 Smarsh Inc.
 */
package com.smarsh.odpa.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
@EqualsAndHashCode(callSuper = false)
public class CustodianHoldMetadataItem {

  private Long startDate;

  private String retentionPolicyId;

  private String clusterId;

  private String network;

  private String channel;

  private boolean afterUnHold;

  private Set<String> transcriptId;

}
