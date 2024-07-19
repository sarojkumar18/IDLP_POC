/*
 * Copyright 2024 Smarsh Inc.
 */
package com.smarsh.odpa.repository;

import com.smarsh.odpa.entity.ArchiveMetrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class ArchiveMetricsRepository {

  private static final String ARCHIVE_METRICS = "archive_metrics";
  private static final String GCID = "gcid";
  private static final String CLUSTER = "cluster";
  private static final String NETWORK = "network";
  private static final String CHANNEL = "channel";
  private static final String START_TIME = "start_time";
  private static final String PROCESSING_STATE = "processing_state";

  @Autowired
  @Qualifier("siteMongoTemplate")
  private MongoTemplate mongoTemplate;

  public Map<String, List<ArchiveMetrics>> getMetadataForDocuments(List<String> gcids) {
    Query query = new Query();
    query.addCriteria(Criteria.where(GCID).in(gcids));
    query.fields()
        .include(GCID, CLUSTER, NETWORK, CHANNEL, START_TIME, PROCESSING_STATE)
        .exclude("_id");

    List<ArchiveMetrics> entities = mongoTemplate.find(query, ArchiveMetrics.class);
    return entities.stream()
        .collect(Collectors.groupingBy(ArchiveMetrics::getGcid));
  }
}
