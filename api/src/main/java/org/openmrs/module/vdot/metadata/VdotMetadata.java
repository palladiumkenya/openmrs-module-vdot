/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.vdot.metadata;

import org.openmrs.api.AdministrationService;
import org.openmrs.module.metadatadeploy.bundle.AbstractMetadataBundle;
import org.springframework.stereotype.Component;

import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.encounterType;
import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.form;
import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.globalProperty;
import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.program;

/**
 * Vdot metadata bundle
 */
@Component
public class VdotMetadata extends AbstractMetadataBundle {
	
	public static String vdot_concept = "164370AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private AdministrationService administrationService;
	
	public static final String VDOT_LOGIN_URL = "vdot.login_url";
	
	public static final String VDOT_ENROLLMENT_POST_API = "vdot.enrollment_post_api";
	
	public static final String VDOT_OBSERVATION_GET_API = "vdot.observations_get_api";
	
	public static final String VDOT_USER = "vdot.login_user";
	
	public static final String VDOT_PWD = "vdot.login_pwd"; //used for auth api token
	
	public static final String VDOT_LAST_ENROLLMENT_ENCOUNTER = "vdot.lastVdotEnrollment";
	
	public static final class _EncounterType {
		
		public static final String VDOT_ENCOUNTER = "d7aaaf20-31ca-4d22-9dd9-0796eb47a341";
		
		public static final String VDOT_CLIENT_ENROLLMENT = "cf805d0a-a470-4194-b375-7e04f56d4dee";
		
		public static final String VDOT_CLIENT_DISCONTINUATION = "90e54c41-da23-4ace-b472-0c8521c97594";
		
		public static final String VDOT_BASELINE_ENCOUNTER = "e360f35f-e496-4f01-843b-e2894e278b5b";
		
	}
	
	public static final class _Form {
		
		public static final String VDOT_ENROLLMENT = "197c6ff4-059d-4440-9693-a4bc7520c7b4";
		
		public static final String VDOT_COMPLETION = "967ed26f-2c4d-4cf4-9f49-1c27a8461756";
		
		public static final String VDOT_BASELINE = "fc2d5727-8392-4a49-b254-6c7d3138e81d";
		
	}
	
	public static final class _Program {
		
		public static final String VDOT_PROGRAM = "b2b2dd4a-3aa5-4c98-93ad-4970b06819ef";
	}
	
	/**
	 * @see AbstractMetadataBundle#install()
	 */
	@Override
	public void install() {
		install(encounterType("Vdot encounter", "Vdot Encounter type", _EncounterType.VDOT_ENCOUNTER));
		install(encounterType("Vdot Enrollment", "Vdot Enrollment Encounter", _EncounterType.VDOT_CLIENT_ENROLLMENT));
		install(encounterType("Vdot Discontinuation", "Vdot Discontinuation Encounter",
		    _EncounterType.VDOT_CLIENT_DISCONTINUATION));
		install(encounterType("Vdot baseline", "Vdot Encounter type", _EncounterType.VDOT_BASELINE_ENCOUNTER));
		
		// Installing forms
		
		install(form("Vdot Enrollment form", "Vdot Enrollment Form", _EncounterType.VDOT_CLIENT_ENROLLMENT, "1",
		    _Form.VDOT_ENROLLMENT));
		install(form("Vdot Discontinuation form", "Vdot Discontinuation Form", _EncounterType.VDOT_CLIENT_DISCONTINUATION,
		    "1", _Form.VDOT_COMPLETION));
		install(form("Baseline questionaire form", "Vdot baseline Questionaire Form",
		    _EncounterType.VDOT_BASELINE_ENCOUNTER, "1", _Form.VDOT_BASELINE));
		
		//Installing program
		
		install(program("NimeCONFIRM", "Program for NimeCONFIRM clients", vdot_concept, _Program.VDOT_PROGRAM));
		
		install(globalProperty(VDOT_LOGIN_URL, "Vdot login url", null));
		install(globalProperty(VDOT_USER, "Vdot user", null));
		install(globalProperty(VDOT_PWD, "Vdot pwd", null));
		install(globalProperty(VDOT_ENROLLMENT_POST_API, "Vdot endpoint for posting enrollment information", null));
		install(globalProperty(VDOT_OBSERVATION_GET_API, "Vdot endpoint for getting video observations", null));
		install(globalProperty(VDOT_LAST_ENROLLMENT_ENCOUNTER, "Vdot last enrollment encounter id", null));
		
	}
}
