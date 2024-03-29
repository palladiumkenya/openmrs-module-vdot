package org.openmrs.module.vdot.converter;

import org.openmrs.api.context.Context;
import org.openmrs.module.vdot.api.NimeconfirmEnrolment;
import org.openmrs.module.vdot.api.INimeconfirmService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Created by Palladium Dev on 13 May, 2021
 */
@Component
public class IntegerToNimeconfirmEnrolmentConverter implements Converter<Integer, NimeconfirmEnrolment> {
	
	/**
	 * @see Converter#convert(Object)
	 */
	@Override
	public NimeconfirmEnrolment convert(Integer id) {
		INimeconfirmService service = Context.getService(INimeconfirmService.class);
		if (id == null) {
			return null;
		} else {
			return service.getNimeconfirmEnrolmentByID(id);
		}
		
	}
}
