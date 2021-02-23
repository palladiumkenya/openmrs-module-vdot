package org.openmrs.module.vdot.metadata;

import org.openmrs.api.UserService;
import org.openmrs.module.metadatadeploy.bundle.AbstractMetadataBundle;
import org.openmrs.module.metadatadeploy.bundle.Requires;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.*;

/**
 * Implementation of access control to the app.
 */
@Component
@Requires(org.openmrs.module.kenyaemr.metadata.SecurityMetadata.class)
public class VdotSecurityMetadata extends AbstractMetadataBundle {
	
	@Autowired
	@Qualifier("userService")
	private UserService userService;
	
	public static final class _Privilege {
		
		public static final String VDOT_MODULE_APP = "App: kenyaemrvdot.home";
		
	}
	
	public static final class _Role {
		
		public static final String VDOT_CLINICIAN = "Vdot clinician";
		
		public static final String APPLICATION_VDOT_MODULE = "Vdot Module";
		
	}
	
	/**
	 * @see AbstractMetadataBundle#install()
	 */
	@Override
	public void install() {
		
		install(privilege(_Privilege.VDOT_MODULE_APP, "Able to access Vdot module features"));
		
		install(role(_Role.APPLICATION_VDOT_MODULE, "Can access Vdot module App",
		    idSet(org.openmrs.module.kenyaemr.metadata.SecurityMetadata._Role.API_PRIVILEGES_VIEW_AND_EDIT),
		    idSet(_Privilege.VDOT_MODULE_APP)));
		
		install(role(_Role.VDOT_CLINICIAN, "Can access Vdot module App",
		    idSet(org.openmrs.module.kenyaemr.metadata.SecurityMetadata._Role.API_PRIVILEGES_VIEW_AND_EDIT),
		    idSet(_Privilege.VDOT_MODULE_APP)));
	}
}
