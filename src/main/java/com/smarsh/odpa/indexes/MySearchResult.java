package com.smarsh.odpa.indexes;/*
 * Copyright 2024 Smarsh Inc.
 */

import com.smarsh.search.model.document.FileCounts;
import com.smarsh.search.model.document.FileMetadata;
import com.smarsh.search.model.document.ParticipantCounts;
import com.smarsh.search.model.document.Tag;
import com.smarsh.search.model.document.Text;
import com.smarsh.search.model.document.TextCounts;
import com.smarsh.search.model.document.Users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MySearchResult {
  private String threadId;
  private String gcid;
  private List<String> transcriptIds;
  private List<String> interactionIds;
  private String cluster;
  private String dedupid;
  private String startTime;
  private String archivedTime;
  private String processedTime;
  private String network;
  private String channel;
  private String communicationType;
  private String importanceFlag;
  private String subject;
  private String fullSubject;
  private List<String> textStatus;
  private ParticipantCounts participantsCount;
  private TextCounts textCounts;
  private FileCounts totalFileCount;
  private Integer failedFileCount;
  private List<String> attributes;
  private Users users;
  private Text text;
  private List<FileMetadata> files;
  private Long contentSize;
  private Long extractedSize;
  private String datatype;
  private String messageId;
  private String bucket;
  private String key;
  private Boolean isDocumentOnHold;
  private List<Tag> tags;
  private Boolean tagfl;
  private String retention;
}
