package org.apereo.cas.pm.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.pm.BasePasswordManagementService;
import org.apereo.cas.pm.PasswordChangeBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link JdbcPasswordManagementService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class JdbcPasswordManagementService extends BasePasswordManagementService {


    private final JdbcTemplate jdbcTemplate;

    public JdbcPasswordManagementService(final CipherExecutor<Serializable, String> cipherExecutor,
                                         final String issuer,
                                         final PasswordManagementProperties passwordManagementProperties,
                                         final DataSource dataSource) {
        super(passwordManagementProperties, cipherExecutor, issuer);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    @Override
    public boolean changeInternal(final Credential credential, final PasswordChangeBean bean) {
        final var c = (UsernamePasswordCredential) credential;
        final var encoder = PasswordEncoderUtils.newPasswordEncoder(properties.getJdbc().getPasswordEncoder());
        final var password = encoder.encode(bean.getPassword());
        final var count = this.jdbcTemplate.update(properties.getJdbc().getSqlChangePassword(), password, c.getId());
        return count > 0;
    }

    @Override
    public String findEmail(final String username) {
        try {
            final var email = this.jdbcTemplate.queryForObject(properties.getJdbc().getSqlFindEmail(), String.class, username);
            if (StringUtils.isNotBlank(email) && EmailValidator.getInstance().isValid(email)) {
                return email;
            }
            LOGGER.debug("Username {} not found when searching for email", username);
            return null;
        } catch (final EmptyResultDataAccessException e) {
            LOGGER.debug("Username {} not found when searching for email", username);
            return null;
        }
    }

    @Override
    public Map<String, String> getSecurityQuestions(final String username) {
        final var sqlSecurityQuestions = properties.getJdbc().getSqlSecurityQuestions();
        final Map<String, String> map = new LinkedHashMap<>();
        final var results = jdbcTemplate.queryForList(sqlSecurityQuestions, username);
        results.forEach(row -> {
            if (row.containsKey("question") && row.containsKey("answer")) {
                map.put(row.get("question").toString(), row.get("answer").toString());
            }
        });
        LOGGER.debug("Found [{}] security questions for [{}]", map.size(), username);
        return map;
    }
}
