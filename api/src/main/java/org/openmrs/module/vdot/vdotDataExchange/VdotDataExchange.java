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
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyacore.RegimenMappingUtils;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.vdot.metadata.VdotMetadata;
import org.openmrs.module.vdot.util.Utils;
import org.openmrs.ui.framework.SimpleObject;

import java.util.List;

public class VdotDataExchange {
	
	private Log log = LogFactory.getLog(VdotDataExchange.class);
	
	Program vdotProgram = MetadataUtils.existing(Program.class, VdotMetadata._Program.VDOT_PROGRAM);
	
	ProgramWorkflowService programWorkflowService = Context.getProgramWorkflowService();
	
	/**
	 * Returns a single object details for patients enrolled in VDOT program
	 * 
	 * @return
	 */
	public ObjectNode generatePayloadForVdot(Patient patient) {
		ObjectNode payload = Utils.getJsonNodeFactory().objectNode();
		
		String dob = patient.getBirthdate() != null ? Utils.getSimpleDateFormat("yyyy-MM-dd").format(patient.getBirthdate())
		        : "";
		
		String fullName = "";
		
		if (patient.getGivenName() != null) {
			fullName += patient.getGivenName();
		}
		
		if (patient.getMiddleName() != null) {
			fullName += " " + patient.getMiddleName();
		}
		
		if (patient.getFamilyName() != null) {
			fullName += " " + patient.getFamilyName();
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
		
		String nascopCode = "";
		if (StringUtils.isNotBlank(regimenName)) {
			nascopCode = RegimenMappingUtils.getDrugNascopCodeByDrugNameAndRegimenLine(regimenName, regimenLine);
		}
		
		//add to list only if enrolled into vdot program
		if (programs.size() > 0) {
			
			payload.put("facility_enrolled", Utils.getDefaultLocationMflCode(Utils.getDefaultLocation()));
			payload.put("ccc_number", cccNumber != null ? cccNumber.getIdentifier() : "");
			payload.put("dob", dob);
			payload.put("current_regimen", regimenName);
			payload.put("drug_code", nascopCode != null ? nascopCode : "");
			payload.put("phone_number", Utils.getPatientPhoneNumber(patient));
			payload.put("next_of_kin_phone_number", Utils.getPatientNextOfKinPhoneNumber(patient));
			payload.put("patient_name", fullName);
			payload.put("last_vl", "");
			payload.put("last_vl_date", "");
			payload.put("county", address.get("COUNTY").textValue());
			payload.put("sub_county", address.get("SUB_COUNTY").textValue());
			payload.put("sex", patient.getGender());
			payload.put("date_started_art", originalRegimenEncounter != null ? Utils.getSimpleDateFormat("yyyy-MM-dd")
			        .format(originalRegimenEncounter.getEncounterDatetime()) : "");
			payload.put(
			    "date_initiated_on_current_regimen",
			    currentRegimenEncounter != null ? Utils.getSimpleDateFormat("yyyy-MM-dd").format(
			        currentRegimenEncounter.getEncounterDatetime()) : "");
		}
		return payload;
	}
	
}
