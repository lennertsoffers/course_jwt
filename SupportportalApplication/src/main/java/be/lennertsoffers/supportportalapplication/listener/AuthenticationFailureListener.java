package be.lennertsoffers.supportportalapplication.listener;

import be.lennertsoffers.supportportalapplication.service.LoginAttemptService;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
@AllArgsConstructor
public class AuthenticationFailureListener {
    private LoginAttemptService loginAttemptService;

    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) throws ExecutionException {
        Object principal = event.getAuthentication().getPrincipal();

        if (principal instanceof String username) {
            this.loginAttemptService.addUserToLoginAttemptCache(username);
        }
    }
}
