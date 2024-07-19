package com.smarsh.odpa.config;/*
 * Copyright 2024 Smarsh Inc.
 */

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

public class SiteMongoDbFactory extends SimpleMongoClientDatabaseFactory {

  public SiteMongoDbFactory(MongoClient mongoClient, String databaseName) {
    super(mongoClient, databaseName);
  }

  @Override
  public MongoDatabase getMongoDatabase() {
    String dbName = "sanity1";
    MongoTemplate mongoTemplate = new MongoTemplate(getMongoClient(), dbName);
    return getMongoClient().getDatabase(dbName);
  }
}
