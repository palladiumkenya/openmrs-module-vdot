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

import org.openmrs.PatientIdentifierType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.metadatadeploy.bundle.AbstractMetadataBundle;
import org.springframework.stereotype.Component;

import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.encounterType;
import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.form;
import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.patientIdentifierType;
import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.program;
import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.relationshipType;
import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.personAttributeType;
import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.globalProperty;

/**
 * Vdot metadata bundle
 */
@Component
public class VdotMetadata extends AbstractMetadataBundle {
	
	public static String vdot_concept = "164370AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private AdministrationService administrationService;
	
	public static final String VDOT_LOGIN_URL = "vdot.login_url";
	
	public static final String VDOT_POST_PATIENT_URL = "vdot.server_url";
	
	public static final String VDOT_USER = "vdot.login_user";
	
	public static final String VDOT_PWD = "vdot.login_pwd";
	
	public static final String VDOT_LAST_PATIENT_ENTRY = "vdot.lastVdotEnrollment";
	
	public static final class _EncounterType {
		
		public static final String VDOT_ENCOUNTER = "d7aaaf20-31ca-4d22-9dd9-0796eb47a341";
		
		public static final String VDOT_CLIENT_ENROLLMENT = "cf805d0a-a470-4194-b375-7e04f56d4dee";
		
		public static final String VDOT_CLIENT_DISCONTINUATION = "90e54c41-da23-4ace-b472-0c8521c97594";
		
		public static final String EXAMPLE = "d69dedbd-3933-4e44-8292-bea939ce980a";
		
	}
	
	public static final class _Form {
		
		public static final String VDOT_ENROLLMENT = "197c6ff4-059d-4440-9693-a4bc7520c7b4";
		
		public static final String VDOT_COMPLETION = "967ed26f-2c4d-4cf4-9f49-1c27a8461756";
		
		public static final String EXAMPLE = "b694b1bc-2086-47dd-a4ad-ba48f9471e4b";
		
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
		
		install(encounterType("Example encounter", "Just an example", _EncounterType.EXAMPLE));
		
		// Installing forms
		
		install(form("Vdot Enrollment form", "Vdot Enrollment Form", _EncounterType.VDOT_CLIENT_ENROLLMENT, "1",
		    _Form.VDOT_ENROLLMENT));
		install(form("Vdot Discontinuation form", "Vdot Discontinuation Form", _EncounterType.VDOT_CLIENT_DISCONTINUATION,
		    "1", _Form.VDOT_COMPLETION));
		
		install(form("Example form", null, _EncounterType.EXAMPLE, "1", _Form.EXAMPLE));
		
		//Installing program
		
		install(program("Nimeconfirm", "Program for VDOT clients", vdot_concept, _Program.VDOT_PROGRAM));
		
	}
}
