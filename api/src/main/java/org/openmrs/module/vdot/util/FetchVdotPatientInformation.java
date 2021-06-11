package org.openmrs.module.vdot.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.vdot.metadata.VdotMetadata;
import org.openmrs.module.vdot.vdotDataExchange.VdotDataExchange;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

public class FetchVdotPatientInformation {
	
	private String url = "http://www.google.com:80/index.html";
	
	private final Log log = LogFactory.getLog(FetchVdotPatientInformation.class);
	
	GlobalProperty gpTimeStamp = Context.getAdministrationService().getGlobalPropertyObject(
	    "vdotVideoMessages.lastFetchDateAndTime"); // this will store the last time stamp prior to fetching data
	
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
			    "timestamp", "\"" + timeStamp + "\"");
			
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
			
		}
		catch (JsonProcessingException e) {
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
