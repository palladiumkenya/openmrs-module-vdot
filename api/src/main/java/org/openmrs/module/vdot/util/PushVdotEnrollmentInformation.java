package org.openmrs.module.vdot.util;

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
import org.openmrs.api.context.Context;
import org.openmrs.module.vdot.api.INimeconfirmService;
import org.openmrs.module.vdot.api.NimeconfirmEnrolment;
import org.openmrs.module.vdot.metadata.VdotMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;

public class PushVdotEnrollmentInformation {
	
	private String url = "http://www.google.com:80/index.html";
	
	private static final Logger log = LoggerFactory.getLogger(PushVdotEnrollmentInformation.class);
	
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
						String[] cipherSuites = new String[] { "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
						        "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA" };
						SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(SSLContexts.createDefault(),
						        new String[] { "TLSv1.2" }, cipherSuites,
						        SSLConnectionSocketFactory.getDefaultHostnameVerifier());
						
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
									//	JSONObject errorObj = (JSONObject) responseObj.get("error");
									System.out.println("Error while submitting enrollment sample. " + "Error - "
									        + statusCode + ". Msg" + responseObj.get("message"));
									log.error("Error while submitting enrollment. " + "Error - " + statusCode + ". Msg"
									        + responseObj.get("message"));
								} else {
									
									JSONParser parser = new JSONParser();
									JSONObject responseObj = (JSONObject) parser.parse(EntityUtils.toString(response
									        .getEntity()));
									//JSONObject errorObj = (JSONObject) responseObj.get("error");
									System.out.println("Error while submitting enrollment sample.  " + "Error - "
									        + statusCode + ". Msg" + responseObj.get("message"));
									log.error("Error while submitting enrollment. " + "Error - " + statusCode + ". Msg"
									        + responseObj.get("message"));
									
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
