package com.smarsh.odpa.entity;/*
 * Copyright 2024 Smarsh Inc.
 */

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@EqualsAndHashCode(callSuper = false)
@Document(collection = "custodian_hold_preserved_items")
public class CustodianHoldPreservedItem {
  public static String DELETED = "DELETED";
  public static String ACTIVE = "ACTIVE";
  public static String FAILED_TO_PURGE = "FAILED_TO_PURGE";
  @Id
  @JsonProperty("_id")
  private String id;

  private String gcId;

  private String policyId;

  private String status;

  private List<Long> evaluatedAtList;

  private CustodianHoldMetadataItem metadata;

  private Integer tier;

  /**
   * This adds evaluated at to list.
   *
   * @param evaluatedAt evaluated at
   */
  public void addEvaluatedAt(Long evaluatedAt) {
    if (evaluatedAt == null) {
      return;
    }
    if (null == evaluatedAtList) {
      evaluatedAtList = new ArrayList<>();
    }
    evaluatedAtList.add(evaluatedAt);
  }
}
