package com.maltsev.clas.service.impl;

import com.maltsev.clas.entity.UserEntity;
import com.maltsev.clas.entity.status.StatusEnum;
import com.maltsev.clas.exception.UserNotFoundException;
import com.maltsev.clas.repository.UserRepository;
import com.maltsev.clas.request.SetNotificationsRequest;
import com.maltsev.clas.response.DeleteUserResponse;
import com.maltsev.clas.response.NotificationsResponse;
import com.maltsev.clas.response.UserResponse;
import com.maltsev.clas.service.UserService;
import com.maltsev.cross.message.NewUserMessage;
import com.maltsev.jwt.Auth;
import com.maltsev.jwt.UserAccount;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.maltsev.clas.constants.MessageConstants.USER_DELETED;
import static com.maltsev.clas.constants.MessageConstants.USER_DID_NOT_FIND;

@AllArgsConstructor
@Service
@Log
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse createUser(NewUserMessage message) {
        UserEntity userEntity = UserEntity.builder()
                .id(message.getId())
                .firstName(message.getFirstName())
                .lastName(message.getLastName())
                .email(message.getEmail())
                .activityInClasses(false)
                .informationAboutUpdates(false)
                .notificationClassStart(false)
                .status(StatusEnum.ACTIVE)
                .build();
        userRepository.save(userEntity);
        return getUserResponse(userEntity);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::getUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DeleteUserResponse deleteUserById(@Auth UserAccount user) {
        UserEntity userEntity = userRepository.findById(user.getUserId()).orElseThrow(() -> new UserNotFoundException(USER_DID_NOT_FIND));
        userEntity.setStatus(StatusEnum.DELETED);
        userRepository.save(userEntity);
        return new DeleteUserResponse(USER_DELETED);
    }

    @Override
    public NotificationsResponse setNotifications(UserAccount user, SetNotificationsRequest request) {
        UserEntity userEntity = userRepository.findById(user.getUserId()).orElseThrow(() -> new UserNotFoundException(USER_DID_NOT_FIND));
        userEntity.setActivityInClasses(request.isActivityInClasses());
        userEntity.setInformationAboutUpdates(request.isInformationAboutUpdates());
        userEntity.setNotificationClassStart(request.isNotificationClassStart());
        userRepository.save(userEntity);
        return NotificationsResponse.builder()
                .notificationClassStart(userEntity.isNotificationClassStart())
                .activityInClasses(userEntity.isActivityInClasses())
                .informationAboutUpdates(userEntity.isInformationAboutUpdates())
                .build();
    }

    @Override
    public NotificationsResponse getNotifications(UserAccount user) {
        UserEntity userEntity = userRepository.findById(user.getUserId()).orElseThrow(() -> new UserNotFoundException(USER_DID_NOT_FIND));
        return NotificationsResponse.builder()
                .notificationClassStart(userEntity.isNotificationClassStart())
                .activityInClasses(userEntity.isActivityInClasses())
                .informationAboutUpdates(userEntity.isInformationAboutUpdates())
                .build();
    }


    private UserResponse getUserResponse(UserEntity user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}
