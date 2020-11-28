package com.maltsev.clas.controller;

import com.maltsev.clas.response.SavePhotoResponse;
import com.maltsev.clas.service.ImageService;
import com.maltsev.jwt.Auth;
import com.maltsev.jwt.UserAccount;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static com.maltsev.clas.constants.URIConstants.IMAGE;
import static org.apache.tomcat.util.http.fileupload.FileUploadBase.MULTIPART_FORM_DATA;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

@AllArgsConstructor
@RestController
public class FileUploadController {
    private final ImageService imageService;

    @PostMapping(value = IMAGE, consumes = MULTIPART_FORM_DATA)
    public SavePhotoResponse saveImage(@Auth UserAccount userAccount,
                                       @RequestParam("file") MultipartFile file) {
        return imageService.saveImage(userAccount, file);
    }

    @GetMapping(value = IMAGE, produces = IMAGE_JPEG_VALUE)
    public byte[] getImage(@Auth UserAccount userAccount) {
        return imageService.getImage(userAccount);
    }
}
