/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.vdot.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.UserService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.vdot.api.INimeconfirmService;
import org.openmrs.module.vdot.api.db.hibernate.NimeconfirmDao;
import org.openmrs.module.vdot.api.NimeconfirmEnrolment;
import org.openmrs.module.vdot.api.NimeconfirmVideoObs;

import java.util.Date;
import java.util.List;

public class NimeconfirmServiceImpl extends BaseOpenmrsService implements INimeconfirmService {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private NimeconfirmDao nimeconfirmDao;
	
	UserService userService;
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setNimeconfirmDao(NimeconfirmDao nimeconfirmDao) {
		this.nimeconfirmDao = nimeconfirmDao;
	}
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	@Override
	public NimeconfirmEnrolment saveNimeconfirmEnrolment(NimeconfirmEnrolment enrolment) {
		return nimeconfirmDao.saveNimeconfirmEnrolment(enrolment);
	}
	
	@Override
	public NimeconfirmEnrolment getNimeconfirmEnrolmentByPatient(Patient patient) {
		return nimeconfirmDao.getNimeconfirmEnrolmentByPatient(patient);
	}
	
	@Override
	public List<NimeconfirmEnrolment> getNimeconfirmEnrolment() {
		return nimeconfirmDao.getNimeconfirmEnrolment();
	}
	
	@Override
	public void voidNimeconfirmEnrolment(int id) {
		nimeconfirmDao.voidNimeconfirmEnrolment(id);
	}
	
	@Override
	public NimeconfirmEnrolment getNimeconfirmEnrolmentByID(Integer id) {
		return nimeconfirmDao.getNimeconfirmEnrolmentByID(id);
	}
	
	@Override
	public NimeconfirmVideoObs saveNimeconfirmVideoObs(NimeconfirmVideoObs videoObs) {
		return nimeconfirmDao.saveNimeconfirmVideoObs(videoObs);
	}
	
	@Override
	public List<NimeconfirmVideoObs> getNimeconfirmVideoObsByPatient(Patient patient) {
		return nimeconfirmDao.getNimeconfirmVideoObsByPatient(patient);
	}
	
	@Override
	public List<NimeconfirmVideoObs> getNimeconfirmVideoObsByPatientAndDate(Patient patient, Date date) {
		return nimeconfirmDao.getNimeconfirmVideoObsByPatientAndDate(patient, date);
	}
	
	@Override
	public List<NimeconfirmVideoObs> getNimeconfirmVideoObs() {
		return nimeconfirmDao.getNimeconfirmVideoObs();
	}
	
	@Override
	public void voidNimeconfirmVideoObs(int id) {
		nimeconfirmDao.voidNimeconfirmVideoObs(id);
	}
	
	@Override
	public NimeconfirmVideoObs getNimeconfirmVideoObsByID(Integer id) {
		return nimeconfirmDao.getNimeconfirmVideoObsByID(id);
	}
}
