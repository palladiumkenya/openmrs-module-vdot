/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.vdot.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.vdot.util.FetchVdotPatientInformation;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.io.IOException;

/**
 * Task used to automatically pull data from Vdot system
 */
public class PullPatientDataFromVdotTask extends AbstractTask {
	
	private Log log = LogFactory.getLog(PullPatientDataFromVdotTask.class);
	
	/**
	 * @see AbstractTask#execute()
	 */
	public void execute() {
		Context.openSession();
		FetchVdotPatientInformation fetchVdotPatientInformation = new FetchVdotPatientInformation();
		try {
			fetchVdotPatientInformation.getNimeConfirmVideoObs();
			System.out.println("Successfully executed the task that pulls data from nimeconfirm server");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
