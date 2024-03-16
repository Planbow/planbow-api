package com.planbow.controllers;


import com.planbow.documents.planboard.NodeMetaData;
import com.planbow.services.NodeApiService;
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
@RequestMapping("/nodes")
public class NodeApiController {

    private NodeApiService nodeApiService;

    @Autowired
    public void setNodeApiService(NodeApiService nodeApiService) {
        this.nodeApiService = nodeApiService;
    }


    @PostMapping("/add-node")
    public ResponseEntity<ResponseJsonHandler> addNode(@RequestBody RequestJsonHandler requestJsonHandler){
        String userId  = requestJsonHandler.getStringValue("userId");
        String planboardId  = requestJsonHandler.getStringValue("planboardId");
        if(StringUtils.isEmpty(planboardId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide planboardId");
        String title  = requestJsonHandler.getStringValue("title");
        if(StringUtils.isEmpty(title))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide title");
        String parentId  = requestJsonHandler.getStringValue("parentId");
        String description  = requestJsonHandler.getStringValue("description");
        NodeMetaData nodeMetaData  = (NodeMetaData) requestJsonHandler.getObjectValue("metaData", NodeMetaData.class);
        return nodeApiService.addNode(userId.trim(),planboardId.trim(),parentId,title.trim(),description,nodeMetaData);
    }


}
