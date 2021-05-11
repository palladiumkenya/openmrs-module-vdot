/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.vdot.calculation;

import org.openmrs.Obs;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculation;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.module.kenyacore.calculation.AbstractPatientCalculation;
import org.openmrs.module.kenyacore.calculation.BooleanResult;
import org.openmrs.module.kenyacore.calculation.Filters;
import org.openmrs.module.kenyaemr.calculation.EmrCalculationUtils;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.LastViralLoadCalculation;
import org.openmrs.module.kenyaemr.calculation.library.hiv.art.LowDetectableViralLoadCalculation;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Vdot program eligibility calculation. Eligibility criteria: Viral load > 1000 copies/ml and age between 0 and 19 years
 */
public class EligibleForVdotProgramCalculation extends AbstractPatientCalculation implements PatientCalculation {
	
	@Override
	public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> parameterValues,
	        PatientCalculationContext context) {
		CalculationResultMap ret = new CalculationResultMap();
		Set<Integer> alive = Filters.alive(cohort, context);
		PersonService personService = Context.getPersonService();
		Integer age;
		CalculationResultMap numericViralLoadValues = calculate(new LastViralLoadCalculation(), cohort, context);
		CalculationResultMap ldlViralLoadValues = calculate(new LowDetectableViralLoadCalculation(), cohort, context);
		
		for (int ptId : cohort) {
			boolean eligible = false;
			
			age = personService.getPerson(ptId).getAge();
			Obs numericVLObs = EmrCalculationUtils.obsResultForPatient(numericViralLoadValues, ptId);
			Obs ldlVLObs = EmrCalculationUtils.obsResultForPatient(ldlViralLoadValues, ptId);
			
			if (alive.contains(ptId) && age >= 0 && age <= 19) {
				if (numericVLObs != null && ldlVLObs == null) {
					if (numericVLObs.getValueNumeric() > 1000) {
						eligible = true;
					}
				}
				if (numericVLObs != null && ldlVLObs != null) {
					//find the latest of the 2
					if (numericVLObs.getObsDatetime().after(ldlVLObs.getObsDatetime())) {
						if (numericVLObs.getValueNumeric() > 1000) {
							eligible = true;
						}
					}
				}
			}
			ret.put(ptId, new BooleanResult(eligible, this));
		}
		
		return ret;
	}
}
