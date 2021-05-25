package org.openmrs.module.vdot.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.vdot.util.Utils;
import org.openmrs.module.vdot.vdotDataExchange.VdotDataExchange;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * The main controller.
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/vdata")
public class VdotApplicationRestController extends BaseRestController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * processes incoming vdot observations
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/vdotobservation")
	@ResponseBody
	public Object processVdotObservations(HttpServletRequest request) {
		String requestBody = null;
		try {
			requestBody = Utils.fetchRequestBody(request.getReader());
		}
		catch (IOException e) {
			return new SimpleObject().add("ServerResponse", "Error extracting request body");
		}
		
		if (requestBody != null) {
			VdotDataExchange shr = new VdotDataExchange();
			return shr.processIncomingVdotData(requestBody);
			
		}
		return new SimpleObject().add("Report", "The request could not be interpreted properly");
	}
}
