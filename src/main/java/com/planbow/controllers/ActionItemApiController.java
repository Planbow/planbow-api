package com.planbow.controllers;


import com.planbow.documents.planboard.ActionItems;
import com.planbow.services.ActionItemApiService;
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

import static com.planbow.utility.PlanbowUtility.isInteger;

@RestController
@RequestMapping("/action-items")
public class ActionItemApiController {

    private ActionItemApiService actionItemApiService;


    @Autowired
    public void setActionItemApiService(ActionItemApiService actionItemApiService) {
        this.actionItemApiService = actionItemApiService;
    }

    @PostMapping("/generate-ai-powered-action-items")
    public ResponseEntity<ResponseJsonHandler> generateAiPoweredActionItems(@RequestBody RequestJsonHandler requestJsonHandler){

        return null;
    }

    @PostMapping("/get-action-items")
    public ResponseEntity<ResponseJsonHandler> getActionItems(@RequestBody RequestJsonHandler requestJsonHandler){
        String userId  = requestJsonHandler.getStringValue("userId");
        String nodeId  = requestJsonHandler.getStringValue("nodeId");
        if(StringUtils.isEmpty(nodeId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide nodeId");

        String planboardId  = requestJsonHandler.getStringValue("planboardId");
        if(StringUtils.isEmpty(planboardId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide planboardId");

        return actionItemApiService.getActionItems(userId.trim(),planboardId.trim(),nodeId.trim());
    }

    @PostMapping("/add-action-item")
    public ResponseEntity<ResponseJsonHandler> addActionItem(@RequestBody RequestJsonHandler requestJsonHandler){
        String userId  = requestJsonHandler.getStringValue("userId");
        String nodeId  = requestJsonHandler.getStringValue("nodeId");
        if(StringUtils.isEmpty(nodeId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide nodeId");
        String planboardId  = requestJsonHandler.getStringValue("planboardId");
        if(StringUtils.isEmpty(planboardId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide planboardId");
        String title  = requestJsonHandler.getStringValue("title");
        if(StringUtils.isEmpty(title))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide title");
        String assignedTo  = requestJsonHandler.getStringValue("assignedTo");
        if(!org.apache.commons.lang3.StringUtils.isEmpty(assignedTo)){
            if(!isInteger(assignedTo))
                return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Provided assignedTo must be in number");
        }
        return actionItemApiService.addActionItem(userId.trim(),planboardId.trim(),nodeId.trim(),title.trim(),requestJsonHandler);
    }

    @PostMapping("/update-action-item")
    public ResponseEntity<ResponseJsonHandler> updateActionItem(@RequestBody RequestJsonHandler requestJsonHandler){
        String userId  = requestJsonHandler.getStringValue("userId");
        String actionItemId  = requestJsonHandler.getStringValue("actionItemId");
        if(StringUtils.isEmpty(actionItemId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide actionItemId");

        String priority  = requestJsonHandler.getStringValue("priority");
        if(!StringUtils.isEmpty(priority)){
            if(!priority.equals(ActionItems.PRIORITY_LOW) && !priority.equals(ActionItems.PRIORITY_MEDIUM) && !priority.equals(ActionItems.PRIORITY_HIGH) && ! priority.equals(ActionItems.PRIORITY_CRITICAL))
                return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide correct priority must be low , medium , high and critical only");
        }
        String assignedTo  = requestJsonHandler.getStringValue("assignedTo");
        if(!org.apache.commons.lang3.StringUtils.isEmpty(assignedTo)){
            if(!isInteger(assignedTo))
                return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Provided assignedTo must be in number");
        }
        return actionItemApiService.updateActionItem(userId.trim(),actionItemId.trim(),requestJsonHandler);
    }


    @PostMapping("/delete-action-item")
    public ResponseEntity<ResponseJsonHandler> deleteActionItem(@RequestBody RequestJsonHandler requestJsonHandler){
        String userId  = requestJsonHandler.getStringValue("userId");
        String actionItemId  = requestJsonHandler.getStringValue("actionItemId");
        if(StringUtils.isEmpty(actionItemId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide actionItemId");
        return actionItemApiService.deleteActionItem(userId.trim(),actionItemId.trim());
    }

}
