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

package org.openmrs.module.vdot.metadata;

import org.openmrs.PatientIdentifierType;
import org.openmrs.module.metadatadeploy.bundle.AbstractMetadataBundle;
import org.springframework.stereotype.Component;

import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.*;

/**
 * Vdot metadata bundle
 */
@Component
public class VdotMetadata extends AbstractMetadataBundle {
	
	public static String vdot_concept = "164370AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	public static class _EncounterType {
		
		public static final String VDOT = "d7aaaf20-31ca-4d22-9dd9-0796eb47a341";
		
		public static final String VDOT_CLIENT_ENROLLMENT = "cf805d0a-a470-4194-b375-7e04f56d4dee";
		
		public static final String VDOT_CLIENT_DISCONTINUATION = "90e54c41-da23-4ace-b472-0c8521c97594";
		
	}
	
	public static class _Form {
		
		public static final String VDOT_ENROLLMENT = "197c6ff4-059d-4440-9693-a4bc7520c7b4";
		
		public static final String VDOT_COMPLETION = "967ed26f-2c4d-4cf4-9f49-1c27a8461756";
	}
	
	public static final class _Program {
		
		public static final String VDOT = "b2b2dd4a-3aa5-4c98-93ad-4970b06819ef";
	}
	
	/**
	 * @see AbstractMetadataBundle#install()
	 */
	@Override
	public void install() {
		install(encounterType("Vdot encounter", "Vdot Encounter type", _EncounterType.VDOT));
		install(encounterType("Vdot Enrollment", "Vdot Enrollment Encounter", _EncounterType.VDOT_CLIENT_ENROLLMENT));
		install(encounterType("Vdot Discontinuation", "Vdot Discontinuation Encounter",
		    _EncounterType.VDOT_CLIENT_DISCONTINUATION));
		
		install(form("Vdot Enrollment form", null, _EncounterType.VDOT_CLIENT_ENROLLMENT, "1", _Form.VDOT_ENROLLMENT));
		install(form("Vdot Completion form", null, _EncounterType.VDOT_CLIENT_DISCONTINUATION, "1", _Form.VDOT_COMPLETION));
		
		install(program("Vdot program", "Program for VDOT clients", vdot_concept, _Program.VDOT));
		
	}
}
