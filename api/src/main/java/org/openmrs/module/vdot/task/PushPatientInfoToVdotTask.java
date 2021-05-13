/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.vdot.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.vdot.api.NimeconfirmEnrolment;
import org.openmrs.module.vdot.api.NimeconfirmService;
import org.openmrs.module.vdot.metadata.VdotMetadata;
import org.openmrs.module.vdot.vdotDataExchange.VdotDataExchange;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Prepare payload for VDOT application
 */
public class PushPatientInfoToVdotTask extends AbstractTask {
	
	NimeconfirmService nimeconfirmService = Context.getService(NimeconfirmService.class);
	
	private Log log = LogFactory.getLog(PushPatientInfoToVdotTask.class);
	
	/**
	 * @see AbstractTask#execute()
	 */
	public void execute() {
		Context.openSession();
		try {
			
			if (!Context.isAuthenticated()) {
				authenticate();
			}
			
			GlobalProperty gpLoginUrl = Context.getAdministrationService().getGlobalPropertyObject(
			    VdotMetadata.VDOT_LOGIN_URL);
			GlobalProperty gpLoginUser = Context.getAdministrationService().getGlobalPropertyObject(VdotMetadata.VDOT_USER);
			GlobalProperty gpLoginPwd = Context.getAdministrationService().getGlobalPropertyObject(VdotMetadata.VDOT_PWD);
			
			GlobalProperty gpPostVdotUrl = Context.getAdministrationService().getGlobalPropertyObject(
			    VdotMetadata.VDOT_ENROLLMENT_POST_API);
			
			String loginUrl = gpLoginUrl.getPropertyValue();
			String user = gpLoginUser.getPropertyValue();
			String pwd = gpLoginPwd.getPropertyValue();
			
			String serverUrl = gpPostVdotUrl.getPropertyValue();
			
			if (StringUtils.isBlank(loginUrl) || StringUtils.isBlank(user) || StringUtils.isBlank(pwd)
			        || StringUtils.isBlank(serverUrl)) {
				System.out.println("No credentials for posting patient info to vdot application");
				return;
			}
			
			GlobalProperty lastPatientEntry = Context.getAdministrationService().getGlobalPropertyObject(
			    VdotMetadata.VDOT_LAST_PATIENT_ENTRY);
			String sql = "select max(patient_id) last_id from patient_program pp\n"
			        + "join program pr on pr.program_id = pp.program_id\n"
			        + "where voided=0 and pr.uuid =\"b2b2dd4a-3aa5-4c98-93ad-4970b06819ef\";";
			List<List<Object>> lastVdotEnrollment = Context.getAdministrationService().executeSQL(sql, true);
			
			Integer lastPatientId = (Integer) lastVdotEnrollment.get(0).get(0);
			lastPatientId = lastPatientId != null ? lastPatientId : 0;
			
			VdotDataExchange e = new VdotDataExchange();
			ObjectNode payload = e.generatePayloadForVdot(Context.getPatientService().getPatient(lastPatientId));
			Date date = new Date();
			boolean successful = false;
			CloseableHttpClient loginClient = HttpClients.createDefault();
			String token = null;
			NimeconfirmEnrolment outMsg = new NimeconfirmEnrolment(Context.getPatientService().getPatient(lastPatientId),
			        payload.toString(), "Pending", date);
			nimeconfirmService.saveNimeconfirmEnrolment(outMsg);
			
			JsonNodeFactory factory = JsonNodeFactory.instance;
				try {
					//Define a postRequest request
					HttpPost loginRequest = new HttpPost(loginUrl);
					ObjectNode loginObject = factory.objectNode();
					loginObject.put("username", user.trim());
					loginObject.put("password", pwd.trim());
					
					//Set the API media type in http content-type header
					loginRequest.addHeader("content-type", "application/json");
					
					//Set the request post body
					StringEntity userEntity = new StringEntity(loginObject.toString());
					loginRequest.setEntity(userEntity);
					
					//Send the request; It will immediately return the response in HttpResponse object if any
					HttpResponse response = loginClient.execute(loginRequest);
					
					//verify the valid error code first
					int statusCode = response.getStatusLine().getStatusCode();
					if (statusCode != 200) {
						throw new RuntimeException("Failed with HTTP error code : " + statusCode);
					}
					HttpEntity entity = response.getEntity();
					String responseString = EntityUtils.toString(entity, "UTF-8");
					Map<String, Object> responseMap = new ObjectMapper().readValue(responseString, Map.class);
					
					Boolean success = (Boolean) responseMap.get("success");
					token = responseMap.get("token").toString();
					successful = success;
					
				}
				finally {
					//Important: Close the connect
					loginClient.close();
				}
				
				CloseableHttpClient httpClient = HttpClients.createDefault();
				
				String API_KEY = token;
				
				if (successful && API_KEY != null) {
					try {
						//Define a postRequest request
						HttpPost postRequest = new HttpPost(serverUrl);
						
						//Set the API media type in http content-type header
						postRequest.addHeader("content-type", "application/json");
						postRequest.addHeader("Authorization", "Bearer " + API_KEY);
						
						//Set the request post body
						StringEntity userEntity = new StringEntity(payload.toString());
						postRequest.setEntity(userEntity);
						
						//Send the request; It will immediately return the response in HttpResponse object if any
						HttpResponse response = httpClient.execute(postRequest);
						
						//verify the valid error code first
						int statusCode = response.getStatusLine().getStatusCode();
						if (statusCode != 200) {
							throw new RuntimeException("Failed with HTTP error code : " + statusCode);
						}
						System.out.println("Successfully executed the task that pushes vdot data");
						log.info("Successfully executed the task that pushes vdot data");
					}
					finally {
						//Important: Close the connect
						httpClient.close();
					}
					
					lastPatientEntry.setPropertyValue(lastPatientId.toString());
					Context.getAdministrationService().saveGlobalProperty(lastPatientEntry);
					
				} else {
					System.out.println("Login to the vdot application was not successful");
					log.info("Login to the Vdot application was not successful");
				}
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Vdot POST task could not be executed!", e);
		}
	}
}
