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

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.vdot.api.INimeconfirmService;
import org.openmrs.module.vdot.api.NimeconfirmEnrolment;
import org.openmrs.module.vdot.metadata.VdotMetadata;
import org.openmrs.module.vdot.vdotDataExchange.VdotDataExchange;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.*;

/**
 * Prepare payload for VDOT application
 */
public class VdotQueueDataProcessorTask extends AbstractTask {
	
	INimeconfirmService nimeconfirmService = Context.getService(INimeconfirmService.class);
	
	private Log log = LogFactory.getLog(VdotQueueDataProcessorTask.class);
	
	/**
	 * @see AbstractTask#execute()
	 */
	public void execute() {
		Context.openSession();
		try {
			
			GlobalProperty lastEncounterEntryFromGP = Context.getAdministrationService().getGlobalPropertyObject(
			    VdotMetadata.VDOT_LAST_ENROLLMENT_ENCOUNTER);// this will store the last encounter id prior to task execution
			
			String lastEncounterSql = "select max(encounter_id) last_id from encounter where voided=0;";
			List<List<Object>> lastEncounterId = Context.getAdministrationService().executeSQL(lastEncounterSql, true);
			
			Integer lastIdFromEncounterTable = (Integer) lastEncounterId.get(0).get(0);
			lastIdFromEncounterTable = lastIdFromEncounterTable != null ? lastIdFromEncounterTable : 0;
			
			String lastEncounterValueFromGPStr = lastEncounterEntryFromGP != null
			        && lastEncounterEntryFromGP.getValue() != null ? lastEncounterEntryFromGP.getValue().toString() : "";
			Integer lastEncounterIDFromGP = StringUtils.isNotBlank(lastEncounterValueFromGPStr) ? Integer
			        .parseInt(lastEncounterValueFromGPStr) : 0;
			
			List<Encounter> pendingEnrollments = fetchPendingEnrollments(lastEncounterIDFromGP, lastIdFromEncounterTable);
			Map<Patient, List<Encounter>> groupedEncounters = groupEncountersByPatient(pendingEnrollments);
			for (Map.Entry entry : groupedEncounters.entrySet()) {
				processPendingEnrollments((Patient) entry.getKey());
			}
			
			lastEncounterEntryFromGP.setPropertyValue(lastIdFromEncounterTable.toString());
			Context.getAdministrationService().saveGlobalProperty(lastEncounterEntryFromGP);
			
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Vdot POST task could not be executed!", e);
		}
	}
	
	private List<Encounter> fetchPendingEnrollments(Integer lastEncounterIdFromGP, Integer lastEncounterFromEncounterTable) {
		
		StringBuilder q = new StringBuilder();
		q.append("select e.encounter_id ");
		q.append("from encounter e inner join "
		        + "( "
		        + " select encounter_type_id, uuid, name from encounter_type where uuid ='cf805d0a-a470-4194-b375-7e04f56d4dee' "
		        + " ) et on et.encounter_type_id=e.encounter_type ");
		
		if (lastEncounterIdFromGP != null && lastEncounterIdFromGP > 0) {
			q.append("where e.encounter_id > " + lastEncounterIdFromGP + " ");
		} else {
			q.append("where e.encounter_id <= " + lastEncounterFromEncounterTable + " ");
		}
		
		q.append(" and e.voided = 0 group by e.encounter_id ");
		List<Encounter> encounters = new ArrayList<Encounter>();
		EncounterService encounterService = Context.getEncounterService();
		List<List<Object>> queryData = Context.getAdministrationService().executeSQL(q.toString(), true);
		for (List<Object> row : queryData) {
			Integer encounterId = (Integer) row.get(0);
			Encounter e = encounterService.getEncounter(encounterId);
			encounters.add(e);
		}
		System.out.println("No. of vdot enrollment encounters found: " + encounters.size());
		return encounters;
		
	}
	
	/**
	 * Processes a list of encounters
	 * 
	 * @return a map containing encounters for each patient
	 */
	private Map<Patient, List<Encounter>> groupEncountersByPatient(List<Encounter> encounters) {
		Map<Patient, List<Encounter>> encounterMap = new HashMap<Patient, List<Encounter>>();
		
		for (Encounter encounter : encounters) {
			if (encounterMap.keySet().contains(encounter.getPatient())) {
				encounterMap.get(encounter.getPatient()).add(encounter);
			} else {
				List<Encounter> eList = new ArrayList<Encounter>();
				eList.add(encounter);
				encounterMap.put(encounter.getPatient(), eList);
			}
		}
		return encounterMap;
	}
	
	private void processPendingEnrollments(Patient patient) {
		VdotDataExchange e = new VdotDataExchange();
		ObjectNode payload = e.generatePayloadForVdot(patient);
		ObjectNode reason = e.extractEnrollmentReasonToVdotProgram(patient);
		String enrolReason = reason.get("reason").textValue();
		String enrolReasonOther = reason.get("reasonOther").textValue();
		
		Date date = new Date();
		NimeconfirmEnrolment outMsg = new NimeconfirmEnrolment(patient, payload.toString(), "Pending", enrolReason,
		        enrolReasonOther, date, "New registration");
		nimeconfirmService.saveNimeconfirmEnrolment(outMsg);
		
	}
	
}
