package ace.charitan.payment.internal.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@AllArgsConstructor
public class AuthModel implements UserDetails {

    private String id;

    @Getter
    private String email;

    private String roleId;

    private boolean active;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(this.roleId));
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return this.id;
    }

    @Override
    public boolean isEnabled() {
        return this.active;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Adjust as necessary
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Adjust as necessary
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Adjust as necessary
    }
}
