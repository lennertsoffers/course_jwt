package be.lennertsoffers.supportportalapplication.service;

import be.lennertsoffers.supportportalapplication.domain.User;
import be.lennertsoffers.supportportalapplication.exception.domain.EmailExistException;
import be.lennertsoffers.supportportalapplication.exception.domain.UserNotFoundException;
import be.lennertsoffers.supportportalapplication.exception.domain.UsernameExistException;

import java.util.List;

public interface UserService {
    User register(String firstName, String lastName, String username, String email) throws UserNotFoundException, EmailExistException, UsernameExistException;
    List<User> getUsers();
    User findUserByUsername(String username);
    User findUserByEmail(String email);
}
