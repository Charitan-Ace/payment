package ace.charitan.payment.internal.auth;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthDetailsService implements UserDetailsService {

    private String roleId;
    private String email;

    public void setRole(String roleId, String email) {
        this.roleId = roleId;
        this.email = email;
    }

    @Override
    public AuthModel loadUserByUsername(String username) throws UsernameNotFoundException {
        return new AuthModel(username, email, roleId, true);
    }
}
