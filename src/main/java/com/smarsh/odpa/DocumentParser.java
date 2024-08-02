/*
 * Copyright 2024 Smarsh Inc.
 */
package com.smarsh.odpa;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class DocumentParser {

  public static void main(String[] args) {
    List<String> strings = extractValuesFromXML(
        "C:\\Users\\saroj.kumar\\workspace\\on-demand-policy-assignment\\src\\main\\resources\\input.xml");
    System.out.println(strings);
  }

  public static List<String> extractValuesFromXML(String filePath) {
    List<String> extractedValues = new ArrayList<>();

    try {
      // Create a DocumentBuilderFactory
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      // Create a DocumentBuilder
      DocumentBuilder builder = factory.newDocumentBuilder();
      // Parse the XML file
      Document document = builder.parse(new File(filePath));

      // Normalize the XML structure
      document.getDocumentElement().normalize();

      // Get all Data nodes in the XML
      NodeList dataNodes = document.getElementsByTagName("Data");

      // Iterate over the nodes and extract values
      for (int i = 1; i < dataNodes.getLength(); i++) {
        Node node = dataNodes.item(i);

        if (node.getNodeType() == Node.ELEMENT_NODE) {
          Element element = (Element) node;
          String textContent = element.getTextContent();
          String[] values = textContent.split("&quot;");
          extractedValues.add(values[0]);
        }
      }
    } catch (ParserConfigurationException | SAXException | IOException e) {
      e.printStackTrace();
      return null;
    }

    return extractedValues;
  }
}
