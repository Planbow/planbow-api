package com.planbow.controllers;


import com.planbow.services.UserApiService;
import com.planbow.utility.ResponseConstant;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import com.planbow.util.json.handler.request.RequestJsonHandler;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseConstants;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@Log4j2
@RequestMapping("/users")
@SuppressWarnings({"ALL"})
public class UserApiController {

    private UserApiService userApiService;
    private ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setUserApiService(UserApiService userApiService) {
        this.userApiService = userApiService;
    }

    @PostMapping("/get-user")
    public ResponseJsonHandler getUser(@RequestBody RequestJsonHandler requestJsonHandler){
        String userId  = requestJsonHandler.getStringValue("userId");
        if (StringUtils.isEmpty(userId))
            return ResponseJsonUtil.getResponse("Please provide userId", 400, ResponseConstants.BAD_REQUEST.getStatus(), true);
        return userApiService.getUser(userId.trim());
    }


    @PostMapping("/update-location")
    public ResponseJsonHandler updateLocation(@RequestBody RequestJsonHandler requestJsonHandler){
        Double latitude  = requestJsonHandler.getDoubleValue("latitude");
        if(latitude==null){
            return ResponseJsonUtil.getResponse("Please provide latitude", 400, ResponseConstants.BAD_REQUEST.getStatus(), true);
        }

        Double longitude  = requestJsonHandler.getDoubleValue("longitude");
        if(longitude==null){
            return ResponseJsonUtil.getResponse("Please provide longitude", 400, ResponseConstants.BAD_REQUEST.getStatus(), true);
        }
        return userApiService.updateLocation(latitude,longitude);
    }

    @PostMapping("/get-users")
    public ResponseJsonHandler getUsers(@RequestBody RequestJsonHandler requestJsonHandler){
        Double distance  = requestJsonHandler.getDoubleValue("distance");
        if(distance==null)
            return ResponseJsonUtil.getResponse("Please provide distance", 400, ResponseConstants.BAD_REQUEST.getStatus(), true);

        Double latitude  = requestJsonHandler.getDoubleValue("latitude");
        Double longitude  = requestJsonHandler.getDoubleValue("longitude");
        return userApiService.getUsers(distance,latitude,longitude);
    }

    @PostMapping("/update-user")
    public ResponseJsonHandler updateUser(@RequestBody RequestJsonHandler requestJsonHandler){
        return userApiService.updateUser(requestJsonHandler);
    }

    @PostMapping("/change-password")
    public ResponseJsonHandler changePassword(@RequestBody  RequestJsonHandler requestJsonHandler){

        String oldPassword = requestJsonHandler.getStringValue("oldPassword");
        if(StringUtils.isEmpty(oldPassword))
            return ResponseJsonUtil.getResponse(
                    "Please provide old password",400,"Bad Request",true
            );
        else{
            if(oldPassword.length()<6){
                return ResponseJsonUtil.getResponse(
                        "old password must be of at least 6 characters",400,"Bad Request",true
                );
            }
        }
        String newPassword = requestJsonHandler.getStringValue("newPassword");
        if(StringUtils.isEmpty(newPassword))
            return ResponseJsonUtil.getResponse(
                    "Please provide new password",400,"Bad Request",true
            );
        else{
            if(newPassword.length()<6){
                return ResponseJsonUtil.getResponse(
                        "new password must be of at least 6 characters",400,"Bad Request",true
                );
            }
        }
        return userApiService.changePassword(oldPassword.trim(),newPassword.trim());
    }


    @PostMapping("/upload-profile")
    public ResponseJsonHandler uploadProfile(@RequestPart("data") String data,
                                             @RequestPart(value ="file") MultipartFile filePart){
        RequestJsonHandler requestJsonHandler=null;
        try {
            requestJsonHandler=objectMapper.readValue(data,RequestJsonHandler.class);
            return  userApiService.uploadProfile(requestJsonHandler,filePart);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseJsonUtil.getResponse(
                ResponseConstant.ERROR_IN_PROCESSING_REQUEST.getStatus(),500,"Internal Server Error",true
        );
    }

}
