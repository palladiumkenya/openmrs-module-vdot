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

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.vdot.api.INimeconfirmService;
import org.openmrs.module.vdot.api.NimeconfirmVideoObs;
import org.openmrs.module.vdot.util.PushVdotEnrollmentInformation;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
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
	 * Post enrollment message to nimeconfirm server Uses button to manually post enrollment
	 * information
	 */
	public void postEnrollmentMessage() {
		PushVdotEnrollmentInformation pushVdotEnrollmentInformation = new PushVdotEnrollmentInformation();
		pushVdotEnrollmentInformation.postEnrollmentMessage();
		
	}
	
}
