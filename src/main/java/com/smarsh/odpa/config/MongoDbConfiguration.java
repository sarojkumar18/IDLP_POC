/*
 * Copyright 2024 Smarsh Inc.
 */
package com.smarsh.odpa.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientException;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@Configuration
@Slf4j
public class MongoDbConfiguration extends AbstractMongoClientConfiguration {

  TrustManager[] trustAllCerts = new TrustManager[]{
      new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          return null;
        }

        public void checkClientTrusted(
            java.security.cert.X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(
            java.security.cert.X509Certificate[] certs, String authType) {
        }
      }
  };

  @Override
  @Primary
  @Bean
  public MongoDatabaseFactory mongoDbFactory() {
    return new MongoDbFactory(mongoClient(), "sanity1");
  }

  @Override
  @Bean
  @Primary
  public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory,
      MappingMongoConverter converter) {
    converter.setTypeMapper(new DefaultMongoTypeMapper(null));
    return new MongoTemplate(mongoDbFactory, converter);
  }

  /**
   * This function construct and return the mongo client.
   *
   * @return Mongo client.
   */
  @Bean
  public MongoClient mongoClient() {
    String mongoDbUri = "mongodb://admin:AcTmOnGoIaNcE0014@10.33.148.49:23757/?authSource=admin&authMechanism=SCRAM-SHA-1&ssl=true&tlsAllowInvalidHostnames=true";
    log.debug("MongoURI With size : ", mongoDbUri.length());
    ConnectionString connectString = new ConnectionString(mongoDbUri);
    Boolean sslEnabled = connectString.getSslEnabled();
    MongoClient mongoClient = null;
    try {
      if (sslEnabled != null && sslEnabled.booleanValue()) {
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        mongoClient = MongoClients.create(MongoClientSettings.builder()
            .applyToSslSettings(builder -> builder.context(sslContext))
            .applyConnectionString(new ConnectionString(mongoDbUri))
            .build());

      } else {
        mongoClient = MongoClients.create(MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(mongoDbUri))
            .build());
      }
      return mongoClient;
    } catch (Exception ex) {
      log.error("Failed to create MongoClient", ex);
      throw new MongoClientException("Failed to create MongoClient", ex);
    }
  }

  @Override
  protected String getDatabaseName() {
    return null;
  }
}
