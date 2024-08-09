/*
 * Copyright 2024 Smarsh Inc.
 */
package com.smarsh.odpa.indexes;

import com.smarsh.search.EsSearchRestClient;
import com.smarsh.search.model.DateRange;
import com.smarsh.search.model.DocumentField;
import com.smarsh.search.model.DocumentSearchRequest;
import com.smarsh.search.model.RangeCondition;
import com.smarsh.search.model.SearchResponse;
import com.smarsh.search.model.Sort;
import com.smarsh.search.model.SortOrder;
import com.smarsh.search.model.document.EsSearchResult;
import com.smarsh.search.model.filter.ProcessedDateTimeFilter;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

@Component
@Slf4j
@AllArgsConstructor
public class EsPromotionEvaluation {

  private EsSearchRestClient esSearchRestClient;

  public Instant getBatchStartDate(String tenantId) {
    Instant batchStartDate = null;
    DocumentSearchRequest request = DocumentSearchRequest.builder()
        .tenantUuid("72e4e1b0-9ef1-48bf-922b-f8f0a1fccedf").tenantId(tenantId)
        .filters(List.of(ProcessedDateTimeFilter.builder()
            .fromTime(DateRange.builder().date(0L).rangeCondition(RangeCondition.GT).build())
            .build()))
        .sort(List.of(new Sort(DocumentField.PROCESSED_TIME, SortOrder.Asc)))
        .searchAfterValues(List.of(10101010101L))
        .stopWords(Collections.emptySet()).isStopWordsFilterEnabled(false).size(1).build();
    SearchResponse searchResponse = esSearchRestClient.search(request,
        UUID.randomUUID().toString());
    if (searchResponse == null || searchResponse.getResults() == null
        || searchResponse.getResults().isEmpty()) {
      log.info("Documents are not available in ES for the tenant: " + tenantId);
    } else {
      String processedTime =
          searchResponse.getResults().stream().findFirst().get().getProcessedTime();
      batchStartDate = parseDate(processedTime);
    }
    return batchStartDate;
  }

  public void filterFromEsAndPublishDocuments(String tenantId, UUID policyId,
      boolean isFirstEvaluation) {
    DocumentSearchRequest request = null;
    SearchResponse searchResponse = null;
    try {
      RangeCondition fromProcessedTimeRangeCondition =
          isFirstEvaluation ? RangeCondition.GTE : RangeCondition.GT;

      Instant batchStartDate = getBatchStartDate(tenantId);
      Instant batchEndDate = batchStartDate.atZone(ZoneOffset.UTC).plusDays(3).toInstant();
      request = DocumentSearchRequest.builder()
          .tenantUuid("72e4e1b0-9ef1-48bf-922b-f8f0a1fccedf").tenantId(tenantId)
          .filters(List.of(
              ProcessedDateTimeFilter.builder()
                  .fromTime(DateRange.builder().date(batchStartDate.toEpochMilli())
                      .rangeCondition(fromProcessedTimeRangeCondition).build())
                  .toTime(DateRange.builder().date(batchEndDate.toEpochMilli())
                      .rangeCondition(RangeCondition.LT).build())
                  .build()))
          .sort(List.of(new Sort(DocumentField.PROCESSED_TIME, SortOrder.Desc)))
          .stopWords(Collections.emptySet()).isStopWordsFilterEnabled(false)
          .size(2).build();
      List<Long> searchAfterValues = new ArrayList<Long>();
      Long sortValue = Long.valueOf("1685358415636");
      searchAfterValues.add(sortValue);
      if (!searchAfterValues.isEmpty() && searchAfterValues.get(0) != null) {
        request.setSearchAfterValues(searchAfterValues);
      }
      log.info(
          "Fetching messages from ES from {} to {} for the "
              + "tenant: {} and the policyId: {} with search request {}",
          batchStartDate, batchEndDate, tenantId, policyId,
          request.toString());
      searchResponse = esSearchRestClient.search(request, UUID.randomUUID().toString());
      if (searchResponse == null || searchResponse.getResults() == null
          || searchResponse.getResults().isEmpty()) {
        log.info("No messages found in ES from {} to {} for the tenant: {} and the policyId: {}",
            batchStartDate, batchEndDate, tenantId, policyId);
        log.info("Cannot proceed further");
      } else {
        log.info("Total {} messages available in ES from {} to {} for the tenant: {} and "
                + "the policyId: {}",
            searchResponse.getTotalHits(), batchStartDate, batchEndDate, tenantId, policyId);
        while (searchResponse.getResults() != null && !searchResponse.getResults().isEmpty()) {
          List<EsSearchResult> results = searchResponse.getResults();
          printTheResult(results);
          searchAfterValues = searchResponse.getSort().stream().map(Long::valueOf)
              .toList();
          System.out.println("searchAfterValues = " + searchAfterValues);
          request.setSearchAfterValues(searchAfterValues);
          searchResponse = esSearchRestClient.search(request, UUID.randomUUID().toString());
          log.info("Total Hits: {}, Result Size: {}", searchResponse.getTotalHits(),
              searchResponse.getResults().size());
          System.out.println("---------------------------------------------------");
        }
      }
    } catch (Exception ex) {
      throw ex;
    }
  }

  private void printTheResult(List<EsSearchResult> results) {
//    List<MySearchResult> printableResult = new ArrayList<>();
    for (EsSearchResult result : results) {
      System.out.println("result.getMessageId() = " + result.getMessageId());
//      printableResult.add(MySearchResult.builder()
//          .isDocumentOnHold(result.getIsDocumentOnHold())
//          .archivedTime(result.getArchivedTime())
//          .attributes(result.getAttributes())
//          .bucket(result.getBucket())
//          .channel(result.getChannel())
//          .cluster(result.getCluster())
//          .files(result.getFiles())
//          .communicationType(result.getCommunicationType())
//          .gcid(result.getGcid())
//          .contentSize(result.getContentSize())
//          .key(result.getKey())
//          .datatype(result.getDatatype())
//          .dedupid(result.getDedupid())
//          .extractedSize(result.getExtractedSize())
//          .failedFileCount(result.getFailedFileCount())
//          .fullSubject(result.getFullSubject())
//          .importanceFlag(result.getImportanceFlag())
//          .interactionIds(result.getInteractionIds())
//          .tagfl(result.getTagfl())
//          .messageId(result.getMessageId())
//          .tags(result.getTags())
//          .network(result.getNetwork())
//          .participantsCount(result.getParticipantsCount())
//          .processedTime(result.getProcessedTime())
//          .retention(result.getRetention())
//          .startTime(result.getStartTime())
//          .users(result.getUsers())
//          .subject(result.getSubject())
//          .textCounts(result.getTextCounts())
//          .text(result.getText())
//          .textStatus(result.getTextStatus())
//          .threadId(result.getThreadId())
//          .totalFileCount(result.getTotalFileCount())
//          .transcriptIds(result.getTranscriptIds())
//          .isDocumentOnHold(result.getIsDocumentOnHold())
//          .build());
    }

  }

  private static @NotNull List<Long> getSearchAfterValues(SearchResponse searchResponse) {
    List<String> sortList = searchResponse.getSort();
    List<Long> longList = new ArrayList<>();

    for (String sort : sortList) {
      Long longValue = Long.valueOf(sort);
      longList.add(longValue);
    }
    return longList;
  }

  public static Instant parseDate(String date) {
    Instant instant = null;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSZ");
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    try {
      instant = sdf.parse(date).toInstant();
    } catch (ParseException e) {
      log.error("Invalid Date: " + date);
    }
    return instant;
  }

}
