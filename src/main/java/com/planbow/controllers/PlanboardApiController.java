package com.planbow.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static com.planbow.utility.PlanbowUtility.*;

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
            if(!CollectionUtils.isEmpty(members)){
                if(!validateMemberAndRoles(members)){
                    return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Invalid member object , emailId or userId is required and role must be Creator , Contributor and Viewer");
                }
            }
            ObjectNode schedule = (ObjectNode) requestJsonHandler.getObjectValue("schedule",ObjectNode.class);
            if(schedule!=null){
                if(!schedule.has("meetingTypeId")){
                    return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide meetingTypeId in schedule node");
                }
                if(!schedule.has("date")){
                    return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide date in schedule node");
                }
                if(!schedule.has("start") && !schedule.has("end")){
                    return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide start & end time in schedule node");
                }
            }
            return planboardApiService.createPlanboard(userId,planboardId.trim(),workspaceId.trim(),domainId.trim(),subdomainId.trim(),markAsDefault,name,description,scope,geography,endDate,members,remark,schedule,multipartFiles);
            } catch (IOException e) {
                log.error("Exception occurred in /create-planboard : {}",e.getMessage());
            }
        return ResponseJsonUtil.getResponse(HttpStatus.INTERNAL_SERVER_ERROR,"Internal Server Error");
    }


    @PostMapping("/get-strategic-nodes")
    public ResponseEntity<ResponseJsonHandler> getStrategicNodes(@RequestBody RequestJsonHandler requestJsonHandler){
        String planboardId  = requestJsonHandler.getStringValue("planboardId");
        if(StringUtils.isEmpty(planboardId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide planboardId");
        return planboardApiService.getStrategicNodes(planboardId);
    }



    @PostMapping("/planboard-summary")
    public ResponseEntity<ResponseJsonHandler> planboardSummary(@RequestBody RequestJsonHandler requestJsonHandler){
        String userId = requestJsonHandler.getStringValue("userId");
        String planboardId = requestJsonHandler.getStringValue("planboardId");
        if(StringUtils.isEmpty(planboardId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide planboardId");
        return planboardApiService.planboardSummary(userId,planboardId.trim());
    }

    @PostMapping("/get-planboard-nodes")
    public ResponseEntity<ResponseJsonHandler> getPlanboardNodes(@RequestBody RequestJsonHandler requestJsonHandler){
        String userId  = requestJsonHandler.getStringValue("userId");
        String planboardId  = requestJsonHandler.getStringValue("planboardId");
        if(StringUtils.isEmpty(planboardId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide planboardId");
        return planboardApiService.getPlanboardNodes(planboardId,userId);
    }

    @PostMapping("/update-planboard")
    public ResponseEntity<ResponseJsonHandler> updatePlanboard(@RequestBody RequestJsonHandler requestJsonHandler){
        String userId  = requestJsonHandler.getStringValue("userId");
        String planboardId  = requestJsonHandler.getStringValue("planboardId");
        if(StringUtils.isEmpty(planboardId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide planboardId");
        return planboardApiService.updatePlanboard(planboardId,userId,requestJsonHandler);
    }

    @PostMapping("/remove-member")
    public ResponseEntity<ResponseJsonHandler> removeMember(@RequestBody RequestJsonHandler requestJsonHandler){
        String userId  = requestJsonHandler.getStringValue("userId");
        String planboardId  = requestJsonHandler.getStringValue("planboardId");
        if(StringUtils.isEmpty(planboardId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide planboardId");
        String memberId  = requestJsonHandler.getStringValue("memberId");
        if(StringUtils.isEmpty(memberId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide memberId");
        else{
            if(!isInteger(memberId) && !isValidEmail(memberId)){
                return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Invalid memberId provided");
            }
        }
        return planboardApiService.removeMember(planboardId,userId,memberId);
    }

    @PostMapping("/add-member")
    public ResponseEntity<ResponseJsonHandler> addMember(@RequestBody RequestJsonHandler requestJsonHandler){
        String userId  = requestJsonHandler.getStringValue("userId");
        String planboardId  = requestJsonHandler.getStringValue("planboardId");
        if(StringUtils.isEmpty(planboardId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide planboardId");

        Members member = (Members) requestJsonHandler.getObjectValue("member", Members.class);
        if(member==null){
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide member node");
        }else{
            if(!validateMemberAndRoles(List.of(member))){
                return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Invalid member object , emailId or userId is required and role must be Creator , Contributor and Viewer");
            }
        }
        return planboardApiService.addMember(planboardId.trim(),userId,member);
    }

    @PostMapping("/update-role")
    public ResponseEntity<ResponseJsonHandler> updateRole(@RequestBody RequestJsonHandler requestJsonHandler){
        String userId  = requestJsonHandler.getStringValue("userId");
        String planboardId  = requestJsonHandler.getStringValue("planboardId");
        if(StringUtils.isEmpty(planboardId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide planboardId");

        String role  = requestJsonHandler.getStringValue("role");
        if(StringUtils.isEmpty(role))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide role");
        else{
            if(!isValidRole(role))
                return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide valid role");
        }

        String memberId  = requestJsonHandler.getStringValue("memberId");
        if(StringUtils.isEmpty(memberId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide memberId");
        else{
            if(!isInteger(memberId) && !isValidEmail(memberId)){
                return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Invalid memberId provided");
            }
        }
        return planboardApiService.updateRole(planboardId.trim(),userId.trim(),memberId.trim(),role.trim());
    }

    @PostMapping("/remove-attachment")
    public ResponseEntity<ResponseJsonHandler> removeAttachment(@RequestBody RequestJsonHandler requestJsonHandler){
        String userId  = requestJsonHandler.getStringValue("userId");
        String planboardId  = requestJsonHandler.getStringValue("planboardId");
        if(StringUtils.isEmpty(planboardId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide planboardId");
        String attachmentId  = requestJsonHandler.getStringValue("attachmentId");
        if(StringUtils.isEmpty(attachmentId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide attachmentId");
        return planboardApiService.removeAttachment(planboardId.trim(),attachmentId.trim(),userId);
    }

    @PostMapping("/add-attachment")
    public ResponseEntity<ResponseJsonHandler> addAttachment(
            @RequestHeader HttpHeaders headers, @RequestPart("data") String data,
            @RequestParam(value ="files", required=false) MultipartFile[] multipartFiles){

        String userId = TokenUtility.getUserId(headers);
        RequestJsonHandler requestJsonHandler=null;
        try {
            requestJsonHandler=objectMapper.readValue(data,RequestJsonHandler.class);
            String planboardId  = requestJsonHandler.getStringValue("planboardId");
            if(StringUtils.isEmpty(planboardId))
                return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide planboardId");
            return planboardApiService.addAttachment(userId,planboardId.trim(),multipartFiles);
        } catch (IOException e) {
            log.error("Exception occurred in /add-attachment : {}",e.getMessage());
        }
        return ResponseJsonUtil.getResponse(HttpStatus.INTERNAL_SERVER_ERROR,"Internal Server Error");
    }

}
