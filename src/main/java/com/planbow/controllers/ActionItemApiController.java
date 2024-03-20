package com.planbow.controllers;


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
}
