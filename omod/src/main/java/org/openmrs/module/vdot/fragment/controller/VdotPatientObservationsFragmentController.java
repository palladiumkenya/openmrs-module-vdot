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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.vdot.api.INimeconfirmService;
import org.openmrs.module.vdot.api.NimeconfirmEnrolment;
import org.openmrs.module.vdot.api.NimeconfirmVideoObs;
import org.openmrs.module.vdot.metadata.VdotMetadata;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Controller for vdot observation page
 */
public class VdotPatientObservationsFragmentController {
	
	private static final Logger log = LoggerFactory.getLogger(VdotPatientObservationsFragmentController.class);
	
	private String url = "http://www.google.com:80/index.html";
	
	INimeconfirmService moduleService = Context.getService(INimeconfirmService.class);
	
	public void controller(@RequestParam("patientId") Patient patient, PageModel model, UiUtils ui) {
		List<NimeconfirmVideoObs> videoObs = moduleService.getNimeconfirmVideoObsByPatient(patient);
		List<List<Long>> vdotTrend = new ArrayList<List<Long>>();
		if (videoObs != null && !videoObs.isEmpty()) {
			for (NimeconfirmVideoObs vObs : videoObs) {
				List<Long> dailyScore = new ArrayList<Long>();
				dailyScore.add(vObs.getDate().getTime());
				dailyScore.add(vObs.getScore().longValue());
				vdotTrend.add(dailyScore);
			}
		}
		model.put("vdotTrend", ui.toJson(vdotTrend));
	}
	
	/**
	 * Post enrollment message to nimeconfirm server
	 */
	public void postEnrollmentMessage() {
		
		INimeconfirmService nimeconfirmService = Context.getService(INimeconfirmService.class);
		
		try {
			URLConnection connection = new URL(url).openConnection();
			connection.connect();
			try {
				
				GlobalProperty gpServerUrl = Context.getAdministrationService().getGlobalPropertyObject(
				    VdotMetadata.VDOT_ENROLLMENT_POST_API);
				GlobalProperty gpApiToken = Context.getAdministrationService().getGlobalPropertyObject(
				    VdotMetadata.VDOT_LOGIN_URL);
				
				String serverUrl = gpServerUrl.getPropertyValue();
				//	String API_KEY = gpApiToken.getPropertyValue();
				
				if (StringUtils.isBlank(serverUrl)) {
					System.out.println("Please set credentials for posting  enrollments to the nimeConfirm system");
					return;
				}
				
				// Get a nimeconfirm enrollment
				NimeconfirmEnrolment toProcess = null;
				toProcess = nimeconfirmService.getNimeconfirmEnrolmentByStatus("Pending");
				
				if (toProcess == null) {
					System.out.println("There are no enrollments to push to the nimeConfirm system");
					return;
				}
				
				List<NimeconfirmEnrolment> nimeconfirmEnrolments = nimeconfirmService
				        .getNimeconfirmEnrolmentsToSend(toProcess);
				
				if (nimeconfirmEnrolments.size() < 1) {
					System.out.println("Found no enrollments requests to post");
					return;
				} else {
					for (NimeconfirmEnrolment enrolment : nimeconfirmEnrolments) {
						SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(SSLContexts.createDefault(),
						        new String[] { "TLSv1.2" }, null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
						
						CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
						if (enrolment.getStatus().equalsIgnoreCase("Pending")) {
							
							try {
								
								//Define a postRequest request
								HttpPost postRequest = new HttpPost(serverUrl);
								
								//Set the API media type in http content-type header
								postRequest.addHeader("content-type", "application/json");
								//	postRequest.addHeader("apikey", API_KEY);
								//Set the request post body
								String payload = enrolment.getPayLoad();
								StringEntity userEntity = new StringEntity(payload);
								postRequest.setEntity(userEntity);
								
								//Send the request; It will immediately return the response in HttpResponse object if any
								HttpResponse response = httpClient.execute(postRequest);
								//verify the valid error code first
								int statusCode = response.getStatusLine().getStatusCode();
								if (statusCode == 429) { // too many requests. just terminate
									System.out.println("Many requests please terminate");
									log.warn("Many requests please terminate");
									return;
								}
								
								if (statusCode == 200) {
									enrolment.setStatus("Processed");
									nimeconfirmService.saveNimeconfirmEnrolment(enrolment);
									log.info("Successfully pushed enrollment info with id " + enrolment.getId());
								} else if (statusCode == 412) {
									enrolment.setStatus("Error");
									nimeconfirmService.saveNimeconfirmEnrolment(enrolment);
									
									JSONParser parser = new JSONParser();
									JSONObject responseObj = (JSONObject) parser.parse(EntityUtils.toString(response
									        .getEntity()));
									JSONObject errorObj = (JSONObject) responseObj.get("error");
									System.out.println("Error while submitting enrollment sample. " + "Error - "
									        + statusCode + ". Msg" + errorObj.get("message"));
									log.error("Error while submitting enrollment. " + "Error - " + statusCode + ". Msg"
									        + errorObj.get("message"));
								} else {
									
									JSONParser parser = new JSONParser();
									JSONObject responseObj = (JSONObject) parser.parse(EntityUtils.toString(response
									        .getEntity()));
									JSONObject errorObj = (JSONObject) responseObj.get("error");
									System.out.println("Error while submitting enrollment sample.  " + "Error - "
									        + statusCode + ". Msg" + errorObj.get("message"));
									log.error("Error while submitting enrollment. " + "Error - " + statusCode + ". Msg"
									        + errorObj.get("message"));
									
								}
								Context.flushSession();
							}
							catch (Exception e) {
								System.out.println("Could not push enrollments to the nimeConfirm system! " + e.getCause());
								log.error("Could not push enrollments to the nimeConfirm system! " + e.getCause());
								e.printStackTrace();
							}
							finally {
								httpClient.close();
							}
						}
					}
				}
				
			}
			catch (Exception e) {
				throw new IllegalArgumentException("Unable to  push enrollments ", e);
			}
			finally {
				Context.closeSession();
				
			}
		}
		catch (IOException ioe) {
			
			try {
				String text = "At " + new Date()
				        + " there was an error reported connecting to the internet. Will not attempt pushing enrollments ";
				log.warn(text);
			}
			catch (Exception e) {
				log.error("Failed to check internet connectivity", e);
			}
		}
		
	}
}
