package com.smarsh.odpa;

import com.smarsh.odpa.indexes.EsPromotionEvaluation;
import com.smarsh.odpa.service.PolicyAssignmentService;
import com.smarsh.search.model.SearchResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
@Slf4j
@EnableMongoRepositories
public class OnDemandPolicyAssignmentApplication {

	@Autowired
	private PolicyAssignmentService service;

	@Autowired
	private EsPromotionEvaluation esPromotionEvaluation;

	public static void main(String[] args) {
		SpringApplication.run(OnDemandPolicyAssignmentApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void execute() throws IOException {
		String tenantId = "sanity2";
		esPromotionEvaluation.filterFromEsAndPublishDocuments(tenantId, UUID.fromString("4bcebecf-a43c-41ed-9e41-d19f5ab82793"), true);
	}

	private void executePolicyAssignment() throws IOException {
		List<String> gcIds = readFileCSV();
		service.assignPolicyToTheDocuments(gcIds, "741f6584-348e-4157-9dfa-02984e0f9635");
	}

	public List<String> readFileCSV() throws IOException {
		List<String> gcIds = new ArrayList<>();
		ClassPathResource resource = new ClassPathResource("gcid.csv");

		InputStream inputStream = resource.getInputStream();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains("GC_ID") || line.trim().isEmpty()) {
					continue;
				} else {
					gcIds.add(line.replace("\"",""));
				}
			}
		} catch (IOException e) {
      throw new RuntimeException(e);
    }
		return gcIds;
  }

}
