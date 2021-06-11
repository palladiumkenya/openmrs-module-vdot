package org.openmrs.module.vdot.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonAddress;
import org.openmrs.Location;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.GlobalProperty;
import org.openmrs.LocationAttribute;
import org.openmrs.api.context.Context;
import org.openmrs.util.PrivilegeConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.Collections;

public class Utils {
	
	public static String UNIQUE_PATIENT_NUMBER = "05ee9cf4-7242-4a17-b4d4-00f707265c8a";
	
	public static final String TELEPHONE_CONTACT = "b2c38640-2603-4629-aebd-3b54f33f1e3a";
	
	public static final String NEXT_OF_KIN_CONTACT = "342a1d39-c541-4b29-8818-930916f4c2dc";
	
	/**
	 * Gets the PatientIdentifierType for a patient UPN
	 * 
	 * @return
	 */
	public static PatientIdentifierType getUniquePatientNumberIdentifierType() {
		return Context.getPatientService().getPatientIdentifierTypeByUuid(UNIQUE_PATIENT_NUMBER);
		
	}
	
	/**
	 * Returns a person's phone number attribute
	 * 
	 * @param patient
	 * @return
	 */
	public static String getPatientPhoneNumber(Patient patient) {
		PersonAttributeType phoneNumberAttrType = Context.getPersonService().getPersonAttributeTypeByUuid(TELEPHONE_CONTACT);
		return patient.getAttribute(phoneNumberAttrType) != null ? patient.getAttribute(phoneNumberAttrType).getValue() : "";
	}
	
	/**
	 * Returns a person's next of kin phone number attribute
	 * 
	 * @param patient
	 * @return
	 */
	public static String getPatientNextOfKinPhoneNumber(Patient patient) {
		PersonAttributeType phoneNumberAttrType = Context.getPersonService().getPersonAttributeTypeByUuid(
		    NEXT_OF_KIN_CONTACT);
		return patient.getAttribute(phoneNumberAttrType) != null ? patient.getAttribute(phoneNumberAttrType).getValue() : "";
	}
	
	/**
	 * Returns a patient's address
	 * 
	 * @param patient
	 * @return
	 */
	public static ObjectNode getPatientAddress(Patient patient) {
		Set<PersonAddress> addresses = patient.getAddresses();
		//patient address
		ObjectNode patientAddressNode = getJsonNodeFactory().objectNode();
		String county = "";
		String sub_county = "";
		String ward = "";
		
		for (PersonAddress address : addresses) {
			
			if (address.getCountyDistrict() != null) {
				county = address.getCountyDistrict() != null ? address.getCountyDistrict() : "";
			}
			
			if (address.getStateProvince() != null) {
				sub_county = address.getStateProvince() != null ? address.getStateProvince() : "";
			}
			
			if (address.getAddress4() != null) {
				ward = address.getAddress4() != null ? address.getAddress4() : "";
			}
			
		}
		
		patientAddressNode.put("COUNTY", county);
		patientAddressNode.put("SUB_COUNTY", sub_county);
		patientAddressNode.put("WARD", ward);
		return patientAddressNode;
	}
	
	public static Encounter lastEncounter(Patient patient, EncounterType type) {
		List<Encounter> encounters = Context.getEncounterService().getEncounters(patient, null, null, null, null,
		    Collections.singleton(type), null, null, null, false);
		return encounters.size() > 0 ? encounters.get(encounters.size() - 1) : null;
	}
	
