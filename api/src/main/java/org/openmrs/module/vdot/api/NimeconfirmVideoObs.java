package org.openmrs.module.vdot.api.model;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Patient;
import org.openmrs.User;

import java.util.Date;

/**
 * Created by Palladium dev on 12 May, 2021
 */
public class NimeconfirmVideoObs extends BaseOpenmrsData {
	
	private Integer id;
	
	private Patient patient;
	
	private Date date;
	
	private Double score;
	
	private String timeStamp;
	
	private String patientStatus;
	
	public NimeconfirmVideoObs() {
	}
	
	public NimeconfirmVideoObs(Patient patient, Date date, Double score, String timeStamp, String patientStatus) {
		this.patient = patient;
		this.date = date;
		this.score = score;
		this.timeStamp = timeStamp;
		this.patientStatus = patientStatus;
	}
	
	public Patient getPatient() {
		return patient;
	}
	
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public Double getScore() {
		return score;
	}
	
	public void setScore(Double score) {
		this.score = score;
	}
	
	public String getTimeStamp() {
		return timeStamp;
	}
	
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	public String getPatientStatus() {
		return patientStatus;
	}
	
	public void setPatientStatus(String patientStatus) {
		this.patientStatus = patientStatus;
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
	
	@Override
	public Integer getId() {
		return null;
	}
	
	@Override
	public void setId(Integer integer) {
		
	}
}
