package com.planbow.controllers;


import com.planbow.services.PlanboardApiService;
import com.planbow.util.json.handler.request.RequestJsonHandler;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/planboard")
@RestController
public class PlanboardApiController {

    private PlanboardApiService planboardApiService;

    @Autowired
    public void setPlanboardApiService(PlanboardApiService planboardApiService) {
        this.planboardApiService = planboardApiService;
    }


    @PostMapping("/validate-prompt")
    public ResponseEntity<ResponseJsonHandler> validatePrompt(@RequestBody RequestJsonHandler requestJsonHandler){
        String domainId = requestJsonHandler.getStringValue("domainId");
        if(StringUtils.isEmpty(domainId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide domainId");

        String subdomainId = requestJsonHandler.getStringValue("subdomainId");
        if(StringUtils.isEmpty(subdomainId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide subdomainId");

        String scope = requestJsonHandler.getStringValue("scope");
        String geography = requestJsonHandler.getStringValue("geography");
        String userId = requestJsonHandler.getStringValue("userId");
        return planboardApiService.validatePrompt(domainId.trim(),subdomainId.trim(),scope,geography,userId);
    }


    @PostMapping("/create-planboard")
    public ResponseEntity<ResponseJsonHandler> createPlanboard(@RequestBody RequestJsonHandler requestJsonHandler){
        String userId = requestJsonHandler.getStringValue("userId");
        String planboardId  = requestJsonHandler.getStringValue("planboardId");
        if(StringUtils.isEmpty(planboardId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide planboardId");

        String workspaceId  = requestJsonHandler.getStringValue("workspaceId");
        if(StringUtils.isEmpty(workspaceId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide workspaceId");

        String name  = requestJsonHandler.getStringValue("name");
        if(StringUtils.isEmpty(name))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide name");

        String description = requestJsonHandler.getStringValue("description");

        String domainId = requestJsonHandler.getStringValue("domainId");
        if(StringUtils.isEmpty(domainId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide domainId");

        String subdomainId = requestJsonHandler.getStringValue("subdomainId");
        if(StringUtils.isEmpty(subdomainId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide subdomainId");

        String scope = requestJsonHandler.getStringValue("scope");
        String geography = requestJsonHandler.getStringValue("geography");


        return planboardApiService.validatePrompt(domainId.trim(),subdomainId.trim(),scope,geography,userId);
    }


}
