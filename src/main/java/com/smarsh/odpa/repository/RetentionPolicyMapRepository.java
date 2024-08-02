/*
 * Copyright 2024 Smarsh Inc.
 */
package com.smarsh.odpa.repository;

import com.mongodb.bulk.BulkWriteResult;
import com.smarsh.odpa.entity.RetentionPolicyMapRecord;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RetentionPolicyMapRepository {

  private static final String GC_ID = "gcId";
  private static final String POLICY_ID = "policyId";
  private static final String START_DATE = "startDate";
  private static final String CHANNEL = "channel";
  private static final String NETWORK = "network";
  private static final String CLUSTER = "clusterId";

  @Autowired
  private MongoTemplate mongoTemplate;

  /**
   * Upserts retention policy map.
   * <p>
   *  For a given GCID , if policyid is already present in
   *  retention policy map collection then update the document
   *  with latest start date .
   * </p>
   *
   * @param retentionPolicyMap Retention policy map entity
   */

  public void upsert(RetentionPolicyMapRecord retentionPolicyMap) {
    try {
      Query query = new Query();
      query.addCriteria(Criteria.where(GC_ID).is(retentionPolicyMap.getGcId())
          .and(POLICY_ID).is(retentionPolicyMap.getPolicyId()));
      Update update = new Update();
      update.max(START_DATE, retentionPolicyMap.getStartDate())
          .setOnInsert(CHANNEL, retentionPolicyMap.getChannel())
          .setOnInsert(NETWORK, retentionPolicyMap.getNetwork())
          .setOnInsert(CLUSTER, retentionPolicyMap.getClusterId());

      mongoTemplate.upsert(query, update, RetentionPolicyMapRecord.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void bulkWrite(List<RetentionPolicyMapRecord> records) {
    BulkOperations bulkInsertion = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,
        RetentionPolicyMapRecord.class);
    bulkInsertion.insert(records);
    BulkWriteResult bulkWriteResult = bulkInsertion.execute();
    System.out.println("RetentionPolicyMap Bulk insert of "+ bulkWriteResult.getInsertedCount());
  }
}
