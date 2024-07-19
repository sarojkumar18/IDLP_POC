/*
 * Copyright 2024 Smarsh Inc.
 */
package com.smarsh.odpa.service;

import com.smarsh.odpa.entity.ArchiveMetrics;
import com.smarsh.odpa.entity.RetentionPolicyMapRecord;
import com.smarsh.odpa.entity.S3AttributesItem;
import com.smarsh.odpa.repository.ArchiveMetricsRepository;
import com.smarsh.odpa.repository.RetentionPolicyMapRepository;
import com.smarsh.odpa.repository.S3AttributesRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Service
public class PolicyAssignmentService {
  private final RetentionPolicyMapRepository retentionPolicyMapRepository;
  private final S3AttributesRepository s3AttributesRepository;
  private final ArchiveMetricsRepository archiveMetricsRepository;

  public PolicyAssignmentService(RetentionPolicyMapRepository retentionPolicyMapRepository,
      S3AttributesRepository s3AttributesRepository,
      ArchiveMetricsRepository archiveMetricsRepository) {
    this.retentionPolicyMapRepository = retentionPolicyMapRepository;
    this.s3AttributesRepository = s3AttributesRepository;
    this.archiveMetricsRepository = archiveMetricsRepository;
  }


  public void assignPolicyToTheDocuments(List<String> gcIds, String policy) {
    Map<String, List<ArchiveMetrics>> metadataForDocuments = archiveMetricsRepository.getMetadataForDocuments(
        gcIds);
    List<ArchiveMetrics> validDocs = new ArrayList<>();
    gcIds.forEach((String gcId) -> {
      List<ArchiveMetrics> snapshots = metadataForDocuments.get(gcId);
      Optional<ArchiveMetrics> latestSnapshot = getLatestSnapshot(snapshots);
      latestSnapshot.ifPresent(validDocs::add);
    });

//    List<ArchiveMetrics> fiveHundredElements = validDocs.subList(0, 500);
//    assignPolicyToEachDoc(policy, fiveHundredElements);

    assignPolicyToAllDoc(policy, validDocs);
  }

  private void assignPolicyToAllDoc(String policy, List<ArchiveMetrics> validDocs) {
    List<S3AttributesItem> s3AttributesItems = validDocs.stream().map(archiveMetrics -> {
      return mapToS3AttributeItem(archiveMetrics, policy);
    }).toList();

    List<RetentionPolicyMapRecord> retentionPolicyMapRecords = validDocs.stream().map(archiveMetrics -> {
      return mapToRetentionPolicyMap(archiveMetrics, policy);
    }).toList();

    long startTimeRetention = System.nanoTime();
    retentionPolicyMapRepository.bulkWrite(retentionPolicyMapRecords);
    s3AttributesRepository.bulkWrite(s3AttributesItems);
    long endTimeRetention = System.nanoTime();
    double elapsedTimeInSecRetention = (endTimeRetention - startTimeRetention) / 1_000_000_000.0;

    System.out.println("Assignment Time Taken: "+ elapsedTimeInSecRetention);
  }

  private void assignPolicyToEachDoc(String policy, List<ArchiveMetrics> fiveHundredElements) {
    long startTimeRetention = System.nanoTime();
    for (ArchiveMetrics archiveMetrics : fiveHundredElements) {
      insertIntoRetentionPolicyMap(archiveMetrics, policy);
      insertIntoS3Attributes(archiveMetrics, policy);
    }
    long endTimeRetention = System.nanoTime();
    double elapsedTimeInSecRetention = (endTimeRetention - startTimeRetention) / 1_000_000_000.0;
    System.out.println("Retention Time Taken: "+ elapsedTimeInSecRetention);
  }

  private void insertIntoS3Attributes(ArchiveMetrics archiveMetrics, String policy) {
    S3AttributesItem s3AttributesItem = mapToS3AttributeItem(archiveMetrics, policy);
    s3AttributesRepository.upsert(s3AttributesItem);
  }

  private void insertIntoRetentionPolicyMap(ArchiveMetrics archiveMetrics, String policy) {
    RetentionPolicyMapRecord retentionPolicyMapRecord = mapToRetentionPolicyMap(archiveMetrics,
        policy);
    retentionPolicyMapRepository.upsert(retentionPolicyMapRecord);
  }

  private RetentionPolicyMapRecord mapToRetentionPolicyMap(ArchiveMetrics archiveMetrics, String policy) {
    RetentionPolicyMapRecord rpm = new RetentionPolicyMapRecord();
    rpm.setPolicyId(policy);
    rpm.setClusterId(archiveMetrics.getCluster());
    rpm.setChannel(archiveMetrics.getChannel());
    rpm.setNetwork(archiveMetrics.getNetwork());
    rpm.setGcId(archiveMetrics.getGcid());
    rpm.setStartDate(getGmtDate(archiveMetrics.getStartTime().getTime()));
    return rpm;
  }

  public Date getGmtDate(long timeInMillis) {
    Calendar gmtCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    gmtCalendar.setTimeInMillis(timeInMillis);
    return gmtCalendar.getTime();
  }

  private Optional<ArchiveMetrics> getLatestSnapshot(List<ArchiveMetrics> snapshots) {
    return snapshots
        .stream()
        .reduce((doc1, doc2) -> {
          if (!doc1.getStartTime().toInstant().isAfter(doc2.getStartTime().toInstant())) {
            return doc2;
          } else {
            return doc1;
          }
        });
  }

  public S3AttributesItem mapToS3AttributeItem(ArchiveMetrics msgInfo, String policyId) {
    S3AttributesItem s3AttributesItem = new S3AttributesItem();
    s3AttributesItem.setGcid(msgInfo.getGcid());
    s3AttributesItem.setKey(policyId);
    s3AttributesItem.setStartTime(msgInfo.getStartTime().getTime());
    s3AttributesItem.setType(msgInfo.getCommunicationType());
    return s3AttributesItem;
  }
}
