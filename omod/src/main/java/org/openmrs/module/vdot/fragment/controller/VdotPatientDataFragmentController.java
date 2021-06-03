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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import org.apache.http.client.utils.URIBuilder;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.vdot.metadata.VdotMetadata;
import org.openmrs.module.vdot.page.controller.DataManagementPageController;
import org.openmrs.module.vdot.util.Utils;
import org.openmrs.module.vdot.vdotDataExchange.VdotDataExchange;
import org.openmrs.ui.framework.page.PageModel;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Controller for vdot patient data fragment
 */
public class VdotPatientDataFragmentController {
	
	public static final String VDOT_PATIENTS_DATA_SERVER_URL = "http://197.248.92.42:88/kenyaemr/patients_data";
	
	private String url = "http://www.google.com:80/index.html";
	
	private final Log log = LogFactory.getLog(DataManagementPageController.class);
	GlobalProperty gpTimeStamp = Context.getAdministrationService().getGlobalPropertyObject(
			"vdotVideoMessages.lastFetchDateAndTime"); // this will store the last time stamp prior to fetching data
	
	public void controller(PageModel model) {
		
	}
	
	/**
	 * Fetch patient video observations from nimeConfirm server based on timestamp and facility
	 * mflcode
	 */
	
	public void getNimeConfirmVideoObs() throws IOException {
		
		GlobalProperty gpServerUrl = Context.getAdministrationService().getGlobalPropertyObject(
		    VdotMetadata.VDOT_OBSERVATION_GET_API);
		String serverUrl = gpServerUrl.getPropertyValue();
		String timeStamp = gpTimeStamp.getPropertyValue();
		
		if (StringUtils.isBlank(serverUrl)) {
			System.out.println("Please set credentials for pulling vdot observations");
			return;
		}
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(SSLContexts.createDefault(),
		        new String[] { "TLSv1.2" }, null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
		try {
			URIBuilder builder = new URIBuilder(serverUrl);
			builder.setParameter("mflCode", Utils.getDefaultLocationMflCode(Utils.getDefaultLocation())).setParameter(
			    "timestamp", timeStamp);
			
			HttpGet getRequest = new HttpGet(builder.build());
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
					VdotDataExchange.processVideoObs(result);
					setVdotTimeStampGlobalProperty(result);
				}
				
			}
			finally {
				response.close();
			}
			
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
		finally {
			httpClient.close();
		}
		
	}
	private void setVdotTimeStampGlobalProperty(String payload) {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = null;
		try {
			jsonNode = mapper.readTree(payload);
			if (jsonNode != null) {
				String lastFetchTimeStamp = jsonNode.get("timestamp").asText();
				gpTimeStamp.setPropertyValue(lastFetchTimeStamp);
				Context.getAdministrationService().saveGlobalProperty(gpTimeStamp);

			}

		} catch (JsonProcessingException e) {
			e.printStackTrace();
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
