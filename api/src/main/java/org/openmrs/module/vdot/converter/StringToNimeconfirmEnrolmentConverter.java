package org.openmrs.module.vdot.converter;
import org.openmrs.module.vdot.api.NimeconfirmEnrolment;
import org.openmrs.api.context.Context;
import org.openmrs.module.vdot.api.NimeconfirmService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class IntegerToNimeconfirmEnrolmentConverter implements Converter<Integer, NimeconfirmEnrolment> {
	
	/**
	 * @see Converter#convert(Object)
	 */
	@Override
	public NimeconfirmEnrolment convert(Integer id) {
		NimeconfirmService service = Context.getService(NimeconfirmService.class);
		if (id == null) {
			return null;
		} else {
			return service.getNimeconfirmEnrolmentByID(id);
		}
		
	}
}
