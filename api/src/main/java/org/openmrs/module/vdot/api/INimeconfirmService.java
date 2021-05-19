/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.vdot.api;

import org.openmrs.Patient;
import org.openmrs.api.OpenmrsService;

import java.util.Date;
import java.util.List;

/**
 * The main service of this module, which is exposed for other modules. See
 * moduleApplicationContext.xml on how it is wired up.
 */

public interface INimeconfirmService extends OpenmrsService {
	
	NimeconfirmEnrolment saveNimeconfirmEnrolment(NimeconfirmEnrolment enrolment);
	
	NimeconfirmEnrolment getNimeconfirmEnrolmentByPatient(Patient patient);
	
	List<NimeconfirmEnrolment> getNimeconfirmEnrolment();
	
	List<NimeconfirmEnrolment> getNimeconfirmEnrolmentsToSend(NimeconfirmEnrolment enrolment);
	
	NimeconfirmEnrolment getNimeconfirmEnrolmentByStatus(String status);
	
	void voidNimeconfirmEnrolment(int id);
	
	NimeconfirmEnrolment getNimeconfirmEnrolmentByID(Integer id);
	
	NimeconfirmVideoObs saveNimeconfirmVideoObs(NimeconfirmVideoObs videoObs);
	
	List<NimeconfirmVideoObs> getNimeconfirmVideoObsByPatient(Patient patient);
	
	List<NimeconfirmVideoObs> getNimeconfirmVideoObsByPatientAndDate(Patient patient, Date date);
	
	List<NimeconfirmVideoObs> getNimeconfirmVideoObs();
	
	void voidNimeconfirmVideoObs(int id);
	
	NimeconfirmVideoObs getNimeconfirmVideoObsByID(Integer id);
}