	/**
	 * gets default location from global property
	 * 
	 * @return
	 */
	public static Location getDefaultLocation() {
		try {
			Context.addProxyPrivilege(PrivilegeConstants.VIEW_LOCATIONS);
			Context.addProxyPrivilege(PrivilegeConstants.VIEW_GLOBAL_PROPERTIES);
			String GP_DEFAULT_LOCATION = "kenyaemr.defaultLocation";
			GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(GP_DEFAULT_LOCATION);
			return gp != null ? ((Location) gp.getValue()) : null;
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.VIEW_LOCATIONS);
			Context.removeProxyPrivilege(PrivilegeConstants.VIEW_GLOBAL_PROPERTIES);
		}
		
	}
	
	/**
	 * Borrowed from KenyaEMR Returns the MFL code for a location
	 * 
	 * @param location
	 * @return
	 */
	public static String getDefaultLocationMflCode(Location location) {
		String MASTER_FACILITY_CODE = "8a845a89-6aa5-4111-81d3-0af31c45c002";
		
		if (location == null) {
			location = getDefaultLocation();
		}
		try {
			Context.addProxyPrivilege(PrivilegeConstants.VIEW_LOCATIONS);
			Context.addProxyPrivilege(PrivilegeConstants.VIEW_GLOBAL_PROPERTIES);
			for (LocationAttribute attr : location.getAttributes()) {
				if (attr.getAttributeType().getUuid().equals(MASTER_FACILITY_CODE) && !attr.isVoided()) {
					return attr.getValueReference();
				}
			}
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.VIEW_LOCATIONS);
			Context.removeProxyPrivilege(PrivilegeConstants.VIEW_GLOBAL_PROPERTIES);
		}
		return null;
	}
	
	public static SimpleDateFormat getSimpleDateFormat(String pattern) {
		return new SimpleDateFormat(pattern);
	}
	
	/**
	 * Creates a node factory
	 * 
	 * @return
	 */
	public static JsonNodeFactory getJsonNodeFactory() {
		final JsonNodeFactory factory = JsonNodeFactory.instance;
		return factory;
	}
	
	/**
	 * Extracts the request body and return it as string
	 * 
	 * @param reader
	 * @return
	 */
	public static String fetchRequestBody(BufferedReader reader) {
		String requestBodyJsonStr = "";
		try {
			
			BufferedReader br = new BufferedReader(reader);
			String output = "";
			while ((output = reader.readLine()) != null) {
				requestBodyJsonStr += output;
			}
		}
		catch (IOException e) {
			
			System.out.println("IOException: " + e.getMessage());
			
		}
		return requestBodyJsonStr;
	}
	
	public static HashMap<String, List<String>> groupVideoTimeStampsByDay(String timeStamps) throws ParseException,
	        JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		
		List<String> sList = mapper.readValue(timeStamps, new TypeReference<List<String>>() {});
		
		HashMap<String, List<String>> groupedTimeStamps = new HashMap<String, List<String>>();
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-M-dd hh:mm:ss");
		for (String ts : sList) {
			cal.setTime(formatter.parse(ts));
			cal.set(cal.HOUR_OF_DAY, 0);
			cal.set(cal.MINUTE, 0);
			cal.set(cal.SECOND, 0);
			cal.set(cal.MILLISECOND, 0);
			
			if (!groupedTimeStamps.containsKey(formatter.format(cal.getTime()))) {
				groupedTimeStamps.put(formatter.format(cal.getTime()), new ArrayList<String>());
			}
			groupedTimeStamps.get(formatter.format(cal.getTime())).add(ts);
		}
		return groupedTimeStamps;
	}
	
	public static Integer conceptNameToIdMapper(String name) {
		Integer concept = null;
		HashMap<String, Integer> conceptMap = new HashMap();
		conceptMap.put("dateDiscontinued", 164384);
		conceptMap.put("discontinuationReason", 161555);
		conceptMap.put("causeOfDeath", 1599);
		conceptMap.put("primaryCaregiver", 160640);
		conceptMap.put("livesWith", 159892);
		conceptMap.put("isAwareOfChildHivStatus", 159424);
		conceptMap.put("othersWithHivAtHome", 5587);
		conceptMap.put("schoolLevel", 1712);
		//	conceptMap.put("schType", 159928);
		conceptMap.put("boards", 159928); // TODO Vdot team to check on the response they provide for this question and also name the key appropriately
		conceptMap.put("meansOfTransport", 1375);
		conceptMap.put("incomeSource", 159740);
		conceptMap.put("sourceOfWater", 1511);
		//numeric
		conceptMap.put("incomeAmount", 164992);
		conceptMap.put("totalAtHome", 160632);
		conceptMap.put("schoolDistance", 162725);
		conceptMap.put("noOfMeals", 162523);
		//value boolean
		conceptMap.put("goesToSchool", 5606);
		conceptMap.put("toiletAccess", 160258);
		if (conceptMap.containsKey(name)) {
			concept = conceptMap.get(name);
		}
		return concept;
	}
	
	public static Integer ansConceptNameToIdMapper(String name) {
		Integer conceptAnswer = null;
		HashMap<String, Integer> conceptMap = new HashMap();
		conceptMap.put("Transferred Out", 159492);
		conceptMap.put("Died", 160034);
		conceptMap.put("Lost to Follow", 5240);
		conceptMap.put("Cannot afford Treatment", 819);
		conceptMap.put("Other", 5622);
		conceptMap.put("Unknown", 1067);
		conceptMap.put("HIV disease resulting in TB", 163324);
		conceptMap.put("HIV disease resulting in cancer", 116030);
		conceptMap.put("HIV disease resulting in other infectious and parasitic diseases", 160159);
		conceptMap.put("Other HIV disease resulting in other diseases or conditions leading to death", 160158);
		conceptMap.put("Other natural causes not directly related to HIV", 133478);
		conceptMap.put("Non-communicable diseases such as Diabetes and hypertension", 145439);
		conceptMap.put("Non-natural causes", 123812);
		conceptMap.put("Unknown cause", 142917);
		conceptMap.put("Parent", 1527);
		conceptMap.put("Grand parents", 973);
		conceptMap.put("Foster parent", 159894);
		conceptMap.put("Relative", 5620);
		conceptMap.put("Sibling", 972);
		conceptMap.put("Guardian", 160639);
		conceptMap.put("none", 1107);
		conceptMap.put("Pre-primary", 160289);
		conceptMap.put("Primary", 1713);
		conceptMap.put("Secondary", 1714);
		conceptMap.put("Tertiary", 159785);
		conceptMap.put("boards", 164211);
		conceptMap.put("day", 164210);
		conceptMap.put("Walk to school", 159310);
		conceptMap.put("Motorbike", 159744);
		conceptMap.put("Bicycle", 159748);
		conceptMap.put("Public transport", 1787);
		conceptMap.put("School bus", 162710);
		conceptMap.put("Business", 159673);
		conceptMap.put("Formal employment", 1540);
		conceptMap.put("Casual labour", 159613);
		conceptMap.put("Farming", 1538);
		conceptMap.put("Piped water", 1697);
		conceptMap.put("Rain water", 1508);
		conceptMap.put("River", 1506);
		conceptMap.put("Borehole", 1510);
		conceptMap.put("Well", 1509);
		conceptMap.put("Yes", 1065);
		conceptMap.put("No", 1066);
		if (conceptMap.containsKey(name)) {
			conceptAnswer = conceptMap.get(name);
			
		}
		return conceptAnswer;
	}
	
	/**
	 * Returns an array of counties and their codes
	 * 
	 * @return
	 */
	public static ArrayNode getCountyCodes() {
		
		ArrayNode countyListNode = getJsonNodeFactory().arrayNode();
		List<String> countyNameList = Arrays.asList("Mombasa", "Kwale", "Kilifi", "Tana River", "Lamu", "Taita-Taveta",
		    "Garissa", "Wajir", "Mandera", "Marsabit", "Isiolo", "Meru", "Tharaka-Nithi", "Embu", "Kitui", "Machakos",
		    "Makueni", "Nyandarua", "Nyeri", "Kirinyaga", "Murang'a", "Kiambu", "Turkana", "West Pokot", "Samburu",
		    "Trans-Nzoia", "Uasin Gishu", "Elgeyo-Marakwet", "Nandi", "Baringo", "Laikipia", "Nakuru", "Narok", "Kajiado",
		    "Kericho", "Bomet", "Kakamega", "Vihiga", "Bungoma", "Busia", "Siaya", "Kisumu", "Homa Bay", "Migori", "Kisii",
		    "Nyamira", "Nairobi");
		
		//process the name list in the order that it appears. Distorting the order changes the mapped key
		for (int i = 0; i < countyNameList.size(); i++) {
			ObjectNode countyNode = getJsonNodeFactory().objectNode();
			countyNode.put("County", countyNameList.get(i)); // we are adding 1 to take care of zero-based index
			countyNode.put("Code", i + 1);
			countyListNode.add(countyNode);
		}
		
		return countyListNode;
	}
	
}
