/*
 * Copyright 2024 Smarsh Inc.
 */
package com.smarsh.odpa.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Setter
@Getter
@EqualsAndHashCode(callSuper = false)
@Document(collection = "retention_policy_map")
public class RetentionPolicyMapRecord {

  private String gcId;

  private String policyId;

  private String channel;

  private String clusterId;

  private String network;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Date startDate;

}
