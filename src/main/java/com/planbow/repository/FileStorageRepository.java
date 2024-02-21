package com.planbow.repository;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.apache.http.util.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Objects;

@Repository
public class FileStorageRepository {

    @Value("${digitalocean.spaces.bucket.name}")
    private String DO_SPACES_BUCKET_NAME;

    @Value("${digitalocean.spaces.bucket.folder}")
    private String DO_SPACES_BUCKET_FOLDER;

    private AmazonS3 s3Client;

    @Autowired
    public void setS3Client(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    public String saveFile(MultipartFile multipartFile, String fileName, String folder) throws IOException {

        if(folder!=null && !TextUtils.isEmpty(folder))
            DO_SPACES_BUCKET_FOLDER=folder;

        String key = DO_SPACES_BUCKET_FOLDER + File.separator+ fileName;
        File fileToUpload = convertFromMultiPartToFile(multipartFile);
        s3Client.putObject(new PutObjectRequest(DO_SPACES_BUCKET_NAME, key, fileToUpload)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        return s3Client.getUrl(DO_SPACES_BUCKET_NAME,key).toString();
    }


    public String saveFile(File file, String fileName, String folder) throws IOException {

        if(folder!=null && !TextUtils.isEmpty(folder))
            DO_SPACES_BUCKET_FOLDER=folder;
        String key = DO_SPACES_BUCKET_FOLDER + File.separator+ fileName ;
        s3Client.putObject(new PutObjectRequest(DO_SPACES_BUCKET_NAME, key, file)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        return s3Client.getUrl(DO_SPACES_BUCKET_NAME,key).toString();
    }


    public void deleteFile(String url) throws Exception {
        String decodedUrl=null;
        try {
            decodedUrl= URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        String path = new URL(decodedUrl).getPath().substring(1);
        System.out.println(path);

        s3Client.deleteObject(
              new DeleteObjectRequest(DO_SPACES_BUCKET_NAME,path)) ;
    }

    private File convertFromMultiPartToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(multipartFile.getBytes());
        fos.close();
        return file;
    }

}
