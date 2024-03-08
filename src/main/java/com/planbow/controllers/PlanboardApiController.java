package com.planbow.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.planbow.documents.planboard.Members;
import com.planbow.services.PlanboardApiService;
import com.planbow.util.json.handler.request.RequestJsonHandler;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import com.planbow.utility.TokenUtility;
import io.micrometer.common.util.StringUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RequestMapping("/planboard")
@RestController
@Log4j2
public class PlanboardApiController {

    private PlanboardApiService planboardApiService;
    private ObjectMapper objectMapper;


    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

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
    public ResponseEntity<ResponseJsonHandler> createPlanboard(
            @RequestHeader HttpHeaders headers, @RequestPart("data") String data,
            @RequestParam(value ="files", required=false) MultipartFile[] multipartFiles){

        String userId = TokenUtility.getUserId(headers);
        RequestJsonHandler requestJsonHandler=null;
        try {
            requestJsonHandler=objectMapper.readValue(data,RequestJsonHandler.class);
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
            String endDate = requestJsonHandler.getStringValue("endDate");
            String remark = requestJsonHandler.getStringValue("remark");
            boolean markAsDefault = requestJsonHandler.getBooleanValue("markAsDefault");
            List<Members> members = (List<Members>) requestJsonHandler.getListValues("members", Members.class);
            return planboardApiService.createPlanboard(userId,planboardId.trim(),workspaceId.trim(),domainId.trim(),subdomainId.trim(),markAsDefault,name,description,scope,geography,endDate,members,remark,multipartFiles);
            } catch (IOException e) {
                log.error("Exception occurred in /create-planboard : {}",e.getMessage());
            }
        return ResponseJsonUtil.getResponse(HttpStatus.INTERNAL_SERVER_ERROR,"Internal Server Error");
    }


    @PostMapping("/planboard-summary")
    public ResponseEntity<ResponseJsonHandler> planboardSummary(@RequestBody RequestJsonHandler requestJsonHandler){
        String userId = requestJsonHandler.getStringValue("userId");
        String planboardId = requestJsonHandler.getStringValue("planboardId");
        if(StringUtils.isEmpty(planboardId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide planboardId");
        return planboardApiService.planboardSummary(userId.trim(),planboardId.trim());

    }

}
