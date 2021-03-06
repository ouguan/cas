package org.apereo.cas.digest.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.auth.DigestScheme;
import org.apereo.cas.util.RandomUtils;

import java.time.ZonedDateTime;

/**
 * This is {@link DigestAuthenticationUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@UtilityClass
public class DigestAuthenticationUtils {

    /**
     * Create nonce string.
     *
     * @return the nonce
     */
    public static String createNonce() {
        final var fmtDate = ZonedDateTime.now().toString();
        final var rand = RandomUtils.getNativeInstance();
        final Integer randomInt = rand.nextInt();
        return DigestUtils.md5Hex(fmtDate + randomInt);
    }

    /**
     * Create cnonce string.
     *
     * @return the cnonce
     */
    public static String createCnonce() {
        return DigestScheme.createCnonce();
    }

    /**
     * Create opaque.
     *
     * @param domain the domain
     * @param nonce  the nonce
     * @return the opaque
     */
    public static String createOpaque(final String domain, final String nonce) {
        return DigestUtils.md5Hex(domain + nonce);
    }

    /**
     * Create authenticate header, containing the realm, nonce, opaque, etc.
     *
     * @param realm      the realm
     * @param authMethod the auth method
     * @param nonce      the nonce
     * @return the header string
     */
    public static String createAuthenticateHeader(final String realm, final String authMethod, final String nonce) {
        final var stringBuilder = new StringBuilder("Digest realm=\"")
            .append(realm).append("\",");
        if (StringUtils.isNotBlank(authMethod)) {
            stringBuilder.append("qop=").append(authMethod).append(',');
        }
        return stringBuilder.append("nonce=\"").append(nonce)
                .append("\",opaque=\"").append(createOpaque(realm, nonce))
                .append('"')
                .toString();
    }
}
