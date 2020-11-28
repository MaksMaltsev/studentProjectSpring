package com.maltsev.clas.service.impl;

import com.maltsev.clas.entity.ClassEntity;
import com.maltsev.clas.entity.UserEntity;
import com.maltsev.clas.exception.ClassNotFoundException;
import com.maltsev.clas.exception.CreateClassException;
import com.maltsev.clas.exception.UserNotFoundException;
import com.maltsev.clas.repository.ClassRepository;
import com.maltsev.clas.repository.UserRepository;
import com.maltsev.clas.request.AddSubscribersRequest;
import com.maltsev.clas.request.CreateClassRequest;
import com.maltsev.clas.response.AddSubscribersResponse;
import com.maltsev.clas.response.ClassInfoResponse;
import com.maltsev.clas.response.UserResponse;
import com.maltsev.clas.service.ClassService;
import com.maltsev.cross.JobType;
import com.maltsev.cross.constatns.Status;
import com.maltsev.cross.message.ClassStatusMessage;
import com.maltsev.cross.message.EmailMessage;
import com.maltsev.cross.request.JobRequest;
import com.maltsev.jwt.UserAccount;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.maltsev.clas.config.RabbitMQConfig.NOTIFICATION_QUEUE;
import static com.maltsev.clas.config.RabbitMQConfig.SCHEDULER_QUEUE;
import static com.maltsev.clas.constants.MessageConstants.*;

@Service
@AllArgsConstructor
public class ClassServiceImpl implements ClassService {
    private final UserRepository userRepository;

    private final ClassRepository classRepository;

    private final RabbitTemplate rabbitTemplate;

    @Override
    public ClassInfoResponse createClass(UserAccount account, CreateClassRequest request) {
        LocalDate now = LocalDate.now();
        checkNotNullDates(request);
        checkOnBefore(now, request);
        checkSequenceOfDates(request);
        UserEntity userEntity = userRepository.findById(account.getUserId()).orElseThrow(() -> new UserNotFoundException(USER_DID_NOT_FIND));
        ClassEntity classEntity = ClassEntity.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .startTime(request.getStartTime())
                .finishTime(request.getFinishTime())
                .description(request.getDescription())
                .creator(userEntity)
                .status(Status.WAIT)
                .build();
        classRepository.save(classEntity);
        sendEmailToCreator(classEntity);
        schedulerClassStart(classEntity);
        return toClassInfoResponse(classEntity);
    }

    @Override
    public ClassInfoResponse getClassInfo(String id) {
        return classRepository.findById(id)
                .map(this::toClassInfoResponse)
                .orElse(null);
    }

    @Override
    public List<ClassInfoResponse> getAllClass() {
        return classRepository.findAll()
                .stream()
                .map(this::toClassInfoResponse)
                .collect(Collectors.toList());
    }


    @Override
    public List<ClassInfoResponse> getMyClass(UserAccount user) {
        return classRepository.findAllByCreatorId(user.getUserId())
                .stream()
                .map(this::toClassInfoResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getMyClassSubscribers(UserAccount user) {
        return classRepository.findAllByCreatorId(user.getUserId())
                .stream()
                .map(ClassEntity::getSubscribers)
                .flatMap(Collection::stream)
                .map(this::getUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AddSubscribersResponse addSubscribers(UserAccount user, AddSubscribersRequest request) {
        UserEntity userEntity = userRepository.findById(user.getUserId()).orElseThrow(() -> new UserNotFoundException(USER_DID_NOT_FIND));
        ClassEntity classEntity = classRepository.findById(request.getClassId()).orElseThrow(() -> new ClassNotFoundException(CLASS_DID_NOT_FIND));
        classEntity.addSubscriber(userEntity);
        classRepository.save(classEntity);
        return new AddSubscribersResponse(SUBSCRIBER_ADDED);
    }

    @Override
    public void setStatus(ClassStatusMessage message) {
        ClassEntity classEntity = classRepository.findById(message.getClassId()).orElseThrow(() -> new ClassNotFoundException(CLASS_DID_NOT_FIND));
        classEntity.setStatus(message.getStatus());
        classRepository.save(classEntity);
        if (message.getStatus().equals(Status.IN_PROGRESS)) {
            schedulerEndClass(classEntity);
        }
    }


    private void checkSequenceOfDates(CreateClassRequest request) {
        if (request.getStartTime().isAfter(request.getFinishTime())) {
            throw new CreateClassException(START_FINISH_TIME_ERROR);
        }
    }

    private void checkNotNullDates(CreateClassRequest request) {
        if (request.getStartTime() == null || request.getFinishTime() == null) {
            throw new CreateClassException(CORRECT_TIME_ERROR);
        }
    }

    private void checkOnBefore(LocalDate now, CreateClassRequest request) {
        if (now.isAfter(request.getFinishTime().toLocalDate()) || now.isAfter(request.getStartTime().toLocalDate())) {
            throw new CreateClassException(START_FINISH_TIME_ERROR);
        }
    }

    private ClassInfoResponse toClassInfoResponse(ClassEntity classEntity) {
        return ClassInfoResponse.builder()
                .id(classEntity.getId())
                .name(classEntity.getName())
                .startTime(classEntity.getStartTime())
                .finishTime(classEntity.getFinishTime())
                .description(classEntity.getDescription())
                .creator(getUserResponse(classEntity.getCreator()))
                .build();
    }

    private UserResponse getUserResponse(UserEntity creator) {
        return UserResponse.builder()
                .id(creator.getId())
                .name(creator.getFirstName())
                .lastName(creator.getLastName())
                .build();
    }

    private void sendEmailToCreator(ClassEntity classEntity) {
        UserEntity userEntity = userRepository.findById(classEntity.getCreator().getId()).orElseThrow(() -> new UserNotFoundException(CREATOR_DID_NOT_FIND));
        EmailMessage message = EmailMessage.builder()
                .email(userEntity.getEmail())
                .topic(CREATED_CLASS)
                .body(BODY_CREATED_CLASS + classEntity.getName() + WILL_START_AT + classEntity.getStartTime())
                .build();
        rabbitTemplate.convertAndSend(NOTIFICATION_QUEUE, message);
    }

    private void schedulerClassStart(ClassEntity classEntity) {
        JobRequest request = JobRequest.builder()
                .classId(classEntity.getId())
                .userId(classEntity.getCreator().getId())
                .startDate(new Date(classEntity.getStartTime().toInstant().toEpochMilli()))
                .jobType(JobType.START_CLASS)
                .build();
        rabbitTemplate.convertAndSend(SCHEDULER_QUEUE, request);
    }

    private void schedulerEndClass(ClassEntity classEntity) {
        JobRequest request = JobRequest.builder()
                .classId(classEntity.getId())
                .userId(classEntity.getCreator().getId())
                .startDate(new Date(classEntity.getFinishTime().toInstant().toEpochMilli()))
                .jobType(JobType.END_CLASS)
                .build();
        rabbitTemplate.convertAndSend(SCHEDULER_QUEUE, request);
    }
}
