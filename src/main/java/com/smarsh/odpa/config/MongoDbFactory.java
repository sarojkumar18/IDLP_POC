package com.smarsh.odpa.config;/*
 * Copyright 2024 Smarsh Inc.
 */

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.smarsh.odpa.entity.CustodianHoldPreservedItem;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexInfo;

import java.util.List;

public class MongoDbFactory extends SimpleMongoClientDatabaseFactory {

  public MongoDbFactory(MongoClient mongoClient, String database) {
    super(mongoClient, database);
  }

  @Override
  public MongoDatabase getMongoDatabase() {
    String dbName = "sanity1";
    MongoTemplate mongoTemplate = new MongoTemplate(getMongoClient(), dbName);
    ensureIndexes(mongoTemplate);
    return getMongoClient().getDatabase(dbName);
  }

  private void ensureIndexes(MongoTemplate mongoTemplate) {
    List<IndexInfo> indexInfoList = mongoTemplate
        .indexOps(CustodianHoldPreservedItem.class).getIndexInfo();
    if (indexInfoList.size() < 4) {
      mongoTemplate.indexOps(CustodianHoldPreservedItem.class)
          .ensureIndex(new Index().on("gcId", Sort.Direction.ASC));
      mongoTemplate.indexOps(CustodianHoldPreservedItem.class)
          .ensureIndex(new Index().on("policyId", Sort.Direction.ASC));
      mongoTemplate.indexOps(CustodianHoldPreservedItem.class)
          .ensureIndex(new Index().on("status", Sort.Direction.ASC));
      mongoTemplate.indexOps(CustodianHoldPreservedItem.class)
          .ensureIndex(new Index().on("metadata.retentionPolicyId", Sort.Direction.ASC));
    } else if (indexInfoList.size() < 5) {
      mongoTemplate.indexOps(CustodianHoldPreservedItem.class)
          .ensureIndex(new Index().on("metadata.retentionPolicyId", Sort.Direction.ASC));
    }

  }
}
