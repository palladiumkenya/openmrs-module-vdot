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

import org.openmrs.module.vdot.util.FetchVdotPatientInformation;

import org.openmrs.ui.framework.page.PageModel;

import java.io.IOException;

/**
 * Controller for vdot patient data fragment
 */
public class VdotPatientDataFragmentController {
	
	public static final String VDOT_PATIENTS_DATA_SERVER_URL = "http://197.248.92.42:88/kenyaemr/patients_data";
	
	public void controller(PageModel model) {
		
	}
	
	/**
	 * Fetch patient video observations from nimeConfirm server based on timestamp and facility
	 * mflcode Uses button to manually fetch data from nimeConfirm system
	 */
	
	public void getNimeConfirmVideoObs() throws IOException {
		FetchVdotPatientInformation fetchVdotPatientInformation = new FetchVdotPatientInformation();
		fetchVdotPatientInformation.getNimeConfirmVideoObs();
		
	}
	
}
