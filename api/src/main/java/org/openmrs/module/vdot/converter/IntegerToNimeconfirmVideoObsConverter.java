package org.openmrs.module.vdot.converter;

import org.openmrs.api.context.Context;
import org.openmrs.module.vdot.api.NimeconfirmService;
import org.openmrs.module.vdot.api.NimeconfirmVideoObs;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Created by Palladium Dev on 13 May, 2021
 */
    @Component
    public class IntegerToNimeconfirmVideoObsConverter implements Converter<Integer, NimeconfirmVideoObs> {

        /**
         * @see Converter#convert(Object)
         */
        @Override
        public NimeconfirmVideoObs convert(Integer id) {
            NimeconfirmService service = Context.getService(NimeconfirmService.class);
            if (id == null) {
                return null;
            } else {
                return service.getNimeconfirmVideoObsByID(id);
            }

        }
    }
