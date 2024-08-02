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
import java.util.TimeZone;

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

    List<ArchiveMetrics> fiveHundredElements = validDocs.subList(0, 500);
//    assignPolicyToEachDoc(policy, fiveHundredElements);

    assignPolicyToAllDoc(policy, fiveHundredElements);
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
    long endTimeRetention = System.nanoTime();
    long elapsedTimeInSecRetention = (endTimeRetention - startTimeRetention) / 1_000_000;
    System.out.println("Retention Time Taken: "+ elapsedTimeInSecRetention);

    long startTimes3 = System.nanoTime();
    s3AttributesRepository.bulkWrite(s3AttributesItems);
    long endTimeS3 = System.nanoTime();
    long elapsedTimeInMillisS3 = (endTimeS3 - startTimes3) / 1_000_000;
    System.out.println("S3 Time Taken: "+ elapsedTimeInMillisS3);
  }

  private void assignPolicyToEachDoc(String policy, List<ArchiveMetrics> fiveHundredElements) {
//    long startTimeRetention = System.nanoTime();
    List<Long> retentionPolicyMapTimeList = new ArrayList<>();
    List<Long> s3AttibutesTimeList = new ArrayList<>();
    for (ArchiveMetrics archiveMetrics : fiveHundredElements) {
      insertIntoRetentionPolicyMap(archiveMetrics, policy, retentionPolicyMapTimeList);
      insertIntoS3Attributes(archiveMetrics, policy, s3AttibutesTimeList);
    }
    long minRetention = retentionPolicyMapTimeList.stream().mapToLong(Long::longValue).min().orElseThrow();
    long maxRetention = retentionPolicyMapTimeList.stream().mapToLong(Long::longValue).max().orElseThrow();
    System.out.println("minRetention: "+minRetention +", maxRetention: "+ maxRetention);

    long minS3 = s3AttibutesTimeList.stream().mapToLong(Long::longValue).min().orElseThrow();
    long maxS3 = s3AttibutesTimeList.stream().mapToLong(Long::longValue).max().orElseThrow();
    System.out.println("minS3: "+minS3 +", maxS3: "+ maxS3);

//    long endTimeRetention = System.nanoTime();
//    double elapsedTimeInSecRetention = (endTimeRetention - startTimeRetention) / 1_000_000_000.0;
//    System.out.println("Retention Time Taken: "+ elapsedTimeInSecRetention);
  }

  private void insertIntoS3Attributes(ArchiveMetrics archiveMetrics, String policy, List<Long> retentionPolicyMapTimeList) {
    S3AttributesItem s3AttributesItem = mapToS3AttributeItem(archiveMetrics, policy);
    long startTimeRetention = System.nanoTime();
    s3AttributesRepository.upsert(s3AttributesItem);
    long endTimeRetention = System.nanoTime();
    long elapsedTimeInMillisRetention = (endTimeRetention - startTimeRetention) / 1_000_000;
    retentionPolicyMapTimeList.add(elapsedTimeInMillisRetention);
  }

  private void insertIntoRetentionPolicyMap(ArchiveMetrics archiveMetrics, String policy, List<Long> s3AttibutesTimeList) {
    RetentionPolicyMapRecord retentionPolicyMapRecord = mapToRetentionPolicyMap(archiveMetrics,
        policy);
    long startTimeRetention = System.nanoTime();
    retentionPolicyMapRepository.upsert(retentionPolicyMapRecord);
    long endTimeRetention = System.nanoTime();
    long elapsedTimeInMillisRetention = (endTimeRetention - startTimeRetention) / 1_000_000;
    s3AttibutesTimeList.add(elapsedTimeInMillisRetention);
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
