package be.lennertsoffers.supportportalapplication.service.impl;

import be.lennertsoffers.supportportalapplication.constant.FileConstant;
import be.lennertsoffers.supportportalapplication.domain.User;
import be.lennertsoffers.supportportalapplication.domain.UserPrincipal;
import be.lennertsoffers.supportportalapplication.enumeration.Role;
import be.lennertsoffers.supportportalapplication.exception.domain.EmailExistException;
import be.lennertsoffers.supportportalapplication.exception.domain.EmailNotFoundException;
import be.lennertsoffers.supportportalapplication.exception.domain.UserNotFoundException;
import be.lennertsoffers.supportportalapplication.exception.domain.UsernameExistException;
import be.lennertsoffers.supportportalapplication.repository.UserRepository;
import be.lennertsoffers.supportportalapplication.service.EmailService;
import be.lennertsoffers.supportportalapplication.service.LoginAttemptService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.*;
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
    private final LoginAttemptService loginAttemptService;
    private final EmailService emailService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userRepository.findUserByUsername(username);

        if (user == null) {
            this.LOGGER.error("User not found by username: " + username);
            throw new UsernameNotFoundException("User not found by username: " + username);
        } else {
            this.validateLoginAttempt(user);

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
        user.setProfileImageUrl(this.getTemporaryProfileImage(username));

        this.userRepository.save(user);

        this.LOGGER.info("New user password: " + password);

//        this.emailService.sendNewPasswordEmail(firstName, password, email);

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

    @Override
    public User addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException {
        this.validateNewUsernameAndEmail(StringUtils.EMPTY, username, email);
        User user = new User();
        String password = this.generatePassword();
        String encodedPassword = this.encodePassword(password);
        user.setUserId(this.generateUserId());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setJoinDate(new Date());
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setActive(isActive);
        user.setNotLocked(isNonLocked);
        user.setRole(this.getRoleEnumName(role).name());
        user.setAuthorities(this.getRoleEnumName(role).getAuthorities());
        user.setProfileImageUrl(this.getTemporaryProfileImage(username));

        this.userRepository.save(user);
        this.saveProfileImage(user, profileImage);

        return user;
    }

    @Override
    public User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername, String newEmail, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException {
        User currentUser = this.validateNewUsernameAndEmail(currentUsername, newUsername, newEmail);
        assert currentUser != null;
        currentUser.setFirstName(newFirstName);
        currentUser.setLastName(newLastName);
        currentUser.setJoinDate(new Date());
        currentUser.setUsername(newUsername);
        currentUser.setEmail(newEmail);
        currentUser.setActive(isActive);
        currentUser.setNotLocked(isNonLocked);
        currentUser.setRole(this.getRoleEnumName(role).name());
        currentUser.setAuthorities(this.getRoleEnumName(role).getAuthorities());

        this.userRepository.save(currentUser);
        this.saveProfileImage(currentUser, profileImage);

        return currentUser;
    }

    @Override
    public void deleteUser(long id) {
        this.userRepository.deleteById(id);
    }

    @Override
    public void resetPassword(String email) throws EmailNotFoundException {
        User user = this.userRepository.findUserByUsername(email);

        if (user == null) throw new EmailNotFoundException("No user found for email: " + email);

        String password = this.generatePassword();
        user.setPassword(this.encodePassword(password));

        this.userRepository.save(user);
//        this.emailService.sendNewPasswordEmail(user.getFirstName(), user.getPassword(), user.getEmail());
    }

    @Override
    public User updateProfileImage(String username, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException {
        User user = this.validateNewUsernameAndEmail(username, null, null);

        this.saveProfileImage(user, profileImage);

        return user;
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

    private String getTemporaryProfileImage(String username) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(FileConstant.DEFAULT_USER_IMAGE_PATH + username).toUriString();
    }

    private void validateLoginAttempt(User user) {
        if (user.isNotLocked()) user.setNotLocked(!this.loginAttemptService.hasExceededMaxAttempts(user.getUsername()));
        else this.loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
    }

    private Role getRoleEnumName(String role) {
        return Role.valueOf(role.toUpperCase());
    }

    private void saveProfileImage(User user, MultipartFile profileImage) throws IOException {
        if (profileImage != null) {
            Path userFolder = Paths.get(FileConstant.USER_FOLDER + user.getUsername()).toAbsolutePath().normalize();

            if (Files.exists(userFolder)) {
                Files.createDirectories(userFolder);
                LOGGER.info(FileConstant.DIRECTORY_CREATED);
            }

            Files.deleteIfExists(Paths.get(userFolder + user.getUsername() + FileConstant.DOT + FileConstant.JPG_EXTENSION));
            Files.copy(profileImage.getInputStream(), userFolder.resolve(user.getUsername() + FileConstant.DOT + FileConstant.JPG_EXTENSION), StandardCopyOption.REPLACE_EXISTING);

            user.setProfileImageUrl(this.setProfileImageUrl(user.getUsername()));

            this.userRepository.save(user);
        }
    }

    private String setProfileImageUrl(String username) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(FileConstant.USER_IMAGE_PATH + username + FileConstant.FORWARD_SLASH + username + FileConstant.DOT + FileConstant.JPG_EXTENSION).toUriString();
    }
}
