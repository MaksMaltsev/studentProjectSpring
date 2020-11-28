package com.maltsev.clas.service;

import com.maltsev.clas.entity.UserEntity;
import com.maltsev.clas.request.LoginRequest;
import com.maltsev.clas.request.PasswordRequest;
import com.maltsev.clas.request.RegistrationRequest;
import com.maltsev.clas.response.AuthResponse;
import com.maltsev.clas.response.ChangePasswordResponse;
import com.maltsev.jwt.UserAccount;

public interface UserService {
    AuthResponse saveUser(RegistrationRequest registrationRequest);

    UserEntity findByUserId(String login);

    AuthResponse findByLoginAndPassword(String login, String password);

    ChangePasswordResponse changePassword(UserAccount userAccount, PasswordRequest passwordRequest);

}
