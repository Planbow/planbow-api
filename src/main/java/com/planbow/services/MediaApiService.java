package com.planbow.services;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.planbow.documents.media.Media;
import com.planbow.documents.users.User;
import com.planbow.repository.MediaApiRepository;
import com.planbow.repository.UserApiRepository;
import com.planbow.security.services.UserDetailsImpl;
import com.planbow.util.json.handler.request.RequestJsonHandler;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import com.planbow.utility.RandomzUtility;
import com.planbow.utility.ResponseConstant;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static com.planbow.utility.RandomzUtility.DIRECTORY_USERS;

@Service
@Log4j2
@Transactional
public class MediaApiService {


    private FileStorageServices fileStorageServices;
    private ObjectMapper objectMapper;
    private MediaApiRepository mediaApiRepository;
    private UserApiRepository userApiRepository;

    @Autowired
    public void setUserApiRepository(UserApiRepository userApiRepository) {
        this.userApiRepository = userApiRepository;
    }

    @Autowired
    public void setMediaApiRepository(MediaApiRepository mediaApiRepository) {
        this.mediaApiRepository = mediaApiRepository;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setFileStorageServices(FileStorageServices fileStorageServices) {
        this.fileStorageServices = fileStorageServices;
    }


    public ResponseJsonHandler uploadProfile(RequestJsonHandler requestJsonHandler, MultipartFile filePart) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();

        User users = userApiRepository.getUserById(userId);
        if(users==null){
            return ResponseJsonUtil.getResponse("Provided userId does not exists",404,"Not Found",true);
        }

        String type  = requestJsonHandler.getStringValue("type");
        if(type.equals(RandomzUtility.MEDIA_IMAGE)){
            Media media = new Media();
            media.setMediaType(type);
            media.setUserId(userId);
            media.setCreatedOn(Instant.now());
            media.setModifiedOn(Instant.now());
            media.setActive(true);

            if(filePart!=null){
                String folder=DIRECTORY_USERS+ File.separator+ userId + File.separator+ type;
                String mediaUrl=fileStorageServices.uploadFile(folder, RandomzUtility._defaultFileName(RandomzUtility.MEDIA_IMAGE),filePart);
                if(mediaUrl!=null){
                    media.setMediaUrl(mediaUrl);
                }
            }
            media  = mediaApiRepository.saveOrUpdateMedia(media);
            log.info("Returning response from uploadMedia() method : {}",media);
            return ResponseJsonUtil.getResponse(objectMapper.valueToTree(media),200, ResponseConstant.SUCCESS.getStatus(),false);
        }else{

            File fileName=new File("IMG_"+System.currentTimeMillis()+".png");
            Media media = new Media();
            media.setMediaType(type);
            media.setUserId(userId);
            media.setCreatedOn(Instant.now());
            media.setModifiedOn(Instant.now());
            media.setActive(true);

            if(filePart!=null){

                File videoFile = convertMultipartFileToFile(filePart);


                String folder=DIRECTORY_USERS+ File.separator+ userId + File.separator+ type;
                String mediaUrl=fileStorageServices.uploadFile(folder, RandomzUtility._defaultFileName(RandomzUtility.MEDIA_VIDEO),filePart);
                String thumbnailUrl=fileStorageServices.uploadFile(folder, RandomzUtility._defaultFileName(RandomzUtility.MEDIA_IMAGE),fileName);
                if(mediaUrl!=null){
                    media.setMediaUrl(mediaUrl);
                }
                if(thumbnailUrl!=null){
                    media.setThumbnailUrl(thumbnailUrl);
                }
            }
            media  = mediaApiRepository.saveOrUpdateMedia(media);
            log.info("Returning response from uploadMedia() method : {}",media);
            return ResponseJsonUtil.getResponse(objectMapper.valueToTree(media),200, ResponseConstant.SUCCESS.getStatus(),false);
        }
    }

    public ResponseJsonHandler getMedias(RequestJsonHandler requestJsonHandler) {
        String userId  = requestJsonHandler.getStringValue("userId");
        if(userId==null){
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        }

        User users = userApiRepository.getUserById(userId);
        if(users==null){
            return ResponseJsonUtil.getResponse("Provided userId does not exists",404,"Not Found",true);
        }
        List<Media> medias  = mediaApiRepository.getMedias("all",userId);
        ObjectNode node  = objectMapper.createObjectNode();
        node.set("medias",objectMapper.valueToTree(medias));
        node.set("user",objectMapper.valueToTree(users));
        return ResponseJsonUtil.getResponse(objectMapper.valueToTree(node),200, ResponseConstant.SUCCESS.getStatus(),false);
    }


    public  File convertMultipartFileToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        convFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

}
