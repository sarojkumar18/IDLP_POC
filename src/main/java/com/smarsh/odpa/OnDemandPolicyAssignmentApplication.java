package com.smarsh.odpa;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.smarsh.odpa.model.GcId;
import com.smarsh.odpa.service.PolicyAssignmentService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.util.ResourceUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SpringBootApplication
@Slf4j
@EnableMongoRepositories
public class OnDemandPolicyAssignmentApplication {

	@Autowired
	private PolicyAssignmentService service;

	public static void main(String[] args) {
		SpringApplication.run(OnDemandPolicyAssignmentApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void loadGcIdFromCSVFile() throws IOException {
		List<String> gcIds = readFileCSV();
		service.assignPolicyToTheDocuments(gcIds, "741f6584-348e-4157-9dfa-02984e0f9635");
	}

	public List<String> readFileCSV() throws IOException {
		List<String> gcIds = new ArrayList<>();
		ClassPathResource resource = new ClassPathResource("gcid.csv"); // Replace "data.csv" with your file name

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
