package com.planbow.controllers;


import com.planbow.services.AdminApiService;
import com.planbow.util.json.handler.request.RequestJsonHandler;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RequestMapping("/admin")
@RestController
public class AdminApiController {

    private AdminApiService adminApiService;


    @Autowired
    public void setAdminApiService(AdminApiService adminApiService) {
        this.adminApiService = adminApiService;
    }

    @PostMapping("/add-domain")
    public ResponseEntity<ResponseJsonHandler> addDomain(@RequestBody RequestJsonHandler requestJsonHandler){
        String name  = requestJsonHandler.getStringValue("name");
        if(StringUtils.isEmpty(name))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide domain name");
        String description  = requestJsonHandler.getStringValue("description");
        String userId  = requestJsonHandler.getStringValue("userId");
        return adminApiService.addDomain(name.trim(),description,userId);
    }

    @PostMapping("/add-subdomain")
    public ResponseEntity<ResponseJsonHandler> addSubDomain(@RequestBody RequestJsonHandler requestJsonHandler){
        String domainId  = requestJsonHandler.getStringValue("domainId");
        if(StringUtils.isEmpty(domainId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide domainId");
        String name  = requestJsonHandler.getStringValue("name");
        if(StringUtils.isEmpty(name))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide subdomain name");
        String description  = requestJsonHandler.getStringValue("description");
        String userId  = requestJsonHandler.getStringValue("userId");
        return adminApiService.addSubDomain(domainId.trim(),name,description,userId);
    }
}
