package org.openmrs.module.vdot.api.model;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Patient;
import org.openmrs.User;

import java.util.Date;

/**
 * Created by Palladium dev on 12 May, 2021
 */
public class NimeconfirmEnrolment extends BaseOpenmrsData {
	
	private Integer id;
	
	private Patient patient;
	
	private String payLoad;
	
	private String status;
	
	private Date date;
	
	public NimeconfirmEnrolment() {
	}
	
	public NimeconfirmEnrolment(Patient patient, String payLoad, String status, Date date) {
		this.patient = patient;
		this.payLoad = payLoad;
		this.status = status;
		this.date = date;
	}
	
	public Patient getPatient() {
		return patient;
	}
	
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	public String getPayLoad() {
		return payLoad;
	}
	
	public void setPayLoad(String payLoad) {
		this.payLoad = payLoad;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	@Override
	public Integer getId() {
		return null;
	}
	
	@Override
	public void setId(Integer integer) {
		
	}
	
	@Override
	public User getCreator() {
		return super.getCreator();
	}
	
	@Override
	public void setCreator(User creator) {
		super.setCreator(creator);
	}
	
	@Override
	public Date getDateCreated() {
		return super.getDateCreated();
	}
	
	@Override
	public void setDateCreated(Date dateCreated) {
		super.setDateCreated(dateCreated);
	}
	
	@Override
	public User getChangedBy() {
		return super.getChangedBy();
	}
	
	@Override
	public void setChangedBy(User changedBy) {
		super.setChangedBy(changedBy);
	}
	
	@Override
	public Date getDateChanged() {
		return super.getDateChanged();
	}
	
	@Override
	public void setDateChanged(Date dateChanged) {
		super.setDateChanged(dateChanged);
	}
	
	@Override
	public Boolean isVoided() {
		return super.isVoided();
	}
	
	@Override
	public Boolean getVoided() {
		return super.getVoided();
	}
	
	@Override
	public void setVoided(Boolean voided) {
		super.setVoided(voided);
	}
	
	@Override
	public Date getDateVoided() {
		return super.getDateVoided();
	}
	
	@Override
	public void setDateVoided(Date dateVoided) {
		super.setDateVoided(dateVoided);
	}
	
	@Override
	public User getVoidedBy() {
		return super.getVoidedBy();
	}
	
	@Override
	public void setVoidedBy(User voidedBy) {
		super.setVoidedBy(voidedBy);
	}
	
	@Override
	public String getVoidReason() {
		return super.getVoidReason();
	}
	
	@Override
	public void setVoidReason(String voidReason) {
		super.setVoidReason(voidReason);
	}
}
