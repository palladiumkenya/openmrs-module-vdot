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
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.vdot.api.INimeconfirmService;
import org.openmrs.module.vdot.api.NimeconfirmVideoObs;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for vdot observation page
 */
public class VdotPillCalendarFragmentController {
	
	INimeconfirmService service = Context.getService(INimeconfirmService.class);
	
	public void controller(@RequestParam("patientId") Patient patient, PageModel model, UiUtils ui) {
		
		List<NimeconfirmVideoObs> allVideoObs = service.getNimeconfirmVideoObsByPatient(patient);
		String b = "";
		List<String> allVideoTimestamps = new ArrayList<String>();
		if (allVideoObs != null && !allVideoObs.isEmpty()) {
			for (NimeconfirmVideoObs vObs : allVideoObs) {
				allVideoTimestamps.add(vObs.getTimeStamp());
			}
		}
		b = StringUtils.join(allVideoTimestamps, ",");
		
		Map<String, Object> jsonConfig = new LinkedHashMap<String, Object>();
		jsonConfig.put("dateofEnrollment", "2021-04-07");
		if (StringUtils.isNotBlank(b)) {
			jsonConfig.put("dates", Arrays.asList(b.split(",")));
		} else {
			jsonConfig.put("dates", new ArrayList<String>());
		}
		
		model.put("patientData", ui.toJson(jsonConfig));
		
	}
}
