package be.lennertsoffers.supportportalapplication.service.impl;

import be.lennertsoffers.supportportalapplication.domain.User;
import be.lennertsoffers.supportportalapplication.domain.UserPrincipal;
import be.lennertsoffers.supportportalapplication.repository.UserRepository;
import be.lennertsoffers.supportportalapplication.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Transient;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Transient
@Qualifier("userDetailsService")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {
    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userRepository.findUserByUsername(username);

        if (user == null) {
            this.LOGGER.error("User not found by username: " + username);
            throw new UsernameNotFoundException("User not found by username: " + username);
        } else {
            user.setLastLoginDateDisplay(user.getLastLoginDate());
            user.setLastLoginDate(new Date());

            this.userRepository.save(user);

            UserPrincipal userPrincipal = new UserPrincipal(user);

            this.LOGGER.info("Returning found user by username: " + username);

            return userPrincipal;
        }
    }
}
