package org.openmrs.module.vdot.metadata;

import org.openmrs.module.metadatadeploy.bundle.AbstractMetadataBundle;
import org.openmrs.module.metadatadeploy.bundle.Requires;
import org.springframework.stereotype.Component;

import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.idSet;
import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.privilege;
import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.role;

/**
 * Implementation of access control to the app.
 */
@Component
@Requires(org.openmrs.module.kenyaemr.metadata.SecurityMetadata.class)
public class VdotSecurityMetadata extends AbstractMetadataBundle {

    public static class _Privilege {
        public static final String APP_NIMECONFIRM_ADMIN = "App: vdot.vdot";
    }

    public static final class _Role {
        public static final String APPLICATION_NIMECONFIRM_ADMIN = "nimeCONFIRM app administration";
    }

    /**
     * @see AbstractMetadataBundle#install()
     */
    @Override
    public void install() {

        install(privilege(_Privilege.APP_NIMECONFIRM_ADMIN, "Able to administer nimeCONFIRM actions"));
        install(role(_Role.APPLICATION_NIMECONFIRM_ADMIN, "Can access nimeCONFIRM app", idSet(
                org.openmrs.module.kenyaemr.metadata.SecurityMetadata._Role.API_PRIVILEGES_VIEW_AND_EDIT
        ), idSet(
                _Privilege.APP_NIMECONFIRM_ADMIN
        )));
    }
}
