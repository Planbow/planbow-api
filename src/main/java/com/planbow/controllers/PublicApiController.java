package com.planbow.controllers;

import com.planbow.services.PublicApiService;
import com.planbow.util.json.handler.request.RequestJsonHandler;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseConstants;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import com.planbow.util.utility.core.Utility;
import com.planbow.utility.RandomzUtility;
import com.planbow.utility.ResponseConstant;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

@Log4j2
@RestController
@RequestMapping("/public")
@SuppressWarnings({"ALL"})
public class PublicApiController {

    private PublicApiService publicApiService;

    @Autowired
    public void setPublicApiService(PublicApiService publicApiService) {
        this.publicApiService = publicApiService;
    }


    @GetMapping("/test")
    public String test(){
        return "test";
    }


    @PostMapping("/create-account")
    public ResponseEntity<ResponseJsonHandler> createAccount(@RequestBody RequestJsonHandler requestJsonHandler){

        String email  = requestJsonHandler.getStringValue("email");
        if(StringUtils.isEmpty(email))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide email");
        else {
            Pattern pattern = Pattern.compile(Utility.EMAIL_PATTERN);
            if(!pattern.matcher(email).find())
                return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,ResponseConstant.INVALID_EMAIL.getStatus());
        }

        String name  = requestJsonHandler.getStringValue("name");
        if(StringUtils.isEmpty(name))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide name");

        String contactNo  = requestJsonHandler.getStringValue("contactNo");
        if(StringUtils.isEmpty(contactNo))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide contactNo");

        String password = requestJsonHandler.getStringValue("password");
        if(StringUtils.isEmpty(password))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide password");
        else{
            if(password.length()<6)
                return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Password must be of at least 6 characters");
        }
        String gender  = requestJsonHandler.getStringValue("gender");
        if(StringUtils.isEmpty(gender))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide gender");
        else if(!gender.equals("male") && !gender.equals("female")){
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Invalid gender ,must be male or female");
        }
        return publicApiService.createAccount(email.trim(),name.trim(),contactNo.trim(),gender.trim(),password.trim(),requestJsonHandler);

    }


    @PostMapping("/authenticate-user")
    public ResponseEntity<ResponseJsonHandler> authenticateUser(@RequestBody RequestJsonHandler requestJsonHandler) {
        log.info("Executing endpoint /authenticate-user for payload : {}",requestJsonHandler);

        String email  = requestJsonHandler.getStringValue("email");
        if(StringUtils.isEmpty(email))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide email");
        else {
            Pattern pattern = Pattern.compile(Utility.EMAIL_PATTERN);
            if(!pattern.matcher(email).find())
                return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,ResponseConstant.INVALID_EMAIL.getStatus());
        }
        String password = requestJsonHandler.getStringValue("password");
        if(StringUtils.isEmpty(password))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide password");
        else{
            if(password.length()<6)
                return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Password must be of at least 6 characters");
        }
        return publicApiService.authenticateUser(email.trim(), password.trim(),requestJsonHandler);
    }


    @PostMapping("/refresh-token")
    public ResponseEntity<ResponseJsonHandler> refreshToken(@RequestBody RequestJsonHandler requestJsonHandler) {
        log.info("Executing endpoint /refresh-token for payload : {}",requestJsonHandler);
        String refreshToken = requestJsonHandler.getStringValue("refreshToken");
        if (StringUtils.isEmpty(refreshToken))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide refreshToken");

        return publicApiService.refreshToken(refreshToken);
    }


    @PostMapping("/authenticate-with-social-account")
    public ResponseJsonHandler authenticateWithSocialAccount(@RequestBody RequestJsonHandler requestJsonHandler){
        String email  = requestJsonHandler.getStringValue("email");
        if(StringUtils.isEmpty(email))
            return ResponseJsonUtil.getResponse("Please provide email",400,"Bad Request",true);
        String deviceId  = requestJsonHandler.getStringValue("deviceId");
        return publicApiService.authenticateWithSocialAccount(email,deviceId ,requestJsonHandler);
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<ResponseJsonHandler> forgotPassword(@RequestBody  RequestJsonHandler requestJsonHandler){
        String email  = requestJsonHandler.getStringValue("email");
        if(StringUtils.isEmpty(email))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,
                    "Please provide email");
        else {
            Pattern pattern = Pattern.compile(Utility.EMAIL_PATTERN);
            if(!pattern.matcher(email).find())
                return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,ResponseConstant.INVALID_EMAIL.getStatus());
        }
        return publicApiService.forgotPassword(email.trim());
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ResponseJsonHandler> verifyOtp(@RequestBody RequestJsonHandler requestJsonHandler){
        String email  = requestJsonHandler.getStringValue("email");
        if(StringUtils.isEmpty(email))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,
                    "Please provide email");
        else {
            Pattern pattern = Pattern.compile(Utility.EMAIL_PATTERN);
            if(!pattern.matcher(email).find())
                return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,ResponseConstant.INVALID_EMAIL.getStatus());
        }
        Integer otp  = requestJsonHandler.getIntegerValue("otp");
        if(otp==null)
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,
                    "Please provide otp");
        else if (String.valueOf(otp).length()!=6)
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,
                    "OTP must be of 6 digit only");

        return publicApiService.verifyOtp(email.trim(),otp);
    }

    @PostMapping("/set-password")
    public ResponseEntity<ResponseJsonHandler> setPassword(@RequestBody  RequestJsonHandler requestJsonHandler){
        String userId  = requestJsonHandler.getStringValue("userId");
        if(StringUtils.isEmpty(userId))
            return ResponseJsonUtil.getResponse(
                    HttpStatus.BAD_REQUEST,"Please provide userId");
        String password = requestJsonHandler.getStringValue("password");
        if(StringUtils.isEmpty(password))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,
                    "Please provide new password");
        else{
            if(password.length()<6){
                return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,
                        "new password must be of at least 6 characters");
            }
        }
        return publicApiService.setPassword(userId.trim(),password.trim());
    }




}
