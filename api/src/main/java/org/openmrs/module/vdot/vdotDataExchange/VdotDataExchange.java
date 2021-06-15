package org.openmrs.module.vdot.vdotDataExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.module.kenyaemr.util.EmrUtils;
import org.openmrs.module.vdot.api.INimeconfirmService;
import org.openmrs.module.vdot.api.NimeconfirmVideoObs;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.openmrs.module.vdot.util.Utils.getJsonNodeFactory;

public class VdotDataExchange {
	
	private Log log = LogFactory.getLog(VdotDataExchange.class);
	
	private List<PatientIdentifierType> allPatientIdentifierTypes;
	
	Program vdotProgram = MetadataUtils.existing(Program.class, VdotMetadata._Program.VDOT_PROGRAM);
	
	ProgramWorkflowService programWorkflowService = Context.getProgramWorkflowService();
	
	private static ConceptService cs = Context.getConceptService();
	
	private static EncounterService encounterService = Context.getEncounterService();
	
	Form vdotDiscontinuationForm = Context.getFormService().getFormByUuid(VdotMetadata._Form.VDOT_COMPLETION);
	
	private static Form baselineQuestionnaireForm = Context.getFormService().getFormByUuid(VdotMetadata._Form.VDOT_BASELINE);
	
	private static Location location = Utils.getDefaultLocation();
	
	private static EncounterType baselineEncounter = encounterService
	        .getEncounterTypeByUuid(VdotMetadata._EncounterType.VDOT_BASELINE_ENCOUNTER);
	
	EncounterType discEncounter = encounterService
	        .getEncounterTypeByUuid(VdotMetadata._EncounterType.VDOT_CLIENT_DISCONTINUATION);
	
	public static final Locale LOCALE = Locale.ENGLISH;
	
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
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
		Encounter lastDrugOrderEncounter = null;
		
		if (encounterId != null) {
			lastDrugOrderEncounter = encounterService.getEncounter(encounterId);
		}
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
		ObjectNode reason = extractEnrollmentReasonToVdotProgram(patient);
		
		String nascopCode = "";
		if (StringUtils.isNotBlank(regimenName)) {
			nascopCode = RegimenMappingUtils.getDrugNascopCodeByDrugNameAndRegimenLine(regimenName, regimenLine);
		}
		//add to list only if enrolled into vdot program
		if (programs.size() > 0) {
			
			payload.put("facilityCode", Utils.getDefaultLocationMflCode(Utils.getDefaultLocation()));
			payload.put("cccNo", cccNumber != null ? cccNumber.getIdentifier() : "");
			payload.put("dob", dob);
			payload.put("regimen", StringUtils.isNotBlank(nascopCode) ? nascopCode : regimenName != null ? regimenName : "");
			// payload.put("drug_code", nascopCode != null ? nascopCode : "");
			payload.put("phoneNumber", phoneNumber != null || StringUtils.isNotBlank(phoneNumber) ? phoneNumber
			        : nextOfKinPhoneNumber != null ? nextOfKinPhoneNumber : "");
			payload.put("firstName", patient.getGivenName());
			payload.put("middleName", patient.getMiddleName() != null ? patient.getMiddleName() : "");
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
			payload.put("reasonForEnrollment", reason.get("reason").textValue());
			payload.put("reasonForEnrollmentOther", reason.get("reasonOther").textValue());
			
		}
		Context.removeProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
		
