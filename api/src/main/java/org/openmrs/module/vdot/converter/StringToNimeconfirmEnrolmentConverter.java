package org.openmrs.module.vdot.converter;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.vdot.api.NimeconfirmEnrolment;
import org.openmrs.module.vdot.api.INimeconfirmService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToNimeconfirmEnrolmentConverter implements Converter<String, NimeconfirmEnrolment> {
	
	/**
	 * @see Converter#convert(Object)
	 */
	@Override
	public NimeconfirmEnrolment convert(String source) {
		if (StringUtils.isEmpty(source)) {
			return null;
		}
		
		return Context.getService(INimeconfirmService.class).getNimeconfirmEnrolmentByID(Integer.valueOf(source));
	}
}
