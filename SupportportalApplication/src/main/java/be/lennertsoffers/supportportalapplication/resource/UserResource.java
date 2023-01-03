package be.lennertsoffers.supportportalapplication.resource;

import be.lennertsoffers.supportportalapplication.constant.FileConstant;
import be.lennertsoffers.supportportalapplication.constant.SecurityConstant;
import be.lennertsoffers.supportportalapplication.domain.HttpResponse;
import be.lennertsoffers.supportportalapplication.domain.User;
import be.lennertsoffers.supportportalapplication.domain.UserPrincipal;
import be.lennertsoffers.supportportalapplication.exception.domain.*;
import be.lennertsoffers.supportportalapplication.service.UserService;
import be.lennertsoffers.supportportalapplication.util.JwtTokenProvider;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

@RestController
@RequestMapping(path = "/user")
@CrossOrigin("http://localhost:4200")
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
        System.out.println(newUser);
        return ResponseEntity.ok(newUser);
    }

    @PostMapping("/add")
    public ResponseEntity<User> addNewUser(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("role") String role,
            @RequestParam("isActive") String isActive,
            @RequestParam("isNonLocked") String isNonLocked,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage
    ) throws UserNotFoundException, EmailExistException, IOException, UsernameExistException {
        User newUser = this.userService.addNewUser(
                firstName,
                lastName,
                username,
                email,
                role,
                Boolean.parseBoolean(isNonLocked),
                Boolean.parseBoolean(isActive),
                profileImage
        );

        return ResponseEntity.ok(newUser);
    }

    @PostMapping("/update")
    public ResponseEntity<User> update(
            @RequestParam("currentUsername") String currentUsername,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("role") String role,
            @RequestParam("isActive") String isActive,
            @RequestParam("isNonLocked") String isNonLocked,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage
    ) throws UserNotFoundException, EmailExistException, IOException, UsernameExistException {
        User updatedUser = this.userService.updateUser(
                currentUsername,
                firstName,
                lastName,
                username,
                email,
                role,
                Boolean.parseBoolean(isNonLocked),
                Boolean.parseBoolean(isActive),
                profileImage
        );

        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/find/{username}")
    public ResponseEntity<User> getUser(@PathVariable("username") String username) {
        User user = this.userService.findUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/list")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = this.userService.getUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/resetPassword/{email}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email) throws EmailNotFoundException, MessagingException {
        this.userService.resetPassword(email);
        return this.response("An email with new password was sent to: " + email);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('user:delete')")
    public ResponseEntity<HttpResponse> deleteUser(@PathVariable("id") long id) {
        this.userService.deleteUser(id);
        return this.response("User was deleted successfully");
    }

    @PostMapping("/updateProfileImage")
    public ResponseEntity<User> updateProfileImage(
            @RequestParam("username") String username,
            @RequestParam("profileImage") MultipartFile profileImage
    ) throws UserNotFoundException, EmailExistException, IOException, UsernameExistException {
        User user = this.userService.updateProfileImage(username, profileImage);
        return ResponseEntity.ok(user);
    }

    @GetMapping(path = "/image/{username}/{filename}", produces = IMAGE_JPEG_VALUE)
    public byte[] getProfileImage(@PathVariable("username") String username, @PathVariable("filename") String filename) throws IOException {
        return Files.readAllBytes(Paths.get(FileConstant.USER_FOLDER + username + FileConstant.FORWARD_SLASH + filename));
    }

    @GetMapping(path = "/image/profile/{username}", produces = IMAGE_JPEG_VALUE)
    public byte[] getTempProfileImage(@PathVariable("username") String username) throws IOException {
        URL url = new URL(FileConstant.TEMP_PROFILE_IMAGE_BASE_URL + username);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        InputStream inputStream = url.openStream();
        int bytesRead;
        byte[] chunk = new byte[1024];
        while ((bytesRead = inputStream.read(chunk)) > 0) {
            byteArrayOutputStream.write(chunk, 0, bytesRead);
        }

        return byteArrayOutputStream.toByteArray();
    }

    private ResponseEntity<HttpResponse> response(String message) {
        HttpResponse httpResponse = new HttpResponse(
                HttpStatus.OK.value(),
                HttpStatus.OK,
                HttpStatus.OK.getReasonPhrase().toUpperCase(),
                message.toUpperCase()
        );

        return new ResponseEntity<>(httpResponse, HttpStatus.OK);
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