		return payload;
	}
	
	/**
	 * Returns a county's code
	 * 
	 * @param name
	 * @return
	 */
	private String getCountyCodes(String name) {
		
		ArrayNode countyListNode = Utils.getCountyCodes();
		String countyName = "";
		String countyCode = "";
		for (int i = 0; i < countyListNode.size(); i++) {
			countyName = countyListNode.get(i).get("County").toString().toLowerCase().replace("\"", "");
			if (StringUtils.isNotBlank(countyName) && countyName.equals(name)) {
				countyCode = countyListNode.get(i).get("Code").toString();
				break;
			}
		}
		return countyCode;
	}
	
	public ObjectNode extractEnrollmentReasonToVdotProgram(Patient patient) {
		Encounter lastEnrollmentEncounter = EmrUtils.lastEncounter(patient, Context.getEncounterService()
		        .getEncounterTypeByUuid(VdotMetadata._EncounterType.VDOT_CLIENT_ENROLLMENT), Context.getFormService()
		        .getFormByUuid(VdotMetadata._Form.VDOT_ENROLLMENT));
		Set<Obs> enrollmentObs = lastEnrollmentEncounter.getObs();
		StringBuilder sbReason = new StringBuilder();
		String reasonOther = "";
		ObjectNode enrollmentReasonNode = getJsonNodeFactory().objectNode();
		
		for (Obs obs : enrollmentObs) {
			
			if (obs.getConcept().getConceptId() == 1887) {
				if (obs.getValueCoded().getConceptId() == 165240) {
					sbReason.append("Newly initiating ART");
					sbReason.append(",");
					
				} else if (obs.getValueCoded().getConceptId() == 164075) {
					sbReason.append("Sub optimal adherence");
					sbReason.append(",");
					
				} else if (obs.getValueCoded().getConceptId() == 5619) {
					sbReason.append("Unstable Caregiver");
					sbReason.append(",");
					
				} else if (obs.getValueCoded().getConceptId() == 989) {
					sbReason.append("Age 0-4 years");
					sbReason.append(",");
					
				}
				
			}
			
			if (obs.getConcept().getConceptId() == 160632) {
				reasonOther = obs.getValueText();
			}
			
		}
		enrollmentReasonNode.put("reason", sbReason.length() > 0 ? sbReason.substring(0, sbReason.length() - 1) : "");
		enrollmentReasonNode.put("reasonOther", reasonOther);
		
		return enrollmentReasonNode;
	}
	
	/**
	 * Processes video observations fetched from nimeConfirm server and persist the data to
	 * kenyaemr_vdot_nimeconfirm_video_obs model
	 */
	public static String processVideoObs(String payload) {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = null;
		String message = "";
		INimeconfirmService iNimeconfirmService = Context.getService(INimeconfirmService.class);
		
		try {
			jsonNode = mapper.readTree(payload);
			if (jsonNode != null) {
				ArrayNode patientArrayNode = (ArrayNode) jsonNode.get("patientsData");
				if (patientArrayNode.size() > 0) {
					for (int i = 0; i < patientArrayNode.size(); i++) {
						String ccc = patientArrayNode.get(i).get("cccNo").asText();
						ObjectNode questionnairePayload = null;
						if (patientArrayNode.get(i).get("baselineQuestionnaire") != null) {
							questionnairePayload = (ObjectNode) patientArrayNode.get(i).get("baselineQuestionnaire");
						}
						
						Patient patient = Context.getPatientService().identifierInUse(ccc,
						    Context.getPatientService().getPatientIdentifierTypeByUuid(Utils.UNIQUE_PATIENT_NUMBER), null);
						Map<String, List<String>> groupedVideoTimeStamps = null;
						try {
							SimpleDateFormat vformatter = new SimpleDateFormat("yyyy-MM-dd");
							String patientStatus = patientArrayNode.get(i).get("patientStatus") != null ? patientArrayNode
							        .get(i).get("patientStatus").asText() : "";
							
							groupedVideoTimeStamps = Utils.groupVideoTimeStampsByDay(patientArrayNode.get(i)
							        .get("videosTimestamps").toString());
							if (groupedVideoTimeStamps != null) {
								for (Map.Entry entry : groupedVideoTimeStamps.entrySet()) {
									List<String> vTimestamps = (List<String>) entry.getValue();
									NimeconfirmVideoObs videoObs = new NimeconfirmVideoObs();
									
									if (patient != null && vTimestamps.size() > 0 && StringUtils.isNotBlank(patientStatus)) {
										videoObs.setTimeStamp(StringUtils.join(vTimestamps, ","));
										videoObs.setPatient(patient);
										if (patientArrayNode.get(i).get("adherenceScore") != null) {
											videoObs.setScore(patientArrayNode.get(i).get("adherenceScore").asDouble());
											
										}
										videoObs.setPatientStatus(patientStatus);
										videoObs.setDate(vformatter.parse(entry.getKey().toString()));
										iNimeconfirmService.saveNimeconfirmVideoObs(videoObs);
										message = "Incoming vdot data processed successfully";
										
									}
								}
								
							}
							if (patient != null && questionnairePayload != null) {
								processBaselineQuestionnaire(questionnairePayload, patient);
								
							}
							
							if (patient != null && patientStatus.equalsIgnoreCase("Discontinued")) {
								ObjectNode discontinueData = null;
								if (patientArrayNode.get(i).get("discontinueData") != null) {
									discontinueData = (ObjectNode) patientArrayNode.get(i).get("discontinueData");
									discontinuePatientFromVdotProgram(patient, discontinueData);
								}
								
							}
							
						}
						catch (ParseException e) {
							e.printStackTrace();
						}
						catch (JsonProcessingException e) {
							e.printStackTrace();
						}
						
					}
				}
			}
		}
		catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return message;
		
	}
	
	/**
	 * Process VDOT program discontinuation data
	 */
	private static void discontinuePatientFromVdotProgram(Patient patient, ObjectNode discontinueData) {
		
		Date discontinuationDate = null;
		try {
			discontinuationDate = dateFormat.parse(discontinueData.get("dateDiscontinued").asText());
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
		
		String discontinuationReason = discontinueData.get("discontinuationReason").asText();
		
		PatientProgram lastEnrollment = getActiveProgram(patient, VdotMetadata._Program.VDOT_PROGRAM);
		if (lastEnrollment != null) {
			lastEnrollment.setDateCompleted(discontinuationDate);
			Context.getProgramWorkflowService().savePatientProgram(lastEnrollment);
		}
		
		Encounter enc = new Encounter();
		enc.setEncounterType(Context.getEncounterService().getEncounterTypeByUuid(
		    VdotMetadata._EncounterType.VDOT_CLIENT_DISCONTINUATION));
		enc.setEncounterDatetime(discontinuationDate);
		enc.setPatient(patient);
		enc.addProvider(Context.getEncounterService().getEncounterRole(1), Context.getProviderService().getProvider(1));
		enc.setForm(Context.getFormService().getFormByUuid(VdotMetadata._Form.VDOT_COMPLETION));
		
		// set discontinuation reason
		
		ConceptService conceptService = Context.getConceptService();
		Obs o = new Obs();
		o.setConcept(conceptService.getConcept(161555));
		o.setDateCreated(new Date());
		o.setCreator(Context.getUserService().getUser(1));
		o.setLocation(enc.getLocation());
		o.setObsDatetime(discontinuationDate);
		o.setPerson(patient);
		
		if (org.apache.commons.lang3.StringUtils.isNotBlank(discontinuationReason)) {
			if (discontinuationReason.equalsIgnoreCase("Transferred out")) {
				o.setValueCoded(conceptService.getConcept(159492));// transferred out
			} else if (discontinuationReason.equalsIgnoreCase("Died")) {
				o.setValueCoded(conceptService.getConcept(160034)); // dead
			} else if (discontinuationReason.equalsIgnoreCase("Lost to follow up")) {
				o.setValueCoded(conceptService.getConcept(5240)); // Lost to followup
			} else if (discontinuationReason.equalsIgnoreCase("cannot afford treatment")) {
				o.setValueCoded(conceptService.getConcept(819)); // cannot afford treatment
			} else if (discontinuationReason.equalsIgnoreCase("Other")) {
				o.setValueCoded(conceptService.getConcept(5622)); // other
			} else if (discontinuationReason.equalsIgnoreCase("Unknown")) {
				o.setValueCoded(conceptService.getConcept(1067)); // unknown
			} else if (discontinuationReason.equalsIgnoreCase("Repeat VL outcome(Suppressed)")) {
				o.setValueCoded(conceptService.getConcept(165244)); // suppressed VL
			}
		}
		
		enc.addObs(o);
		Context.getEncounterService().saveEncounter(enc);
	}
	
	/**
	 * Checks if a contact is enrolled in a program
	 * 
	 * @param patient
	 * @return
	 */
	public static PatientProgram getActiveProgram(Patient patient, String programUUID) {
		ProgramWorkflowService service = Context.getProgramWorkflowService();
		List<PatientProgram> programs = service.getPatientPrograms(patient, service.getProgramByUuid(programUUID), null,
		    null, null, null, true);
		return programs.size() > 0 ? programs.get(programs.size() - 1) : null;
	}
	
	/**
	 * Process VDOT baseline questionnaire data
	 */
	
	private static void processBaselineQuestionnaire(ObjectNode baselineQuestionnairePayload, Patient patient) {
		
		Encounter encounter = new Encounter();
		Iterator<Map.Entry<String, JsonNode>> mapItr = baselineQuestionnairePayload.fields();
		List<Obs> individualObsList = new ArrayList();
		while (mapItr.hasNext()) {
			Map.Entry<String, JsonNode> pair = mapItr.next();
			if (StringUtils.isNotBlank(pair.getValue().asText())) {
				Integer conceptId = Utils.conceptNameToIdMapper(pair.getKey());
				
				Obs o = new Obs();
				if (conceptId != null) {
					
					if (conceptId == 159892 || conceptId == 159424 || conceptId == 5587) {
						ArrayNode arrNode = handleMultiSelectOptions(pair.getValue().asText());
						
						for (JsonNode i : arrNode) {
							Obs obs = new Obs();
							obs.setConcept(cs.getConcept(conceptId));
							Integer ansConceptId = Utils.ansConceptNameToIdMapper(i.asText());
							if (ansConceptId != null) {
								obs.setValueCoded(cs.getConcept(ansConceptId));
								
							}
							obs.setDateCreated(new Date());
							obs.setCreator(Context.getAuthenticatedUser());
							obs.setObsDatetime(new Date());
							obs.setPerson(patient);
							individualObsList.add(obs);
						}
						
					} else {
						
						o.setConcept(cs.getConcept(conceptId));
						o.setDateCreated(new Date());
						o.setCreator(Context.getAuthenticatedUser());
						o.setObsDatetime(new Date());
						o.setPerson(patient);
						if (conceptId == 164992 || conceptId == 160632 || conceptId == 162725) {
							o.setValueText(pair.getValue().asText());
							
						} else if (conceptId == 162523) {
							o.setValueNumeric(Double.parseDouble(pair.getValue().asText()));
							
						} else {
							Integer ansConceptId = Utils.ansConceptNameToIdMapper(pair.getValue().asText());
							if (ansConceptId != null) {
								o.setValueCoded(cs.getConcept(ansConceptId));
								
							}
							
						}
						individualObsList.add(o);
						
					}
				}
				
				encounter.setPatient(patient);
				encounter.setLocation(location);
				encounter.addProvider(Context.getEncounterService().getEncounterRole(1), Context.getProviderService()
				        .getProvider(1));
				encounter.setEncounterType(baselineEncounter);
				encounter.setEncounterDatetime(new Date());
				encounter.setDateCreated(new Date());
				encounter.setForm(baselineQuestionnaireForm);
				
				for (Obs individualObs : individualObsList) {
					encounter.addObs(individualObs);
				}
			}
			
		}
		
		Context.getEncounterService().saveEncounter(encounter);
		
	}
	
	private static ArrayNode handleMultiSelectOptions(String multiSelectOptions) {
		ArrayNode arrNode = getJsonNodeFactory().arrayNode();
		if (StringUtils.isNotBlank(multiSelectOptions)) {
			for (String s : multiSelectOptions.split(",")) {
				arrNode.add(s);
			}
		}
		
		return arrNode;
		
	}
	
}
