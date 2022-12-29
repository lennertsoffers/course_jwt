package be.lennertsoffers.supportportalapplication.resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/user")
public class UserResource {
    @GetMapping("/user")
    public String showUser() {
        return "Application works";
    }
}
