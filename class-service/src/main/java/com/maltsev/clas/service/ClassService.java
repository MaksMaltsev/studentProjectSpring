package com.maltsev.clas.service;

import com.maltsev.clas.request.AddSubscribersRequest;
import com.maltsev.clas.request.CreateClassRequest;
import com.maltsev.clas.response.AddSubscribersResponse;
import com.maltsev.clas.response.ClassInfoResponse;
import com.maltsev.clas.response.UserResponse;
import com.maltsev.cross.message.ClassStatusMessage;
import com.maltsev.cross.message.EmailMessage;
import com.maltsev.jwt.Auth;
import com.maltsev.jwt.UserAccount;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface ClassService {
    ClassInfoResponse createClass(UserAccount user, CreateClassRequest createClassRequest);

    ClassInfoResponse getClassInfo(String id);

    List<ClassInfoResponse> getAllClass();

    List<ClassInfoResponse> getMyClass(UserAccount user);

    List<UserResponse> getMyClassSubscribers(UserAccount user);

    AddSubscribersResponse addSubscribers(UserAccount user, AddSubscribersRequest request);

    void setStatus(ClassStatusMessage message);


}
