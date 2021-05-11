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

package org.openmrs.module.vdot.fragment.controller.form;

import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.module.vdot.VdotConstants;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;

/**
 * Home pages controller
 */
@AppPage(VdotConstants.APP_VDOT)
public class VdotPatientObsFragmentController {
	
	public void controller(@RequestParam("patientId") Patient patient, PageModel model) {
		
		List<Obs> obs = Context.getObsService().getObservationsByPerson(patient);
		
		Date lastObs = null;
		if (obs.size() > 0) {
			lastObs = obs.get(obs.size() - 1).getObsDatetime();
		}
		model.addAttribute("date", lastObs);
		model.addAttribute("status", "status");
		model.addAttribute("patient", patient);
		
	}
}
