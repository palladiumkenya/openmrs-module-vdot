package org.openmrs.module.vdot.util;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.text.SimpleDateFormat;
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
}
