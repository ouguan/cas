package org.apereo.cas.support.saml.util;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

/**
 * This is {@link GoogleSaml20ObjectBuilder} that
 * attempts to build the saml response. QName based on the spec described here:
 * https://developers.google.com/google-apps/sso/saml_reference_implementation_web#samlReferenceImplementationWebSetupChangeDomain
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class GoogleSaml20ObjectBuilder extends AbstractSaml20ObjectBuilder {
    private static final long serialVersionUID = 2979638064754730668L;

    public GoogleSaml20ObjectBuilder(final OpenSamlConfigBean configBean) {
        super(configBean);
    }

    @Override
    public QName getSamlObjectQName(final Class objectType) {
        try {
            final var f = objectType.getField(DEFAULT_ELEMENT_LOCAL_NAME_FIELD);
            final var name = f.get(null).toString();

            if (objectType.equals(Response.class) || objectType.equals(Status.class)
                    || objectType.equals(StatusCode.class)) {
                return new QName(SAMLConstants.SAML20P_NS, name, "samlp");
            }
            return new QName(SAMLConstants.SAML20_NS, name, XMLConstants.DEFAULT_NS_PREFIX);
        } catch (final Exception e) {
            throw new IllegalStateException("Cannot access field " + objectType.getName() + '.' + DEFAULT_ELEMENT_LOCAL_NAME_FIELD);
        }
    }
}
