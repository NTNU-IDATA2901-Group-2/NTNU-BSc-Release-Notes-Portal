package no.reliablesolutions.release_notes_portal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Token contract for mapping JWT claims to application authorities,
 * configurable per identity provider. Defaults match a Keycloak realm that
 * emits roles in a top-level "roles" claim and customer groups as
 * "/Customers/&lt;name&gt;" entries in a "groups" claim. For Entra ID, set
 * customer-claim=roles and customer-prefix=Customer: to use app roles like
 * "Customer:ACME".
 */
@ConfigurationProperties(prefix = "app.auth.claims")
public record AuthClaimsProperties(
    @DefaultValue("roles") String rolesClaim,
    @DefaultValue("groups") String customerClaim,
    @DefaultValue("/Customers/") String customerPrefix) {
}
