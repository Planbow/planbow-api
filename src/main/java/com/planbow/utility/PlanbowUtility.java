package com.planbow.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.common.util.StringUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;

import java.util.Base64;


@Log4j2
public class PlanbowUtility {



    public static String getUserId(String authorization){
        ObjectNode node  = decodeJwtToken(authorization);
        if(node!=null){
            return node.get("userId").asText();
        }
        return null;
    }

    public static ObjectNode decodeJwtToken(String authorization){
        if(authorization!=null){
            String token =removeBearerPrefix(authorization);
            String[] chunks = token.split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();

            String header = new String(decoder.decode(chunks[0]));
            String payload = new String(decoder.decode(chunks[1]));
            ObjectMapper objectMapper  = new ObjectMapper();
            try {
                return objectMapper.readValue(payload, ObjectNode.class);
            } catch (JsonProcessingException e) {
                log.error("Exception occurred in decodeJwtToken() : {}",e.getMessage());
            }
            return null;
        }
        return null;

    }

    public static String removeBearerPrefix(String token) {
        if (StringUtils.isEmpty(token)) {
            return token;
        }
        if (token.trim().toLowerCase().startsWith("bearer ")) {
            return token.substring("Bearer ".length()).trim();
        }
        return token;
    }

}
