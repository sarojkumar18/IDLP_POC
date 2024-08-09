/*
 * Copyright 2024 Smarsh Inc.
 */
package com.smarsh.odpa.config;

import com.smarsh.microservice.client.HttpClientWrapper;
import com.smarsh.microservice.client.models.OAuthConfig;
import com.smarsh.microservice.client.models.SyncConfigManager;
import com.smarsh.search.EsSearchRestClient;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ElasticSearchConfig {

  @Bean
  public EsSearchRestClient esSearchRestClient() {
    return new EsSearchRestClient(getHttpClientWrapper(getOauthConfig()),
        "https://search-rest-api.ea-internal.staging.us-west-2.aws.smarsh.cloud",
        "abp-poc");
  }

  private HttpClientWrapper getHttpClientWrapper(OAuthConfig oauthConfig) {
    SyncConfigManager syncConfigManager =
        SyncConfigManager.builder().authConfig(oauthConfig).build();
    return HttpClientWrapper.getInstance(syncConfigManager);
  }

  private OAuthConfig getOauthConfig() {
    return OAuthConfig.builder().authenticationEnabled(true)
        .oauthServerUrl("https://uaa.ea-internal.staging.us-west-2.aws.smarsh.cloud/oauth/token")
        .oauthServerClientId("us-west-2-app-client")
        .oauthServerClientSecret("kXO3FLbAf7xHRmj2RQ82ZLmtlKrgEj").build();
  }
}
