package com.planbow.services;

import com.planbow.repository.FileStorageRepository;
import com.planbow.utility.ResponseConstant;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.planbow.util.json.handler.request.RequestJsonHandler;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.util.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class FileStorageServices {

    private FileStorageRepository fileStorageRepository;

    @Autowired
    public void setFileStorageRepository(FileStorageRepository fileStorageRepository) {
        this.fileStorageRepository = fileStorageRepository;
    }

    private ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ResponseJsonHandler uploadFile(RequestJsonHandler requestJsonHandler, MultipartFile multipartFile){
        String folder  = requestJsonHandler.getStringValue("folder");
        String fileName  = requestJsonHandler.getStringValue("fileName");
        if(fileName==null || TextUtils.isEmpty(fileName))
            fileName  =multipartFile.getOriginalFilename();
        else{
            fileName=FilenameUtils.removeExtension(fileName);
            String extension = FilenameUtils.getExtension(multipartFile.getOriginalFilename());
            fileName=fileName+"."+extension;
        }
        String callbackUrl=null;
        try {
            callbackUrl  = fileStorageRepository.saveFile(multipartFile,fileName,folder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(callbackUrl==null)
            return ResponseJsonUtil.getResponse(
                    ResponseConstant.ERROR_IN_PROCESSING_REQUEST.getStatus(),500,"Internal Server Error",true
            );

        ObjectNode dataNode  = objectMapper.createObjectNode();
        dataNode.put("downloadUrl",callbackUrl);
        return ResponseJsonUtil.getResponse(
                dataNode,200,ResponseConstant.SUCCESS.getStatus(),false
        );
    }

    public String uploadFile(String folder,String fileName, MultipartFile multipartFile){
        if(fileName==null || TextUtils.isEmpty(fileName))
            fileName  =multipartFile.getOriginalFilename();
        else{
            fileName=FilenameUtils.removeExtension(fileName);
            String extension = FilenameUtils.getExtension(multipartFile.getOriginalFilename());
            fileName=fileName+"."+extension;
        }
        String callbackUrl=null;
        try {
            callbackUrl  = fileStorageRepository.saveFile(multipartFile,fileName,folder);
            File  file  = new File(multipartFile.getOriginalFilename());
            file.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(callbackUrl!=null)
            return callbackUrl;
        return null;

    }

    public String uploadFile(String folder,String fileName, File file){
        if(fileName==null || TextUtils.isEmpty(fileName))
            fileName  =file.getName();
        else{
            fileName=FilenameUtils.removeExtension(fileName);
            String extension = FilenameUtils.getExtension(file.getName());
            fileName=fileName+"."+extension;
        }
        String callbackUrl=null;
        try {
            callbackUrl  = fileStorageRepository.saveFile(file,fileName,folder);
            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(callbackUrl!=null)
            return callbackUrl;

        return null;

    }


    public ResponseJsonHandler uploadFiles(RequestJsonHandler requestJsonHandler, MultipartFile[] multipartFiles){
        String folder  = requestJsonHandler.getStringValue("folder");
        ArrayNode callbackUrls  =objectMapper.createArrayNode();

        if(multipartFiles.length>=1){

            for (MultipartFile multipartFile : multipartFiles) {
                try {
                    String fileName  =FilenameUtils.removeExtension(multipartFile.getOriginalFilename());
                    callbackUrls.add(fileStorageRepository.saveFile(multipartFile,fileName,folder));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }


        if(callbackUrls.size()==0)
            return ResponseJsonUtil.getResponse(
                    ResponseConstant.ERROR_IN_PROCESSING_REQUEST.getStatus(),500,"Internal Server Error",true
            );

        ObjectNode dataNode  = objectMapper.createObjectNode();
        dataNode.put("downloadUrls",callbackUrls);
        return ResponseJsonUtil.getResponse(
                dataNode,200,ResponseConstant.SUCCESS.getStatus(),false
        );
    }


    public ResponseJsonHandler deleteFiles(List<String> urls){
        if(urls!=null){
            if(!urls.isEmpty()){
                urls.forEach(e->{
                    try {
                        fileStorageRepository.deleteFile(e);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                });
            }
        }
        return ResponseJsonUtil.getResponse(
                ResponseConstant.SUCCESS.getStatus(),200,ResponseConstant.SUCCESS.getStatus(),false
        );
    }


    public File convertFromMultiPartToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(multipartFile.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(multipartFile.getBytes());
        fos.close();
        return file;
    }
}
