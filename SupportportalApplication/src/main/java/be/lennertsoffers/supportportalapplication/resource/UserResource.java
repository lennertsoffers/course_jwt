package be.lennertsoffers.supportportalapplication.resource;

import be.lennertsoffers.supportportalapplication.exception.domain.ExceptionHandling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/user")
public class UserResource extends ExceptionHandling {
    @GetMapping("/home")
    public String showUser() {
        return "Application works";
    }
}
