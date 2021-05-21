package org.openmrs.module.vdot.vdotDataExchange;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
//import org.codehaus.jackson.map.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BaseJsonNode;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.module.vdot.api.INimeconfirmService;
import org.openmrs.module.vdot.api.NimeconfirmVideoObs;
import org.openmrs.module.vdot.api.impl.NimeconfirmServiceImpl;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.openmrs.module.vdot.util.Utils.getJsonNodeFactory;

public class VdotDataExchange {
	
	private Log log = LogFactory.getLog(VdotDataExchange.class);
	
	private List<PatientIdentifierType> allPatientIdentifierTypes;

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
		
		ObjectNode payload = getJsonNodeFactory().objectNode();
		
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
			payload.put("countyCode", getCountyCodes(address.get("COUNTY").textValue().toLowerCase()));
			payload.put("subcounty", address.get("SUB_COUNTY").textValue());
			payload.put("gender", patient.getGender());
			
		}
		Context.removeProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
		
		return payload;
	}
	
	/**
	 * processes incoming message from vdot server *
	 * 
	 * @return
	 */
	public String processIncomingVdotData(JSONObject jsonObject) {
		
		// Consume read data
		INimeconfirmService iNimeconfirmService = Context.getService(INimeconfirmService.class);
<<<<<<< HEAD
		NimeconfirmVideoObs videoObs = new NimeconfirmVideoObs();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		ConceptService cs = Context.getConceptService();

		EncounterService encounterService = Context.getEncounterService();

		Form vdotDiscontinuationForm = Context.getFormService().getFormByUuid(VdotMetadata._Form.VDOT_COMPLETION);
		Form baselineQuestionnaireForm = Context.getFormService().getFormByUuid(VdotMetadata._Form.VDOT_BASELINE);

		EncounterType baselineEncounter = encounterService
				.getEncounterTypeByUuid(VdotMetadata._EncounterType.VDOT_BASELINE_ENCOUNTER);

		EncounterType discEncounter = encounterService
				.getEncounterTypeByUuid(VdotMetadata._EncounterType.VDOT_CLIENT_DISCONTINUATION);
		//TODO: Need to handle duplications
		// Get last time a fetch was conducted and compare with incoming timestamp
		// to avoid fetching same messages
		Date fetchDate = null;
		Date timestampDate = null;
		String message = "";
		GlobalProperty globalPropertyObject = Context.getAdministrationService().getGlobalPropertyObject(
		    "vdotVideoMessages.lastFetchDateAndTime");

		try {
			String ft = globalPropertyObject.getValue().toString();
			fetchDate = formatter.parse(ft);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// Get timestamp to compare with last run timestamp
		try {

			String timeStamp = jsonObject.get("timestamp").toString();
			timestampDate = formatter.parse(timeStamp);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		if (fetchDate.after(timestampDate)) {

			videoObs.setDate((Date) jsonObject.get("timestamp"));

			JSONArray pdataJArray = (JSONArray) jsonObject.get("patientsData");

			//Iterating over PatientsData

			Iterator arrayItr = pdataJArray.iterator();
			// TODO: 20/05/2021 -cccNo and mfl code not in the model. Clinical report not in the EMR Discontinue form - How to handle?. DiscontinueData and Baseline Questionaire data - Map message to concepts.
			while (arrayItr.hasNext()) {
				Encounter encounter = new Encounter();
				String cccNo = (String) jsonObject.get("cccNo");
				String mflCode = (String) jsonObject.get("mflCode");
				videoObs.setScore((Double) jsonObject.get("adherenceScore"));
				videoObs.setDate((Date) jsonObject.get("adherenceTime"));
				videoObs.setPatientStatus((String) jsonObject.get("patientStatus"));
				videoObs.setScore((Double) jsonObject.get("adherenceScore"));

				Map discontinueData = ((Map) jsonObject.get("discontinueData"));

				// iterating discontinueData Map

				Iterator<Map.Entry> mapItr = discontinueData.entrySet().iterator();
				while (mapItr.hasNext()) {
					Obs o = new Obs();
					Map.Entry pair = mapItr.next();
					o.setConcept(cs.getConcept(conceptNameToIdMapper(jsonObject.get(pair.getKey()).toString())));
					o.setDateCreated(new Date());
					o.setCreator(Context.getAuthenticatedUser());
					o.setObsDatetime(new Date());
					o.setPerson(null);// TODO: 20/05/2021 get the patient
					if ((conceptNameToIdMapper(jsonObject.get(pair.getKey()).toString()).equals(164384))) {
						o.setValueDatetime((Date) pair.getValue());
					} else if ((conceptNameToIdMapper(jsonObject.get(pair.getKey()).toString()).equals(161555))
							|| (conceptNameToIdMapper(jsonObject.get(pair.getKey()).toString()).equals(1599))) {
						o.setValueCoded(cs.getConcept(conceptNameToIdMapper(jsonObject.get(pair.getValue()).toString())));
					}

					// TODO: 20/05/2021 Extract K,V and Create a discontinue encounter
					encounter.setPatient(null);// TODO: 20/05/2021 get patient
					encounter.setEncounterType(discEncounter);
					encounter.setEncounterDatetime(new Date());// TODO: 20/05/2021 get proper date
					encounter.setDateCreated(new Date());
					encounter.setForm(vdotDiscontinuationForm);
					encounter.addObs(o);
				}
				// TODO: 20/05/2021 create a record per for each day
				while (arrayItr.hasNext()) {
					videoObs.setTimeStamp((String) jsonObject.get("videosTimestamps"));
				}
				//
				Map baselineQuestionnaire = ((Map) jsonObject.get("baselineQuestionnaire"));
				Iterator<Map.Entry> baseItr = baselineQuestionnaire.entrySet().iterator();
				while (baseItr.hasNext()) {
					Map.Entry pair = mapItr.next();
					Obs o = new Obs();
					// TODO: 20/05/2021 Extract K,V and Create a baselineQuestionnaire encounter
					encounter.setPatient(null);// TODO: 20/05/2021 get patient
					encounter.setEncounterType(discEncounter);
					encounter.setEncounterDatetime(new Date());// TODO: 20/05/2021 get proper date
					encounter.setDateCreated(new Date());
					encounter.setForm(baselineQuestionnaireForm);
					encounter.addObs(o);
				}
			}
			org.codehaus.jackson.node.ArrayNode patientDataNode = (org.codehaus.jackson.node.ArrayNode) jsonObject
			        .get("patientsData");

			List<Object> patientsData = new ArrayList<Object>();
			patientsData.add(patientDataNode);

			if (patientsData.size() > 0) {

				String cccNo = (String) jsonObject.get("cccNo");
				// Check to see a patient with similar upn number exists
				List<Patient> patients = Context.getPatientService().getPatients(null, cccNo, allPatientIdentifierTypes,
				    true);
				if (patients.size() > 0) {
					Patient patient = patients.get(0);
					for (int i = 0; i < patientsData.size(); ++i) {
						videoObs.setPatient(patient);
						videoObs.setId(patient.getId());
						videoObs.setScore(videoObs.getScore());
						videoObs.setPatientStatus(videoObs.getPatientStatus());
						videoObs.setDate(videoObs.getDate());
					}
				}
			}
			//videoObs.setTimeStamp(timestampNode.toString());

			iNimeconfirmService.saveNimeconfirmVideoObs(videoObs);
			message = "Incoming vdot data processed successfully";
		} else {
			message = "Vdot message already processed";
		}
		return message;
		
	}
	// TODO: 21/05/2021 Might need refactoring. There might be mapping methods in openmrs and this mapper may not be required
	public static Integer conceptNameToIdMapper(String name) {
		HashMap<String, Integer> conceptMap = new HashMap();
		conceptMap.put("dateDiscontinued", 164384);
		conceptMap.put("discontinuationReason", 161555);
		conceptMap.put("Transferred Out", 159492);
		conceptMap.put("Died", 160034);
		conceptMap.put("Lost to Follow", 5240);
		conceptMap.put("Cannot afford Treatment", 819);
		conceptMap.put("Other", 5622);
		conceptMap.put("Unknown", 1067);
		conceptMap.put("causeOfDeath", 1599);
		conceptMap.put("HIV disease resulting in TB", 163324);
		conceptMap.put("HIV disease resulting in cancer", 116030);
		conceptMap.put("HIV disease resulting in other infectious and parasitic diseases", 160159);
		conceptMap.put("Other HIV disease resulting in other diseases or conditions leading to death", 160158);
		conceptMap.put("Other natural causes not directly related to HIV", 133478);
		conceptMap.put("Non-communicable diseases such as Diabetes and hypertension", 145439);
		conceptMap.put("Non-natural causes", 123812);
		conceptMap.put("Unknown cause", 142917);
		if (conceptMap.containsKey(name)) {
			return conceptMap.get(name);
		} else {
			return null;
		}

=======
		
		ObjectNode payload = getJsonNodeFactory().objectNode();
		
		ConceptService cs = Context.getConceptService();
		
		EncounterService encounterService = Context.getEncounterService();
		
		Form vdotDiscontinuationForm = Context.getFormService().getFormByUuid(VdotMetadata._Form.VDOT_COMPLETION);
		Form baselineQuestionnaireForm = Context.getFormService().getFormByUuid(VdotMetadata._Form.VDOT_BASELINE);

		EncounterType baselineEncounter = encounterService
		        .getEncounterTypeByUuid(VdotMetadata._EncounterType.VDOT_BASELINE_ENCOUNTER);
		
		EncounterType discEncounter = encounterService
		        .getEncounterTypeByUuid(VdotMetadata._EncounterType.VDOT_CLIENT_DISCONTINUATION);
		
		//TODO: Need to handle duplications
		
		JSONArray pdataJArray = (JSONArray) jsonObject.get("patientsData");
		
		//Iterating over PatientsData
		
		Iterator arrayItr = pdataJArray.iterator();
		
		// TODO: 20/05/2021 -cccNo and mfl code not in the model. Clinical report not in the EMR Discontinue form - How to handle?. DiscontinueData and Baseline Questionaire data - Map message to concepts.
		while (arrayItr.hasNext()) {
			NimeconfirmVideoObs videoObs = new NimeconfirmVideoObs();
			Encounter encounter = new Encounter();
			String cccNo = (String) jsonObject.get("cccNo");
			String mflCode = (String) jsonObject.get("mflCode");
			videoObs.setDate((Date) jsonObject.get("timestamp"));
			videoObs.setScore((Double) jsonObject.get("adherenceScore"));
			//videoObs.setDate((Date) jsonObject.get("adherenceTime"));
			videoObs.setPatientStatus((String) jsonObject.get("patientStatus"));
			videoObs.setScore((Double) jsonObject.get("adherenceScore"));
			
			Map discontinueData = ((Map) jsonObject.get("discontinueData"));
			/*	discData.add(jsonObject.get("dateDiscontinued"));
				discData.add(jsonObject.get("discontinuationReason"));
				discData.add(jsonObject.get("causeOfDeath"));
			Obs o = new Obs();o.setConcept();*/
			// iterating discontinueData Map
			
			Iterator<Map.Entry> mapItr = discontinueData.entrySet().iterator();
			/*	Iterator<Set>setItr = discData.iterator();*/
			while (mapItr.hasNext()) {
				//while (setItr.hasNext()) {
				Obs o = new Obs();
				Map.Entry pair = mapItr.next();
				o.setConcept(cs.getConcept(conceptNameToIdMapper(jsonObject.get(pair.getKey()).toString())));
				o.setDateCreated(new Date());
				o.setCreator(Context.getAuthenticatedUser());
				o.setObsDatetime(new Date());
				o.setPerson(null);// TODO: 20/05/2021 get the patient
				if ((conceptNameToIdMapper(jsonObject.get(pair.getKey()).toString()).equals(164384))) {
					o.setValueDatetime((Date) pair.getValue());
				} else if ((conceptNameToIdMapper(jsonObject.get(pair.getKey()).toString()).equals(161555))
				        || (conceptNameToIdMapper(jsonObject.get(pair.getKey()).toString()).equals(1599))) {
					o.setValueCoded(cs.getConcept(conceptNameToIdMapper(jsonObject.get(pair.getValue()).toString())));
				}
				
				// TODO: 20/05/2021 Extract K,V and Create a discontinue encounter
				encounter.setPatient(null);// TODO: 20/05/2021 get patient
				encounter.setEncounterType(discEncounter);
				encounter.setEncounterDatetime(new Date());// TODO: 20/05/2021 get proper date
				encounter.setDateCreated(new Date());
				encounter.setForm(vdotDiscontinuationForm);
				encounter.addObs(o);
			}
			
			// TODO: 20/05/2021 create a record for each day per patient. Concatenate with comma for multiple timestamps in a day for a patient
			while (arrayItr.hasNext()) {
				videoObs.setTimeStamp((String) jsonObject.get("videosTimestamps"));
			}
			//
			Map baselineQuestionnaire = ((Map) jsonObject.get("baselineQuestionnaire"));
			Iterator<Map.Entry> baseItr = baselineQuestionnaire.entrySet().iterator();
			while (baseItr.hasNext()) {
				Map.Entry pair = mapItr.next();
				Obs o = new Obs();
				// TODO: 20/05/2021 Extract K,V and Create a baselineQuestionnaire encounter

				// TODO: 20/05/2021 Extract K,V and Create a discontinue encounter
				encounter.setPatient(null);// TODO: 20/05/2021 get patient
				encounter.setEncounterType(discEncounter);
				encounter.setEncounterDatetime(new Date());// TODO: 20/05/2021 get proper date
				encounter.setDateCreated(new Date());
				encounter.setForm(baselineQuestionnaireForm);
				encounter.addObs(o);
			}
		}
		org.codehaus.jackson.node.ArrayNode patientDataNode = (org.codehaus.jackson.node.ArrayNode) jsonObject
		        .get("patientsData");
		
		List<Object> patientsData = new ArrayList<Object>();
		patientsData.add(patientDataNode);
		
		/*	Patient patient = videoObs.getPatient();
			if (patientsData.size() > 0) {
				for (int i = 0; i < patientsData.size(); ++i) {
					videoObs.setPatient(patient);
					videoObs.setId(patient.getId());
					videoObs.setScore(videoObs.getScore());
					videoObs.setPatientStatus(videoObs.getPatientStatus());
					videoObs.setDate(videoObs.getDate());
				}
			}*/
		//videoObs.setTimeStamp(timestampNode.toString());
		
		//iNimeconfirmService.saveNimeconfirmVideoObs(videoObs);
		return "Incoming vdot data processed successfully";
		
>>>>>>> bdbb4e5... Parsing incoming JSON message. Fixed nimeconformVideoObs id attribute's getter and setter
	}

	// TODO: 21/05/2021 Might need refactoring. There might be mapping methods in openmrs and this mapper may not be required
	public static Integer conceptNameToIdMapper(String name) {
		HashMap<String, Integer> conceptMap = new HashMap();
		conceptMap.put("dateDiscontinued", 164384);
		conceptMap.put("discontinuationReason", 161555);
		conceptMap.put("Transferred Out", 159492);
		conceptMap.put("Died", 160034);
		conceptMap.put("Lost to Follow", 5240);
		conceptMap.put("Cannot afford Treatment", 819);
		conceptMap.put("Other", 5622);
		conceptMap.put("Unknown", 1067);
		conceptMap.put("causeOfDeath", 1599);
		conceptMap.put("HIV disease resulting in TB", 163324);
		conceptMap.put("HIV disease resulting in cancer", 116030);
		conceptMap.put("HIV disease resulting in other infectious and parasitic diseases", 160159);
		conceptMap.put("Other HIV disease resulting in other diseases or conditions leading to death", 160158);
		conceptMap.put("Other natural causes not directly related to HIV", 133478);
		conceptMap.put("Non-communicable diseases such as Diabetes and hypertension", 145439);
		conceptMap.put("Non-natural causes", 123812);
		conceptMap.put("Unknown cause", 142917);
		if (conceptMap.containsKey(name)) {
			return conceptMap.get(name);
		} else {
			return null;
		}
		
	}
	
	private String getCountyCodes(String name) {
		
		String json = "{\"counties\":[\n" + "  {\n" + "    \"County\": \"Mombasa\",\n" + "    \"Code\": 1\n" + "  },\n"
		        + "  {\n" + "    \"County\": \"Kwale\",\n" + "    \"Code\": 2\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Kilifi\",\n" + "    \"Code\": 3\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Tana River\",\n" + "    \"Code\": 4\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Lamu\",\n" + "    \"Code\": 5\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Taita-Taveta\",\n" + "    \"Code\": 6\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Garissa\",\n" + "    \"Code\": 7\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Wajir\",\n" + "    \"Code\": 8\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Mandera\",\n" + "    \"Code\": 9\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Marsabit\",\n" + "    \"Code\": 10\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Isiolo\",\n" + "    \"Code\": 11\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Meru\",\n" + "    \"Code\": 12\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Tharaka-Nithi\",\n" + "    \"Code\": 13\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Embu\",\n" + "    \"Code\": 14\n" + "  },\n" + "  {\n" + "    \"County\": \"Kitui\",\n"
		        + "    \"Code\": 15\n" + "  },\n" + "  {\n" + "    \"County\": \"Machakos\",\n" + "    \"Code\": 16\n"
		        + "  },\n" + "  {\n" + "    \"County\": \"Makueni\",\n" + "    \"Code\": 17\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Nyandarua\",\n" + "    \"Code\": 18\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Nyeri\",\n" + "    \"Code\": 19\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Kirinyaga\",\n" + "    \"Code\": 20\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Murang'a\",\n" + "    \"Code\": 21\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Kiambu\",\n" + "    \"Code\": 22\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Turkana\",\n" + "    \"Code\": 23\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"West Pokot\",\n" + "    \"Code\": 24\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Samburu\",\n" + "    \"Code\": 25\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Trans-Nzoia\",\n" + "    \"Code\": 26\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Uasin Gishu\",\n" + "    \"Code\": 27\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Elgeyo-Marakwet\",\n" + "    \"Code\": 28\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Nandi\",\n" + "    \"Code\": 29\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Baringo\",\n" + "    \"Code\": 30\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Laikipia\",\n" + "    \"Code\": 31\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Nakuru\",\n" + "    \"Code\": 32\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Narok\",\n" + "    \"Code\": 33\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Kajiado\",\n" + "    \"Code\": 34\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Kericho\",\n" + "    \"Code\": 35\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Bomet\",\n" + "    \"Code\": 36\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Kakamega\",\n" + "    \"Code\": 37\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Vihiga\",\n" + "    \"Code\": 38\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Bungoma\",\n" + "    \"Code\": 39\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Busia\",\n" + "    \"Code\": 40\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Siaya\",\n" + "    \"Code\": 41\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Kisumu\",\n" + "    \"Code\": 42\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Homa Bay\",\n" + "    \"Code\": 43\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Migori\",\n" + "    \"Code\": 44\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Kisii\",\n" + "    \"Code\": 45\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Nyamira\",\n" + "    \"Code\": 46\n" + "  },\n" + "  {\n"
		        + "    \"County\": \"Nairobi\",\n" + "    \"Code\": 47\n" + "  }\n" + "]\n" + "}";
		
		ObjectMapper mapper = new ObjectMapper(); //using jackson
		JsonNode jsonNode = null;
		String countyName = "";
		String countyCode = "";
		try {
			jsonNode = mapper.readTree(json);
			ArrayNode arrayNode = (ArrayNode) jsonNode.get("counties");
			for (int i = 0; i < arrayNode.size(); i++) {
				countyName = arrayNode.get(i).get("County").toString().toLowerCase().replace("\"", "");
				if (StringUtils.isNotBlank(countyName) && countyName.equals(name)) {
					countyCode = arrayNode.get(i).get("Code").toString();
					break;
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return countyCode;
	}
	
}
