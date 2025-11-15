package com.dentallab.security.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dentallab.persistence.entity.RoleEntity;
import com.dentallab.persistence.entity.UserAccountEntity;
import com.dentallab.persistence.entity.UserRoleEntity;
import com.dentallab.persistence.repository.UserAccountRepository;
import com.dentallab.security.model.CustomUserDetails;

/**
 * Loads user credentials and roles from the database.
 * <p>
 * Supports lookup by username OR email.
 * Enforces 'enabled' and 'locked' flags from UserAccountEntity.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserAccountRepository userAccountRepository;

    public CustomUserDetailsService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        log.debug("Attempting to load user '{}'", usernameOrEmail);

        UserAccountEntity user = userAccountRepository
                .findByUsernameOrEmailAndEnabledTrue(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found or not enabled: " + usernameOrEmail));

        if (Boolean.TRUE.equals(user.isLocked())) {
            throw new UsernameNotFoundException("User account is locked: " + usernameOrEmail);
        }

        // user.getUserRoles() -> each maps to RoleEntity
        List<SimpleGrantedAuthority> authorities = user.getUserRoles().stream()
                .map(UserRoleEntity::getRole)
                .map(RoleEntity::getName)
                .map(roleName -> roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        
        return new CustomUserDetails(
                user.getUsername(),
                user.getPasswordHash(),     // stored BCrypt hash
                user.getEmail(),
                authorities,
                user.isEnabled(),
                !user.isLocked()
        );
    }
}
	