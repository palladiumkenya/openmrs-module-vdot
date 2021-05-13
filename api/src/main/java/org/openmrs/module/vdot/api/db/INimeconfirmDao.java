package org.openmrs.module.vdot.api.dao;

import org.openmrs.Patient;
import org.openmrs.module.vdot.api.model.NimeconfirmEnrolment;
import org.openmrs.module.vdot.api.model.NimeconfirmVideoObs;

import java.util.List;

/**
 * Created by Palladium dev on 12 May, 2021
 */
public interface INimeconfirmDao {
	
	NimeconfirmEnrolment saveNimeconfirmEnrolment(NimeconfirmEnrolment enrolment);
	
	NimeconfirmEnrolment getNimeconfirmEnrolmentByPatient(Patient patient);
	
	List<NimeconfirmEnrolment> getNimeconfirmEnrolment();
	
	void voidNimeconfirmEnrolment(int id);
	
	NimeconfirmEnrolment getNimeconfirmEnrolmentByID(Integer id);
	
	NimeconfirmVideoObs saveNimeconfirmVideoObs(NimeconfirmVideoObs videoObs);
	
	NimeconfirmVideoObs getNimeconfirmVideoObsByPatient(Patient patient);
	
	List<NimeconfirmVideoObs> getNimeconfirmVideoObs();
	
	void voidNimeconfirmVideoObs(int id);
	
	NimeconfirmVideoObs getNimeconfirmVideoObsByID(Integer id);
}
