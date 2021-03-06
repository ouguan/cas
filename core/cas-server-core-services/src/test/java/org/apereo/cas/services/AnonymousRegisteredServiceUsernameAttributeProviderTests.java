package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
public class AnonymousRegisteredServiceUsernameAttributeProviderTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "anonymousRegisteredServiceUsernameAttributeProvider.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String CASROX = "casrox";

    @Test
    public void verifyPrincipalResolution() {
        final var provider = new AnonymousRegisteredServiceUsernameAttributeProvider(
            new ShibbolethCompatiblePersistentIdGenerator(CASROX));

        final var service = mock(Service.class);
        when(service.getId()).thenReturn("id");
        final var principal = mock(Principal.class);
        when(principal.getId()).thenReturn("uid");
        final var id = provider.resolveUsername(principal, service, RegisteredServiceTestUtils.getRegisteredService("id"));
        assertNotNull(id);
    }

    @Test
    public void verifyEquality() {
        final var provider = new AnonymousRegisteredServiceUsernameAttributeProvider(
            new ShibbolethCompatiblePersistentIdGenerator(CASROX));
        final var provider2 = new AnonymousRegisteredServiceUsernameAttributeProvider(
            new ShibbolethCompatiblePersistentIdGenerator(CASROX));

        assertEquals(provider, provider2);
    }

    @Test
    public void verifySerializeADefaultRegisteredServiceUsernameProviderToJson() throws IOException {
        final var providerWritten = new AnonymousRegisteredServiceUsernameAttributeProvider(
            new ShibbolethCompatiblePersistentIdGenerator(CASROX));
        MAPPER.writeValue(JSON_FILE, providerWritten);
        final RegisteredServiceUsernameAttributeProvider providerRead = MAPPER.readValue(JSON_FILE, AnonymousRegisteredServiceUsernameAttributeProvider.class);
        assertEquals(providerWritten, providerRead);
    }

    @Test
    public void verifyGeneratedIdsMatch() {
        final var salt = "nJ+G!VgGt=E2xCJp@Kb+qjEjE4R2db7NEW!9ofjMNas2Tq3h5h!nCJxc3Sr#kv=7JwU?#MN=7e+r!wpcMw5RF42G8J"
            + "8tNkGp4g4rFZ#RnNECL@wZX5=yia+KPEwwq#CA9EM38=ZkjK2mzv6oczCVC!m8k!=6@!MW@xTMYH8eSV@7yc24Bz6NUstzbTWH3pnGojZm7pW8N"
            + "wjLypvZKqhn7agai295kFBhMmpS\n9Jz9+jhVkJfFjA32GiTkZ5hvYiFG104xWnMbHk7TsGrfw%tvACAs=f3C";
        final var gen = new ShibbolethCompatiblePersistentIdGenerator(salt);
        gen.setAttribute("employeeId");
        final var provider = new AnonymousRegisteredServiceUsernameAttributeProvider(gen);
        final var result = provider.resolveUsername(CoreAuthenticationTestUtils.getPrincipal("anyuser",
            CollectionUtils.wrap("employeeId", "T911327")),
            CoreAuthenticationTestUtils.getService("https://cas.example.org/app"),
            CoreAuthenticationTestUtils.getRegisteredService());
        assertEquals("ujWTRNKPPso8S+4geOvcOZtv778=", result);
    }

    @Test
    public void verifyGeneratedIdsMatchMultiValuedAttribute() {
        final var salt = "whydontyoustringmealong";
        final var gen = new ShibbolethCompatiblePersistentIdGenerator(salt);
        gen.setAttribute("uid");
        final var provider = new AnonymousRegisteredServiceUsernameAttributeProvider(gen);
        final var result = provider.resolveUsername(CoreAuthenticationTestUtils.getPrincipal("anyuser",
            CollectionUtils.wrap("uid", CollectionUtils.wrap("obegon"))),
            CoreAuthenticationTestUtils.getService("https://sp.testshib.org/shibboleth-sp"),
            CoreAuthenticationTestUtils.getRegisteredService());
        assertEquals("lykoGRE9QbbrsEBlHJVEz0U8AJ0=", result);
    }
}
