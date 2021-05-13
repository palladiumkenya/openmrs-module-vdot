package org.openmrs.module.vdot.converter;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.vdot.api.NimeconfirmVideoObs;
import org.openmrs.module.vdot.api.NimeconfirmService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToNimeconfirmVideoObsConverter implements Converter<String, NimeconfirmVideoObs> {
	
	/**
	 * @see Converter#convert(Object)
	 */
	@Override
	public NimeconfirmVideoObs convert(String source) {
		if (StringUtils.isEmpty(source)) {
			return null;
		}
		
		return Context.getService(NimeconfirmService.class).getNimeconfirmVideoObsByID(Integer.valueOf(source));
	}
}
