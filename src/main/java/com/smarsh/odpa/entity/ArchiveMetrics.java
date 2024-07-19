/*
 * Copyright 2024 Smarsh Inc.
 */
package com.smarsh.odpa.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@Setter
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
@Document(collection = "archive_metrics")
public class ArchiveMetrics {

  @Id
  @Field("_id")
  private String id;

  private String gcid;
  private String channel;
  private String cluster;

  @Field("content_source_type")
  private String contentSourceType;

  @Field("dig_buffer_file_key")
  private String digBufferFileKey;

  @Field("inter_id")
  private String interId;

  @Field("last_updated_timestamp")
  private Date lastUpdatedTimestamp;

  @Field("native_size")
  private Integer nativeSize;
  private String network;

  @Field("processing_state")
  private String processingState;

  @Field("received_checksum")
  private String receivedChecksum;

  @Field("received_size")
  private Integer receivedSize;

  @Field("received_time")
  private Date receivedTime;

  @Field("sent_time")
  private Date sentTime;

  @Field("site_id")
  private String siteId;

  @Field("snapshot_id")
  private String snapshotId;

  @Field("start_time")
  private Date startTime;

  @Field("transcript_id")
  private String transcriptId;

  @Field("policy_ids")
  private List<String> policyIds;

  @Field("processed_checksum")
  private String processedChecksum;

  @Field("archived_time")
  private Date archiveDateTime;

  @Field("commtype")
  private String communicationType;

  @Field("participants_checksum")
  private String participantsChecksum;

  @Field("processed_time")
  private Date processedTime;

  @Field("significant_flag")
  private boolean significantFlag;

  @Field("snapshot_action_count")
  private Integer snapshotActionCount;

  @Field("snapshot_checksum")
  private String snapshotChecksum;

  @Field("snapshot_file_count")
  private Integer snapshotFileCount;

  @Field("snapshot_inter_count")
  private Integer snapshotInteractionCount;

  @Field("snapshot_participant_count")
  private Integer snapshotParticipantCount;

  @Field("snapshot_policy_count")
  private Integer snapshotPolicyCount;

  @Field("snapshot_text_count")
  private Integer snapshotTextCount;

  @Field("snapshot_total_count")
  private Integer snapshotTotalCount;

  @Field("text_ext_failures_count")
  private Integer textExtractionFailuresCount;
}
