package com.planbow.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.planbow.services.MediaApiService;
import com.planbow.util.json.handler.request.RequestJsonHandler;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import com.planbow.utility.ResponseConstant;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/media")
@Log4j2
public class MediaApiController {
    private ObjectMapper objectMapper;
    private MediaApiService mediaApiService;

    @Autowired
    public void setMediaApiService(MediaApiService mediaApiService) {
        this.mediaApiService = mediaApiService;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostMapping("/upload-media")
    public ResponseJsonHandler uploadMedia(@RequestPart("data") String data,
                                           @RequestPart(value ="file") MultipartFile filePart){
        log.info("Request received at /upload-media endpoint ");
        RequestJsonHandler requestJsonHandler=null;
        try {
            requestJsonHandler=objectMapper.readValue(data,RequestJsonHandler.class);
            return  mediaApiService.uploadProfile(requestJsonHandler,filePart);

        } catch (IOException e) {
            log.info("Exception occurred at /upload-media endpoint  {}",e.getMessage());
            e.printStackTrace();
        }
        return ResponseJsonUtil.getResponse(
                ResponseConstant.ERROR_IN_PROCESSING_REQUEST.getStatus(),500,"Internal Server Error",true
        );
    }


    @PostMapping("/get-medias")
    public ResponseJsonHandler getMedias(@RequestBody RequestJsonHandler requestJsonHandler){
        return mediaApiService.getMedias(requestJsonHandler);
    }

}
