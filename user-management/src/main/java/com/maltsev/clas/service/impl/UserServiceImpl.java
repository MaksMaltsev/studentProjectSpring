package com.maltsev.clas.service.impl;

import com.maltsev.clas.entity.UserEntity;
import com.maltsev.clas.exception.ChangePasswordException;
import com.maltsev.clas.exception.UserNotFoundException;
import com.maltsev.clas.repository.UserRepository;
import com.maltsev.clas.request.PasswordRequest;
import com.maltsev.clas.request.RegistrationRequest;
import com.maltsev.clas.response.AuthResponse;
import com.maltsev.clas.response.ChangePasswordResponse;
import com.maltsev.clas.service.UserService;
import com.maltsev.cross.message.NewUserMessage;
import com.maltsev.jwt.UserAccount;
import com.maltsev.jwt.provider.JwtTokenProvider;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.UUID;

import static com.maltsev.clas.config.RabbitMQConfig.NEW_USER_QUEUE;

@AllArgsConstructor
@Service
@Log
@CrossOrigin("*")
public class UserServiceImpl implements UserService {

    private final JwtTokenProvider jwtProvider;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final RabbitTemplate rabbitTemplate;


    @Override
    public AuthResponse saveUser(RegistrationRequest registrationRequest) {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(UUID.randomUUID().toString());
        userEntity.setLogin(registrationRequest.getLogin());
        userEntity.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        userEntity.setName(registrationRequest.getName());
        userEntity.setLastName(registrationRequest.getLastName());
        UserEntity savedUser = userRepository.save(userEntity);
        transferUser(savedUser);
        String token = jwtProvider.generateToken(savedUser.getId());
        return new AuthResponse(token);
    }


    @Override
    public UserEntity findByUserId(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Override
    public AuthResponse findByLoginAndPassword(String login, String password) {
        UserEntity userEntity = findByUserId(login);
        if (userEntity != null) {
            boolean check;
            check = passwordEncoder.matches(password, userEntity.getPassword());
            if (check) {
                String token = jwtProvider.generateToken(userEntity.getId());
                return new AuthResponse(token);
            }
        }
        return null;
    }

    @Override
    public ChangePasswordResponse changePassword(UserAccount userAccount, PasswordRequest passwordRequest) {
        ChangePasswordResponse result = new ChangePasswordResponse();
        result.setResult("Please try again");
        UserEntity user = findByUserId(userAccount.getUserId());
        if (!passwordEncoder.matches(passwordRequest.getOldPassword(), user.getPassword()))
            throw new ChangePasswordException("Old password is not correct");
        if (!passwordRequest.getFirstPassword().equals(passwordRequest.getSecondPassword()))
            throw new ChangePasswordException("The second password does not match the first");
        user.setPassword(passwordEncoder.encode(passwordRequest.getFirstPassword()));
        userRepository.save(user);
        result.setResult("Password changed");
        return result;
    }

    private void transferUser(UserEntity user) {
        NewUserMessage newUserMessage = NewUserMessage.builder()
                .id(user.getId())
                .firstName(user.getName())
                .lastName(user.getLastName())
                .email(user.getLogin())
                .build();
        rabbitTemplate.convertAndSend(NEW_USER_QUEUE, newUserMessage);
    }
}
