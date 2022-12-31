package be.lennertsoffers.supportportalapplication.service.impl;

import be.lennertsoffers.supportportalapplication.domain.User;
import be.lennertsoffers.supportportalapplication.domain.UserPrincipal;
import be.lennertsoffers.supportportalapplication.enumeration.Role;
import be.lennertsoffers.supportportalapplication.exception.domain.EmailExistException;
import be.lennertsoffers.supportportalapplication.exception.domain.UserNotFoundException;
import be.lennertsoffers.supportportalapplication.exception.domain.UsernameExistException;
import be.lennertsoffers.supportportalapplication.repository.UserRepository;
import be.lennertsoffers.supportportalapplication.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Transient;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Date;
import java.util.List;

@Service
@Transient
@Qualifier("userDetailsService")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {
    public static final String USERNAME_ALREADY_EXISTS = "Username already exists";
    public static final String EMAIL_ALREADY_EXISTS = "Email already exists";

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

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

    @Override
    public User register(String firstName, String lastName, String username, String email) throws UserNotFoundException, EmailExistException, UsernameExistException {
        this.validateNewUsernameAndEmail(StringUtils.EMPTY, username, email);

        User user = new User();
        user.setUserId(this.generateUserId());
        String password = this.generatePassword();
        String encodedPassword = this.encodePassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setJoinDate(new Date());
        user.setPassword(encodedPassword);
        user.setActive(true);
        user.setNotLocked(true);
        user.setRole(Role.ROLE_USER.name());
        user.setAuthorities(Role.ROLE_USER.getAuthorities());
        user.setProfileImageUrl(this.getTemporaryProfileImage());

        this.userRepository.save(user);

        this.LOGGER.info("New user password: " + password);

        return null;
    }

    @Override
    public List<User> getUsers() {
        return this.userRepository.findAll();
    }

    @Override
    public User findUserByUsername(String username) {
        return this.userRepository.findUserByUsername(username);
    }

    @Override
    public User findUserByEmail(String email) {
        return this.userRepository.findUserByEmail(email);
    }

    private User validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail) throws UsernameExistException, EmailExistException, UserNotFoundException {
        User userByUsername = this.findUserByUsername(newUsername);
        User userByEmail = this.findUserByEmail(newEmail);
        if (StringUtils.isNotBlank(currentUsername)) {
            User currentUser = this.findUserByUsername(currentUsername);
            if (currentUser == null) {
                throw new UserNotFoundException("No user found by username " + currentUsername);
            }

            if (userByUsername != null && !currentUser.getUserId().equals(userByUsername.getUserId())) {
                throw new UsernameExistException(USERNAME_ALREADY_EXISTS);
            }

            if (userByEmail != null && !currentUser.getUserId().equals(userByEmail.getUserId())) {
                throw new EmailExistException(EMAIL_ALREADY_EXISTS);
            }

            return currentUser;
        } else {
            if (userByUsername != null) {
                throw new UsernameExistException(USERNAME_ALREADY_EXISTS);
            }

            if (userByEmail != null) {
                throw new EmailExistException(EMAIL_ALREADY_EXISTS);
            }

            return null;
        }
    }

    private String generateUserId() {
        return RandomStringUtils.randomNumeric(10);
    }

    private String generatePassword() {
        return RandomStringUtils.randomAlphabetic(10);
    }

    private String encodePassword(String password) {
        return this.passwordEncoder.encode(password);
    }

    private String getTemporaryProfileImage() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/image/profile/temp").toUriString();
    }
}
