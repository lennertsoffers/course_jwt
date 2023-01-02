package be.lennertsoffers.supportportalapplication.resource;

import be.lennertsoffers.supportportalapplication.constant.SecurityConstant;
import be.lennertsoffers.supportportalapplication.domain.User;
import be.lennertsoffers.supportportalapplication.domain.UserPrincipal;
import be.lennertsoffers.supportportalapplication.exception.domain.EmailExistException;
import be.lennertsoffers.supportportalapplication.exception.domain.ExceptionHandling;
import be.lennertsoffers.supportportalapplication.exception.domain.UserNotFoundException;
import be.lennertsoffers.supportportalapplication.exception.domain.UsernameExistException;
import be.lennertsoffers.supportportalapplication.service.UserService;
import be.lennertsoffers.supportportalapplication.util.JwtTokenProvider;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;

@RestController
@RequestMapping(path = "/user")
@AllArgsConstructor
public class UserResource extends ExceptionHandling {
    private UserService userService;
    private AuthenticationManager authenticationManager;
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User user) {
        this.authenticate(user.getUsername(), user.getPassword());
        User loginUser = this.userService.findUserByUsername(user.getUsername());
        UserPrincipal userPrincipal = new UserPrincipal(loginUser);

        HttpHeaders jwtHeader = this.getJwtHeader(userPrincipal);

        return new ResponseEntity<>(loginUser, jwtHeader, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) throws UserNotFoundException, EmailExistException, UsernameExistException, MessagingException {
        User newUser = this.userService.register(user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail());
        return ResponseEntity.ok(newUser);
    }

    private void authenticate(String username, String password) {
        this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    private HttpHeaders getJwtHeader(UserPrincipal userPrincipal) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(SecurityConstant.JWT_TOKEN_HEADER, this.jwtTokenProvider.generateJwtToken(userPrincipal));

        return headers;
    }
}
