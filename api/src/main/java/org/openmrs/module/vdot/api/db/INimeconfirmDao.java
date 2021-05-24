package org.openmrs.module.vdot.api.dao;

import org.openmrs.Patient;
import org.openmrs.module.vdot.api.NimeconfirmEnrolment;
import org.openmrs.module.vdot.api.NimeconfirmVideoObs;

import java.util.Date;
import java.util.List;

/**
 * Created by Palladium dev on 12 May, 2021
 */
public interface INimeconfirmDao {
	
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
