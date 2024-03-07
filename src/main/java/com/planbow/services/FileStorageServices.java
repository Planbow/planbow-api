package com.planbow.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.planbow.repository.FileStorageRepository;
import com.planbow.util.json.handler.request.RequestJsonHandler;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public ResponseEntity<ResponseJsonHandler> uploadFile(RequestJsonHandler requestJsonHandler, MultipartFile multipartFile){
        String folder  = requestJsonHandler.getStringValue("folder");
        String fileName  = requestJsonHandler.getStringValue("fileName");
        if(fileName==null || StringUtils.isEmpty(fileName))
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
            return ResponseJsonUtil.getResponse(HttpStatus.INTERNAL_SERVER_ERROR,"Internal Server Error"
            );

        ObjectNode dataNode  = objectMapper.createObjectNode();
        dataNode.put("downloadUrl",callbackUrl);
        return ResponseJsonUtil.getResponse(HttpStatus.OK,dataNode);
    }

    public String uploadFile(String folder,String fileName, MultipartFile multipartFile){
        if(fileName==null || StringUtils.isEmpty(fileName))
            fileName  =FilenameUtils.removeExtension(multipartFile.getOriginalFilename());
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
        if(fileName==null || StringUtils.isEmpty(fileName))
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


    public ResponseEntity<ResponseJsonHandler> uploadFiles(RequestJsonHandler requestJsonHandler, MultipartFile[] multipartFiles){
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
            return ResponseJsonUtil.getResponse(HttpStatus.INTERNAL_SERVER_ERROR,"Internal Server Error");

        ObjectNode dataNode  = objectMapper.createObjectNode();
        dataNode.put("downloadUrls",callbackUrls);
        return ResponseJsonUtil.getResponse(HttpStatus.OK,dataNode);
    }


    public ResponseEntity<ResponseJsonHandler> deleteFiles(List<String> urls){
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
        return ResponseJsonUtil.getResponse(HttpStatus.OK,"success");
    }


    public File convertFromMultiPartToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(multipartFile.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(multipartFile.getBytes());
        fos.close();
        return file;
    }
}
