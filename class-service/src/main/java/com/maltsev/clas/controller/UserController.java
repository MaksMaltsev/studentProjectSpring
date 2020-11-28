package com.maltsev.clas.controller;

import com.maltsev.clas.request.SetNotificationsRequest;
import com.maltsev.clas.response.DeleteUserResponse;
import com.maltsev.clas.response.NotificationsResponse;
import com.maltsev.clas.response.UserResponse;
import com.maltsev.clas.service.UserService;
import com.maltsev.jwt.Auth;
import com.maltsev.jwt.UserAccount;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.maltsev.clas.constants.URIConstants.*;

@AllArgsConstructor
@RestController
public class UserController {
    private final UserService userService;

    @GetMapping(ALL_USERS)
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @DeleteMapping(USER)
    public DeleteUserResponse deleteUser(@Auth UserAccount user) {
        return userService.deleteUserById(user);
    }

    @PostMapping(NOTIFICATIONS)
    public NotificationsResponse setNotifications(@Auth UserAccount user,
                                                  @RequestBody SetNotificationsRequest request) {
        return userService.setNotifications(user, request);
    }

    @GetMapping(NOTIFICATIONS)
    public NotificationsResponse getNotifications(@Auth UserAccount user) {
        return userService.getNotifications(user);
    }
}
