package org.openmrs.module.vdot.vdotDataExchange;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.DrugOrder;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.calculation.result.CalculationResult;

import org.openmrs.api.EncounterService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemr.calculation.EmrCalculationUtils;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.LastViralLoadResultCalculation;

import org.openmrs.module.kenyacore.RegimenMappingUtils;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.vdot.metadata.VdotMetadata;
import org.openmrs.module.vdot.util.Utils;
import org.openmrs.ui.framework.SimpleObject;

import java.util.List;
import java.util.Locale;

public class VdotDataExchange {
	
	private Log log = LogFactory.getLog(VdotDataExchange.class);
	
	Program vdotProgram = MetadataUtils.existing(Program.class, VdotMetadata._Program.VDOT_PROGRAM);
	
	ProgramWorkflowService programWorkflowService = Context.getProgramWorkflowService();
	
	public static final Locale LOCALE = Locale.ENGLISH;
	
	/**
	 * Returns a single object details for patients enrolled in VDOT program
	 * 
	 * @return
	 */
	public ObjectNode generatePayloadForVdot(Patient patient) {
		Context.addProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
		EncounterService encounterService = Context.getEncounterService();
		
		ObjectNode payload = Utils.getJsonNodeFactory().objectNode();
		
		String dob = patient.getBirthdate() != null ? Utils.getSimpleDateFormat("yyyy-MM-dd").format(patient.getBirthdate())
		        : "";
		
		// get last drug regimen encounter
		
		StringBuilder q = new StringBuilder();
		q.append("select max(e.encounter_id)");
		q.append("from encounter e inner join \n"
		        + "( select encounter_type_id, uuid, name from encounter_type where uuid ='7df67b83-1b84-4fe2-b1b7-794b4e9bfcc3'\n"
		        + ") et on et.encounter_type_id=e.encounter_type\n"
		        + "inner join orders o on o.encounter_id=e.encounter_id and o.voided=0 and o.order_action='NEW' and o.date_stopped is null and o.order_group_id is not null "
		
		);
		q.append("where e.patient_id = " + patient.getPatientId());
		
		List<List<Object>> queryData = Context.getAdministrationService().executeSQL(q.toString(), true);
		Integer encounterId = (Integer) queryData.get(0).get(0);
		Encounter lastDrugOrderEncounter = encounterService.getEncounter(encounterId);
		String frequency = "";
		
		if (lastDrugOrderEncounter != null) {
			for (Order order : lastDrugOrderEncounter.getOrders()) {
				if (order != null) {
					
					DrugOrder drugOrder = (DrugOrder) order;
					if (drugOrder.getFrequency() != null && drugOrder.getFrequency().getConcept() != null) {
						frequency = drugOrder.getFrequency().getConcept().getShortNameInLocale(LOCALE) != null ? drugOrder
						        .getFrequency().getConcept().getShortNameInLocale(LOCALE).getName() : drugOrder
						        .getFrequency().getConcept().getName().getName();
					}
				}
			}
		}
		
		CalculationResult lastViralLoad = EmrCalculationUtils.evaluateForPatient(LastViralLoadResultCalculation.class, null,
		    patient);
		SimpleObject vl = null;
		if (!lastViralLoad.isEmpty()) {
			vl = (SimpleObject) lastViralLoad.getValue();
		}
		
		List<PatientProgram> programs = programWorkflowService.getPatientPrograms(patient, vdotProgram, null, null, null,
		    null, true);
		
		PatientIdentifier cccNumber = patient.getPatientIdentifier(Utils.getUniquePatientNumberIdentifierType());
		Encounter originalRegimenEncounter = RegimenMappingUtils.getFirstEncounterForProgram(patient, "ARV");
		Encounter currentRegimenEncounter = RegimenMappingUtils.getLastEncounterForProgram(patient, "ARV");
		SimpleObject regimenDetails = RegimenMappingUtils.buildRegimenChangeObject(currentRegimenEncounter.getObs(),
		    currentRegimenEncounter);
		String regimenName = (String) regimenDetails.get("regimenShortDisplay");
		String regimenLine = (String) regimenDetails.get("regimenLine");
		ObjectNode address = Utils.getPatientAddress(patient);
		String phoneNumber = Utils.getPatientPhoneNumber(patient);
		String nextOfKinPhoneNumber = Utils.getPatientNextOfKinPhoneNumber(patient);
		
		String nascopCode = "";
		if (StringUtils.isNotBlank(regimenName)) {
			nascopCode = RegimenMappingUtils.getDrugNascopCodeByDrugNameAndRegimenLine(regimenName, regimenLine);
		}
		
		//add to list only if enrolled into vdot program
		if (programs.size() > 0) {
			
			payload.put("facilityCode", Utils.getDefaultLocationMflCode(Utils.getDefaultLocation()));
			payload.put("cccNo", cccNumber != null ? cccNumber.getIdentifier() : "");
			payload.put("dob", dob);
			payload.put("regimen", regimenName.replace("/", "+"));
			// payload.put("drug_code", nascopCode != null ? nascopCode : "");
			payload.put("phoneNumber", phoneNumber != null || StringUtils.isNotBlank(phoneNumber) ? phoneNumber
			        : nextOfKinPhoneNumber != null ? nextOfKinPhoneNumber : "");
			payload.put("firstName", patient.getGivenName());
			payload.put("middleName", patient.getMiddleName());
			payload.put("surname", patient.getFamilyName());
			payload.put("currentVl",
			    vl != null && vl.get("lastVl") != null ? vl.get("lastVl").toString().replace("copies/ml", "") : "");
			payload.put("lastVlDate", vl != null && vl.get("lastVlDate") != null ? vl.get("lastVlDate").toString() : "");
			payload.put("on_selfcare", "0");
			payload.put("frequency", frequency);
			//payload.put("morningIntakeTime", ""); // not collected in the emr
			//payload.put("eveningIntakeTime", ""); // not collected in the emr
			payload.put("countyCode", address.get("COUNTY").textValue());
			payload.put("subcounty", address.get("SUB_COUNTY").textValue());
			payload.put("gender", patient.getGender());
			
		}
		Context.removeProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
		
		return payload;
	}
	
}
