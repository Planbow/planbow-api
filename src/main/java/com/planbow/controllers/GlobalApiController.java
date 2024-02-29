package com.planbow.controllers;


import com.fasterxml.jackson.databind.node.ObjectNode;
import com.planbow.services.GlobalApiService;
import com.planbow.util.json.handler.request.RequestJsonHandler;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import com.planbow.utility.PlanbowUtility;
import org.apache.http.util.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/global")
@RestController
public class GlobalApiController {

    private GlobalApiService globalApiService;

    @Autowired
    public void setGlobalApiService(GlobalApiService globalApiService) {
        this.globalApiService = globalApiService;
    }

    @PostMapping("/get-organizations")
    public ResponseEntity<ResponseJsonHandler> getOrganization(@RequestBody RequestJsonHandler  requestJsonHandler, @RequestHeader(name="Authorization") String  authorization){
        String userId = PlanbowUtility.getUserId(authorization);
        return globalApiService.getOrganizations(userId);
    }

    @PostMapping("/create-organization")
    public ResponseEntity<ResponseJsonHandler> createOrganization(@RequestBody RequestJsonHandler requestJsonHandler,@RequestHeader(name="Authorization") String authorization){
        String organizationName  = requestJsonHandler.getStringValue("organizationName");
        if(TextUtils.isEmpty(organizationName))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide organizationName");
        String userId = PlanbowUtility.getUserId(authorization);
        return globalApiService.createOrganization(organizationName.trim(),userId);
    }

}
