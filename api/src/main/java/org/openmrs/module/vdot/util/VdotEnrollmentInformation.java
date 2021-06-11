package org.openmrs.module.vdot.util;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyacore.RegimenMappingUtils;
import org.openmrs.module.kenyaemr.util.EmrUtils;
import org.openmrs.module.vdot.metadata.VdotMetadata;
import org.openmrs.ui.framework.SimpleObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VdotEnrollmentInformation {
	
	ConceptService conceptService = Context.getConceptService();
	
	public SimpleObject getEnrollmenAndBaselineInformation(Patient patient) {
		
		SimpleObject enrollmentData = null;
		SimpleObject baselineData = null;
		
		Encounter lastEnrollmentEncounter = EmrUtils.lastEncounter(patient, Context.getEncounterService()
		        .getEncounterTypeByUuid(VdotMetadata._EncounterType.VDOT_CLIENT_ENROLLMENT), Context.getFormService()
		        .getFormByUuid(VdotMetadata._Form.VDOT_ENROLLMENT));
		
		Encounter lastBaselineEncounter = EmrUtils.lastEncounter(patient, Context.getEncounterService()
		        .getEncounterTypeByUuid(VdotMetadata._EncounterType.VDOT_BASELINE_ENCOUNTER), Context.getFormService()
		        .getFormByUuid(VdotMetadata._Form.VDOT_BASELINE));
		if (lastEnrollmentEncounter != null) {
			enrollmentData = getVdotEnrollmentFormData(lastEnrollmentEncounter.getObs());
		}
		
		if (lastBaselineEncounter != null) {
			baselineData = getVdotBaselineQuestionnaireInformation(lastBaselineEncounter.getObs());
		}
		
		Encounter currentRegimenEncounter = RegimenMappingUtils.getLastEncounterForProgram(patient, "ARV");
		SimpleObject regimenDetails = RegimenMappingUtils.buildRegimenChangeObject(currentRegimenEncounter.getObs(),
		    currentRegimenEncounter);
		String regimenName = (String) regimenDetails.get("regimenShortDisplay");
		
		return SimpleObject.create("enrollmentData", enrollmentData, "baselineData", baselineData, "regimenName",
		    regimenName);
	}
	
	SimpleObject getVdotEnrollmentFormData(Set<Obs> obsList) {
		
		Integer enrollmentReasonConcept = 1887;
		Integer enrollmentReasonOtherConcept = 160632;
		Integer consentConcept = 161641;
		
		String consent = "";
		String enrollmentReason = "";
		String enrollmentReasonOther = "";
		
		for (Obs obs : obsList) {
			
			if (obs.getConcept().getConceptId().equals(enrollmentReasonConcept)) {
				enrollmentReason = enrollmentReasonConverter(obs.getValueCoded());
			} else if (obs.getConcept().getConceptId().equals(enrollmentReasonOtherConcept)) {
				enrollmentReasonOther = obs.getValueText();
			} else if (obs.getConcept().getConceptId().equals(consentConcept)) {
				consent = obs.getValueCoded().getConceptId().equals(1065) ? "Yes" : "No";
			}
		}
		return SimpleObject.create("enrollmentReason", enrollmentReason, "enrollmentReasonOther", enrollmentReasonOther,
		    "consent", consent);
		
	}
	
	SimpleObject getVdotBaselineQuestionnaireInformation(Set<Obs> obsList) {
		
		Integer livingWithConcept = 159892;
		Integer primaryIncomeSourceConcept = 159740;
		Integer hivPositivehouseholdMembersConcept = 5587;
		Integer awareOfPatientStatusConcept = 159424;
		Integer primaryCaregiverConcept = 160640;
		Integer householdIncomeConcept = 164992;
		Integer noOfHouseholdMembersConcept = 160632; // using freetext general to store range
		Integer hasLatrineConcept = 160258; //1065,1066
		Integer drinkingWaterSourceConcept = 1511;
		Integer mealsInAdayConcept = 162523;
		Integer inSchoolConcept = 159928;//yes,no
		Integer schoolLevelConcept = 1712;
		Integer schoolTypeConcept = 159928;
		Integer distanceFromSchoolConcept = 162725;
		Integer meansOfTransportConcept = 1375;
		
		String livingWith = "";
		String primarySourceOfIncome = "";
		String hivPositiveHouseholdMembers = "";
		String membersAwareOfPatientStatus = "";
		String primaryCaregiver = "";
		String householdIncome = "";
		String noOfHouseholdMembers = "";
		String hasLatrineOrToilet = "";
		String sourceOfDrinkingWater = "";
		int noOfMealsPerDay = 0;
		String inSchool = "";
		String schoolLevel = "";
		String schoolType = "";
		String meansOfTransport = "";
		String distanceToSchool = "";
		
		for (Obs obs : obsList) {
			
			if (obs.getConcept().getConceptId().equals(livingWithConcept)) {
				livingWith = relationshipConverter(obs.getValueCoded());
			} else if (obs.getConcept().getConceptId().equals(primaryIncomeSourceConcept)) {
				primarySourceOfIncome = sourceOfIncomeConverter(obs.getValueCoded());
			} else if (obs.getConcept().getConceptId().equals(hivPositivehouseholdMembersConcept)) {
				hivPositiveHouseholdMembers = hivPositiveHouseholdMembers + relationshipConverter(obs.getValueCoded())
				        + ", ";
			} else if (obs.getConcept().getConceptId().equals(awareOfPatientStatusConcept)) {
				membersAwareOfPatientStatus = membersAwareOfPatientStatus + relationshipConverter(obs.getValueCoded())
				        + ", ";
			} else if (obs.getConcept().getConceptId().equals(primaryCaregiverConcept)) {
				primaryCaregiver = relationshipConverter(obs.getValueCoded());
			} else if (obs.getConcept().getConceptId().equals(householdIncomeConcept)) {
				householdIncome = obs.getValueText();
			} else if (obs.getConcept().getConceptId().equals(noOfHouseholdMembersConcept)) {
				noOfHouseholdMembers = obs.getValueText();
			} else if (obs.getConcept().getConceptId().equals(hasLatrineConcept)) {
				hasLatrineOrToilet = obs.getValueCoded().getConceptId().equals(1065) ? "Yes" : "No";
			} else if (obs.getConcept().getConceptId().equals(drinkingWaterSourceConcept)) {
				sourceOfDrinkingWater = waterSourceConverter(obs.getValueCoded());
			} else if (obs.getConcept().getConceptId().equals(mealsInAdayConcept)) {
				noOfMealsPerDay = obs.getValueNumeric().intValue();
			} else if (obs.getConcept().getConceptId().equals(inSchoolConcept)) {
				inSchool = obs.getValueCoded().getConceptId().equals(1065) ? "Yes" : "No";
			} else if (obs.getConcept().getConceptId().equals(schoolLevelConcept)) {
				schoolLevel = schoolLevelConverter(obs.getValueCoded());
			} else if (obs.getConcept().getConceptId().equals(schoolTypeConcept)) {
				schoolType = schoolTypeConverter(obs.getValueCoded());
			} else if (obs.getConcept().getConceptId().equals(meansOfTransportConcept)) {
				meansOfTransport = meansOfTransportConverter(obs.getValueCoded());
			} else if (obs.getConcept().getConceptId().equals(distanceFromSchoolConcept)) {
				distanceToSchool = obs.getValueNumeric() != null ? obs.getValueNumeric().toString() : "";
			}
		}
		
		return SimpleObject.create("livingWith", livingWith, "primarySourceOfIncome", primarySourceOfIncome,
		    "hivPositiveHouseholdMembers", hivPositiveHouseholdMembers, "membersAwareOfPatientStatus",
		    membersAwareOfPatientStatus, "primaryCaregiver", primaryCaregiver, "householdIncome", householdIncome,
		    "noOfHouseholdMembers", noOfHouseholdMembers, "hasLatrineOrToilet", hasLatrineOrToilet, "sourceOfDrinkingWater",
		    sourceOfDrinkingWater, "noOfMealsPerDay", noOfMealsPerDay, "inSchool", inSchool, "schoolLevel", schoolLevel,
		    "schoolType", schoolType, "meansOfTransport", meansOfTransport, "distanceToSchool", distanceToSchool);
		
	}
	
	String relationshipConverter(Concept key) {
		Map<Concept, String> relationshipList = new HashMap<Concept, String>();
		relationshipList.put(conceptService.getConcept(1527), "Parent");
		relationshipList.put(conceptService.getConcept(973), "Grand parent");
		relationshipList.put(conceptService.getConcept(159894), "Foster parent");
		relationshipList.put(conceptService.getConcept(5620), "Relative");
		relationshipList.put(conceptService.getConcept(972), "Sibling");
		relationshipList.put(conceptService.getConcept(160639), "Guardian");
		relationshipList.put(conceptService.getConcept(1107), "None");
		
		return relationshipList.get(key);
	}
	
	String sourceOfIncomeConverter(Concept key) {
		Map<Concept, String> relationshipList = new HashMap<Concept, String>();
		relationshipList.put(conceptService.getConcept(159673), "Business");
		relationshipList.put(conceptService.getConcept(1540), "Formal employment");
		relationshipList.put(conceptService.getConcept(159613), "Casual labour");
		relationshipList.put(conceptService.getConcept(1538), "Farming");
		
		return relationshipList.get(key);
	}
	
	String waterSourceConverter(Concept key) {
		Map<Concept, String> relationshipList = new HashMap<Concept, String>();
		relationshipList.put(conceptService.getConcept(1697), "Piped water");
		relationshipList.put(conceptService.getConcept(1508), "Rain water");
		relationshipList.put(conceptService.getConcept(1506), "River");
		relationshipList.put(conceptService.getConcept(1510), "Borehole");
		relationshipList.put(conceptService.getConcept(1509), "Well");
		
		return relationshipList.get(key);
	}
	
	String schoolLevelConverter(Concept key) {
		Map<Concept, String> relationshipList = new HashMap<Concept, String>();
		relationshipList.put(conceptService.getConcept(160289), "Pre-primary");
		relationshipList.put(conceptService.getConcept(1713), "Primary");
		relationshipList.put(conceptService.getConcept(1714), "Secondary");
		relationshipList.put(conceptService.getConcept(159785), "Tertiary");
		
		return relationshipList.get(key);
	}
	
	String schoolTypeConverter(Concept key) {
		Map<Concept, String> relationshipList = new HashMap<Concept, String>();
		relationshipList.put(conceptService.getConcept(164210), "Day school");
		relationshipList.put(conceptService.getConcept(164211), "Boarding school");
		
		return relationshipList.get(key);
	}
	
	String meansOfTransportConverter(Concept key) {
		Map<Concept, String> relationshipList = new HashMap<Concept, String>();
		relationshipList.put(conceptService.getConcept(159310), "Walk");
		relationshipList.put(conceptService.getConcept(159744), "Motorbike");
		relationshipList.put(conceptService.getConcept(159748), "Bicycle");
		relationshipList.put(conceptService.getConcept(1787), "Public transport");
		relationshipList.put(conceptService.getConcept(162710), "School bus");
		
		return relationshipList.get(key);
		
	}
	
	String enrollmentReasonConverter(Concept key) {
		Map<Concept, String> relationshipList = new HashMap<Concept, String>();
		relationshipList.put(conceptService.getConcept(164075), "Sub optimal adherence");
		relationshipList.put(conceptService.getConcept(165240), "Newly initiating ART");
		relationshipList.put(conceptService.getConcept(5619), "Unstable Caregiver");
		relationshipList.put(conceptService.getConcept(989), "Age 0-4 years");
		relationshipList.put(conceptService.getConcept(5622), "Other");
		
		return relationshipList.get(key);
		
	}
	
}
