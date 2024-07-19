/*
 * Copyright 2024 Smarsh Inc.
 */
package com.smarsh.odpa.entity;

import lombok.Data;

import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "s3_attributes")
public class S3AttributesItem {

  private String gcid;

  private String type;

  private String key;

  private Long startTime;
}