package be.lennertsoffers.supportportalapplication.resource;

import be.lennertsoffers.supportportalapplication.domain.User;
import be.lennertsoffers.supportportalapplication.exception.domain.EmailExistException;
import be.lennertsoffers.supportportalapplication.exception.domain.ExceptionHandling;
import be.lennertsoffers.supportportalapplication.exception.domain.UserNotFoundException;
import be.lennertsoffers.supportportalapplication.exception.domain.UsernameExistException;
import be.lennertsoffers.supportportalapplication.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/user")
@AllArgsConstructor
public class UserResource extends ExceptionHandling {
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) throws UserNotFoundException, EmailExistException, UsernameExistException {
        User newUser = this.userService.register(user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail());
        return ResponseEntity.ok(newUser);
    }
}
