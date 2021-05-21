/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.vdot.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.vdot.page.controller.DataManagementPageController;
import org.openmrs.module.vdot.vdotDataExchange.VdotDataExchange;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Controller for vdot patient data fragment
 */
public class VdotPatientDataFragmentController {
	
	public static final String VDOT_PATIENTS_DATA_SERVER_URL = "http://197.248.92.42:88/kenyaemr/patients_data";
	
	private final Log log = LogFactory.getLog(DataManagementPageController.class);
	
	ObjectNode jsonNode = null;
	
	ObjectMapper mapper = new ObjectMapper();
	
	VdotDataExchange vdotDataExchange = new VdotDataExchange();
	
	public void controller(PageModel model) {
		
	}
	
	public String getMessagesFromVdot() {
		boolean isOnline = false;
		String message = "";
		
		if (checkInternetConnectionStatus()) {
			isOnline = true;
			try {
				jsonNode = (ObjectNode) mapper.readTree(payloadString);
				if (jsonNode != null) {
					
					JSONParser parser = new JSONParser();
					try {
						JSONObject jsonObject = (JSONObject) parser.parse(payloadString);
						message = vdotDataExchange.processIncomingVdotData(jsonObject);
					}
					catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return message;
	}
	
	private static String getVdotNimeConfirmVideoObs() throws IOException {
		String vdotServerUrl = Context.getAdministrationService().getGlobalProperty(VDOT_PATIENTS_DATA_SERVER_URL);
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(SSLContexts.createDefault(),
		        new String[] { "TLSv1.2" }, null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
		try {
			HttpGet getRequest = new HttpGet(vdotServerUrl);
			getRequest.addHeader("content-type", "application/json");
			CloseableHttpResponse response = httpClient.execute(getRequest);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				throw new RuntimeException("Failed with HTTP error code : " + statusCode);
			}
			String result = null;
			try {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					result = EntityUtils.toString(entity);
				}
				
			}
			finally {
				response.close();
			}
			return result;
			
		}
		finally {
			httpClient.close();
		}
	}
	
	private boolean checkInternetConnectionStatus() {
		boolean isConnected = false;
		
		try {
			URL url = new URL("https://www.google.com");
			URLConnection connection = url.openConnection();
			connection.connect();
			isConnected = true;
		}
		catch (MalformedURLException e) {
			log.info("Internet is not connected");
		}
		catch (IOException e) {
			log.error("Internet is not connected");
		}
		
		return isConnected;
	}
	
	String payloadString = "{\n"
	        + "  \"timestamp\" : \"2021-05-20 19:01:15\",\n"
	        + "  \"patientsData\" : [\n"
	        + "    {\n"
	        + "      \"cccNo\" : \"13872008237\",\n"
	        + "      \"mflCode\" : 12345,\n"
	        + "      \"adherenceScore\" : 84.45,\n"
	        + "      \"adherenceTime\": \"2021-05-01 19:01:15\",\n"
	        + "      \"patientStatus\" : \"Discontinued\",\n"
	        + "      \"discontinueData\" : {\n"
	        + "        \"dateDiscontinued\" : \"2021-04-16\",\n"
	        + "        \"discontinuationReason\" : \"Died\",\n"
	        + "        \"causeOfDeath\" : \"Other HIV disease resulting in other diseases or conditions leading to death\",\n"
	        + "        \"clinical_report\" : \"The patient .......\"\n"
	        + "      },\n"
	        + "      \"videosTimestamps\" : [\n"
	        + "        \"2021-04-20 19:01:15\", \"2021-04-21 07:00:55\", \"2021-04-21 18:52:30\", \"2021-04-22 07:07:28\", \"2021-04-22 18:58:56\", \"2021-04-23 06:55:45\", \"2021-04-23 18:37:52\", \"2021-04-24 06:51:42\", \"2021-04-24 18:59:00\", \"2021-04-25 06:52:00\", \"2021-04-25 18:56:13\", \"2021-04-26 06:59:41\", \"2021-04-27 06:45:30\", \"2021-04-27 18:54:07\", \"2021-04-28 06:46:26\", \"2021-04-28 18:51:01\", \"2021-04-29 06:41:41\"\n"
	        + "      ],\n"
	        + "      \"baselineQuestionnaire\" : {\n"
	        + "        \"primaryCaregiver\" : \"\",\n"
	        + "        \"livesWith\" : \"\" ,\n"
	        + "        \"isAwareOfChildHivStatus\" : \"\",\n"
	        + "        \"othersWithHivAtHome\" : \"\",\n"
	        + "        \"totalAtHome\" : \"\",\n"
	        + "        \"goesToSchool\" :  \"\",\n"
	        + "        \"schoolLevel\" :  \"\",\n"
	        + "        \"Boards\" : \"\",\n"
	        + "        \"SchoolDistance\" : \"\",\n"
	        + "        \"meansOfTransport\" : \"\",\n"
	        + "        \"incomeSource\" :  \"\",\n"
	        + "        \"incomeAmount\" : \"\",\n"
	        + "        \"toiletAccess\" : \"\",\n"
	        + "        \"sourceOfWater\" : \"\",\n"
	        + "        \"noOfMeals\" : \"\"\n"
	        + "      }\n"
	        + "    },\n"
	        + "    {\n"
	        + "      \"cccNo\" : \"11222334456\",\n"
	        + "      \"mflCode\" : 12345,\n"
	        + "      \"adherenceScore\" : 84.45,\n"
	        + "      \"adherenceTime\": \"2021-05-01 19:01:15\",\n"
	        + "      \"patientStatus\" : \"Discontinued\",\n"
	        + "      \"discontinueData\" : {\n"
	        + "        \"dateDiscontinued\" : \"2021-04-23\",\n"
	        + "        \"discontinuationReason\" : \"Transferred out\",\n"
	        + "        \"facilityTransferredToCode\" : 23098,\n"
	        + "        \"clinical_report\" : \"The patient .......\"\n"
	        + "      },\n"
	        + "      \"videosTimestamps\" : [\n"
	        + "        \"2021-04-20 19:01:15\", \"2021-04-21 07:00:55\", \"2021-04-21 18:52:30\", \"2021-04-22 07:07:28\", \"2021-04-22 18:58:56\", \"2021-04-23 06:55:45\", \"2021-04-23 18:37:52\", \"2021-04-24 06:51:42\", \"2021-04-24 18:59:00\", \"2021-04-25 06:52:00\", \"2021-04-25 18:56:13\", \"2021-04-26 06:59:41\", \"2021-04-27 06:45:30\", \"2021-04-27 18:54:07\", \"2021-04-28 06:46:26\", \"2021-04-28 18:51:01\", \"2021-04-29 06:41:41\"\n"
	        + "      ]\n"
	        + "    },\n"
	        + "    {\n"
	        + "      \"cccNo\" : \"11222334457\",\n"
	        + "      \"mflCode\" : 12345,\n"
	        + "      \"adherenceScore\" : 84.45,\n"
	        + "      \"adherenceTime\": \"2021-05-01 19:01:15\",\n"
	        + "      \"patientStatus\" : \"Active\",\n"
	        + "      \"videosTimestamps\" : [\n"
	        + "        \"2021-04-20 19:01:15\", \"2021-04-21 07:00:55\", \"2021-04-21 18:52:30\", \"2021-04-22 07:07:28\", \"2021-04-22 18:58:56\", \"2021-04-23 06:55:45\", \"2021-04-23 18:37:52\", \"2021-04-24 06:51:42\", \"2021-04-24 18:59:00\", \"2021-04-25 06:52:00\", \"2021-04-25 18:56:13\", \"2021-04-26 06:59:41\", \"2021-04-27 06:45:30\", \"2021-04-27 18:54:07\", \"2021-04-28 06:46:26\", \"2021-04-28 18:51:01\", \"2021-04-29 06:41:41\"\n"
	        + "      ]\n" + "    }\n" + "  ]\n" + "}";
}
