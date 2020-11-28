package com.maltsev.clas.service;

import com.maltsev.clas.response.SavePhotoResponse;
import com.maltsev.jwt.UserAccount;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    SavePhotoResponse saveImage(UserAccount userAccount, MultipartFile file);

    byte[] getImage(UserAccount userAccount);
}
