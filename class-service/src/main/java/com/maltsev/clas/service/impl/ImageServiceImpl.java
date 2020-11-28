package com.maltsev.clas.service.impl;

import com.maltsev.clas.entity.Image;
import com.maltsev.clas.exception.SavePhotoException;
import com.maltsev.clas.exception.UserNotFoundException;
import com.maltsev.clas.repository.ImageRepository;
import com.maltsev.clas.response.SavePhotoResponse;
import com.maltsev.clas.service.ImageService;
import com.maltsev.jwt.UserAccount;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.maltsev.clas.constants.MessageConstants.*;

@Service
@AllArgsConstructor
public class ImageServiceImpl implements ImageService {
    private final ImageRepository imageRepository;

    @Override
    public SavePhotoResponse saveImage(UserAccount userAccount, MultipartFile file) {
        if (file.isEmpty()) {
            throw new SavePhotoException(PHOTO_DOES_NOT_EXIST);
        }
        if (file.getSize() > PHOTO_BIG_SIZE_INT) {
            throw new SavePhotoException(PHOTO_BIG_SIZE);
        }
        try {
            byte[] bytes = file.getBytes();
            Image image = new Image(userAccount.getUserId(), bytes);
            imageRepository.save(image);
            return new SavePhotoResponse(PHOTO_UPLOADED);
        } catch (IOException e) {
            throw new SavePhotoException(PHOTO_DOES_NOT_UPLOAD);
        }
    }

    @Override
    public byte[] getImage(UserAccount userAccount) {
        Image image = imageRepository.findByUserId(userAccount.getUserId()).orElseThrow(() -> new UserNotFoundException(PHOTO_DID_NOT_FIND));
        return image.getImage();
    }
}
