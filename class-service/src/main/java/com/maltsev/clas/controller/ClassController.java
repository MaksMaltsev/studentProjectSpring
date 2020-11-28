package com.maltsev.clas.controller;

import com.maltsev.clas.request.AddSubscribersRequest;
import com.maltsev.clas.request.CreateClassRequest;
import com.maltsev.clas.response.AddSubscribersResponse;
import com.maltsev.clas.response.ClassInfoResponse;
import com.maltsev.clas.response.UserResponse;
import com.maltsev.clas.service.ClassService;
import com.maltsev.jwt.Auth;
import com.maltsev.jwt.UserAccount;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.maltsev.clas.constants.URIConstants.*;

@AllArgsConstructor
@RestController
public class ClassController {
    private final ClassService classService;

    @GetMapping(CLASS_INFO)
    public ClassInfoResponse getClassInfo(@RequestParam String id) {
        return classService.getClassInfo(id);
    }

    @PostMapping(CLASS_INFO)
    public ClassInfoResponse createClass(@Auth UserAccount user,
                                         @RequestBody CreateClassRequest request) {
        return classService.createClass(user, request);
    }

    @GetMapping(ALL_CLASS_INFO)
    public List<ClassInfoResponse> getAllClass() {
        return classService.getAllClass();
    }

    @GetMapping(MY_CLASS_INFO)
    public List<ClassInfoResponse> getMyClass(@Auth UserAccount user) {
        return classService.getMyClass(user);
    }

    @GetMapping(MY_CLASS_SUBSCRIBERS)
    public List<UserResponse> getMyClassSubscribers(@Auth UserAccount user) {
        return classService.getMyClassSubscribers(user);
    }

    @PostMapping(SUBSCRIBE)
    public AddSubscribersResponse addSubscribers(@Auth UserAccount user,
                                                 @RequestBody AddSubscribersRequest request) {
        return classService.addSubscribers(user, request);
    }
}
