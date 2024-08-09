/*
 * Copyright 2024 Smarsh Inc.
 */
package com.smarsh.odpa.indexes;

import com.actiance.platform.commons.spi.quicksearch.DocumentSearchRequest;
import com.actiance.platform.commons.spi.quicksearch.DocumentSearchRequestHelper;
import com.actiance.platform.commons.spi.quicksearch.DocumentSearchResponse;
import com.actiance.platform.commons.spi.quicksearch.InteractionDocument;
import com.actiance.platform.commons.spi.quicksearch.SearchRequestParam;
import com.actiance.platform.commons.spi.search.IndexPattern;
import com.actiance.platform.commons.spi.search.index.v5.Participant;
import com.actiance.platform.sfab.cis.persistence.index.IdocDAO;
import com.actiance.platform.sfab.cis.persistence.index.utils.ArchiveIndexPatternImpl;
import com.smarsh.purge.exceptions.FailSafeErrorType;
import com.smarsh.purge.exceptions.PurgeFailSafeException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
//@Component
public class IndexHandler {
  private IdocDAO idocDAO;

  public List<String> getSnapshotParticipants(String tenantUUID, String gcId, String clusterId, long snapshotStartTime) {
    long start = System.currentTimeMillis();
    IndexPattern indexPattern = getIndexPattern(tenantUUID, clusterId, snapshotStartTime);
    List<String> participantList = getInternalParticipantsFromGcid(tenantUUID, gcId, snapshotStartTime, indexPattern);
    log.debug("Total time taken to fetch participants for gcId {} - {} mSecs", gcId,
        (System.currentTimeMillis() - start));
    return participantList;
  }
  private List<String> getInternalParticipantsFromGcid(String tenantUUID, String gcId,
      long snapshotStartTime, IndexPattern indexPattern) {
    String tenantId = "dtccprod";

    SearchRequestParam requestParam = new SearchRequestParam();
    requestParam.setGcId(gcId);
    requestParam.setTenantUUID(tenantUUID);
    requestParam.setTenantId(tenantId);
    requestParam.setSnapshotStartTime(snapshotStartTime);
    requestParam.setIncludeExcludeFields(DocumentSearchRequestHelper.PROJECTION_ARCHIVE_PARTICIPANTS);
    DocumentSearchRequest documentSearchRequest = DocumentSearchRequestHelper.buildDocumentSearchRequest(requestParam);

    DocumentSearchResponse documentSearchResponse = idocDAO.search(documentSearchRequest, indexPattern);

    return getParticipantList(tenantUUID, gcId, documentSearchResponse);
  }

  private static @NotNull List<String> getParticipantList(String tenantUUID, String gcId,
      DocumentSearchResponse documentSearchResponse) {
    List<InteractionDocument> documents = documentSearchResponse.getDocsList();
    List<String> participantList = new ArrayList<>();
    if (documents != null && !documents.isEmpty() && documents.get(0) != null) {
      InteractionDocument document = documents.get(0);
      List<Participant> participants = document.getUsers_participants();
      for (Participant participant : participants) {
        participantList.add(participant.getUserId());
      }
    }else {
      throw new PurgeFailSafeException(FailSafeErrorType.ES_DOCUMENT_NOT_FOUND,
          "Documents not found for tenantUUID : " + tenantUUID + " gcId : " + gcId);
    }
    return participantList;
  }

  public IndexPattern getIndexPattern(String tenantUUID, String clusterId, long snapshotStartTime) {
    return ArchiveIndexPatternImpl.getIndexPattern(tenantUUID, clusterId,
        "1", snapshotStartTime,
        "5");
  }
}
