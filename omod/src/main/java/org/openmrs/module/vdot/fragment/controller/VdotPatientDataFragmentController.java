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
				jsonNode = (ObjectNode) mapper.readTree(getVdotNimeConfirmVideoObs());
				if (jsonNode != null) {
					message = vdotDataExchange.processIncomingVdotData(jsonNode);
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
}
