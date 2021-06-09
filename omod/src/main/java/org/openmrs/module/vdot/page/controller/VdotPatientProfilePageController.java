/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.vdot.page.controller;

import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.vdot.VdotConstants;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.module.vdot.metadata.VdotMetadata;
import org.openmrs.module.vdot.vdotDataExchange.VdotDataExchange;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Patient Profile pages controller
 */
@AppPage(VdotConstants.APP_VDOT)
public class VdotPatientProfilePageController {
	
	public void controller(@RequestParam("patientId") Patient patient, PageModel model) {
		PatientProgram lastEnrollment = null;//VdotDataExchange.getActiveProgram(patient, VdotMetadata._Program.VDOT_PROGRAM);
		model.put("enrolledInVdot", lastEnrollment != null ? true : true);
	}
}
