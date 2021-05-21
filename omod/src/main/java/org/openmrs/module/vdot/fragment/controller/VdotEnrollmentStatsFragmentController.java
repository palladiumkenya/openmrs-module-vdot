/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.vdot.fragment.controller;

import org.openmrs.api.context.Context;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.util.PrivilegeConstants;

/**
 * mUzima error queue fragment
 */
public class VdotEnrollmentStatsFragmentController {
	
	public void controller(FragmentModel model) {
		
		Context.addProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
		
		String pendingEnrollment = "select count(*) from kenyaemr_vdot_nimeconfirm_enrolment where status='Pending';";
		String allProcessedEnrollment = "select count(*) from kenyaemr_vdot_nimeconfirm_enrolment where status='Processed';";
		Long totalProcessedEnrollment = (Long) Context.getAdministrationService().executeSQL(allProcessedEnrollment, true)
		        .get(0).get(0);
		Long pendingEnrols = (Long) Context.getAdministrationService().executeSQL(pendingEnrollment, true).get(0).get(0);

		model.put("totalProcessed", totalProcessedEnrollment.intValue());
		model.put("pendingEnrollments", pendingEnrols.intValue());
		Context.removeProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
		
	}
	
}
