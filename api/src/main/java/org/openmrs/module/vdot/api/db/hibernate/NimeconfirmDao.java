/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.vdot.api.db.hibernate;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Patient;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.vdot.api.NimeconfirmEnrolment;
import org.openmrs.module.vdot.api.NimeconfirmVideoObs;
import org.openmrs.module.vdot.api.dao.INimeconfirmDao;

import java.util.Date;
import java.util.List;

public class NimeconfirmDao implements INimeconfirmDao {
	
	private SessionFactory sessionFactory;
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public NimeconfirmEnrolment saveNimeconfirmEnrolment(NimeconfirmEnrolment enrolment) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(enrolment);
		return enrolment;
	}
	
	@Override
	public NimeconfirmEnrolment getNimeconfirmEnrolmentByPatient(Patient patient) {
		Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(NimeconfirmEnrolment.class);
		criteria.add(Restrictions.eq("patient", patient));
		criteria.add(Restrictions.eq("voided", false));
		if (!CollectionUtils.isEmpty(criteria.list())) {
			return (NimeconfirmEnrolment) criteria.list().get(0);
		}
		return null;
	}
	
	@Override
	public List<NimeconfirmEnrolment> getNimeconfirmEnrolment() {
		Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(NimeconfirmEnrolment.class);
		criteria.add(Restrictions.eq("voided", false));
		//return result
		return criteria.list();
	}
	
	@Override
	public void voidNimeconfirmEnrolment(int id) {
		sessionFactory.getCurrentSession().saveOrUpdate(id);
	}
	
	@Override
	public NimeconfirmEnrolment getNimeconfirmEnrolmentByID(Integer id) {
		Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(NimeconfirmEnrolment.class);
		criteria.add(Restrictions.eq("id", id));
		criteria.add(Restrictions.eq("voided", false));
		if (!CollectionUtils.isEmpty(criteria.list())) {
			return (NimeconfirmEnrolment) criteria.list().get(0);
		}
		return null;
	}
	
	@Override
	public NimeconfirmVideoObs saveNimeconfirmVideoObs(NimeconfirmVideoObs videoObs) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(videoObs);
		return videoObs;
	}
	
	@Override
	public List<NimeconfirmVideoObs> getNimeconfirmVideoObsByPatient(Patient patient) {
		Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(NimeconfirmVideoObs.class);
		criteria.add(Restrictions.eq("patient", patient));
		criteria.add(Restrictions.eq("voided", false));
		if (!CollectionUtils.isEmpty(criteria.list())) {
			return criteria.list();
		}
		return null;
	}
	
	@Override
	public List<NimeconfirmVideoObs> getNimeconfirmVideoObsByPatientAndDate(Patient patient, Date date) {
		Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(NimeconfirmVideoObs.class);
		criteria.add(Restrictions.eq("patient", patient));
		criteria.add(Restrictions.eq("voided", false));
		criteria.add(Restrictions.eq("date", date.after(date)));
		if (!CollectionUtils.isEmpty(criteria.list())) {
			return criteria.list();
		}
		return null;
	}
	
	@Override
	public List<NimeconfirmVideoObs> getNimeconfirmVideoObs() {
		Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(NimeconfirmVideoObs.class);
		criteria.add(Restrictions.eq("voided", false));
		//return result
		return criteria.list();
	}
	
	@Override
	public void voidNimeconfirmVideoObs(int id) {
		sessionFactory.getCurrentSession().saveOrUpdate(id);
	}
	
	@Override
	public NimeconfirmVideoObs getNimeconfirmVideoObsByID(Integer id) {
		Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(NimeconfirmVideoObs.class);
		criteria.add(Restrictions.eq("id", id));
		criteria.add(Restrictions.eq("voided", false));
		if (!CollectionUtils.isEmpty(criteria.list())) {
			return (NimeconfirmVideoObs) criteria.list().get(0);
		}
		return null;
	}
}
