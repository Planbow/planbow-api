package com.planbow.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Log4j2
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        authException.printStackTrace();
        log.error("Unauthorized error: {}", authException.getMessage());
        ObjectNode data=getCustomMessage("Unauthorized",HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(data.toString());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    public ObjectNode getCustomMessage(String value,int code,String message){
        ObjectNode rootNode=objectMapper.createObjectNode();
        ObjectNode productNode=objectMapper.createObjectNode();
        productNode.put("name","Planbow");
        productNode.put("version","v1.0");
        ObjectNode statusNode=objectMapper.createObjectNode();
        statusNode.put("code",code);
        statusNode.put("value",value);

        rootNode.set("product",productNode);
        rootNode.set("status",statusNode);
        rootNode.put("data",message);
        rootNode.put("error",true);

        return rootNode;
    }
}
