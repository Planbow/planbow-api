package com.planbow.repository;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Repository
@Log4j2
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

        if(folder!=null && !StringUtils.isEmpty(folder))
            DO_SPACES_BUCKET_FOLDER=folder;

        String extension = FilenameUtils.getExtension(multipartFile.getOriginalFilename());
        String key = DO_SPACES_BUCKET_FOLDER + File.separator+ fileName + "." + extension;
        File fileToUpload = convertFromMultiPartToFile(multipartFile);
        s3Client.putObject(new PutObjectRequest(DO_SPACES_BUCKET_NAME, key, fileToUpload)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        return s3Client.getUrl(DO_SPACES_BUCKET_NAME,key).toString();
    }


    public String saveFile(File file, String fileName, String folder) throws IOException {

        if(folder!=null && !StringUtils.isEmpty(folder))
            DO_SPACES_BUCKET_FOLDER=folder;
        String key = DO_SPACES_BUCKET_FOLDER + File.separator+ fileName ;
        s3Client.putObject(new PutObjectRequest(DO_SPACES_BUCKET_NAME, key, file)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        return s3Client.getUrl(DO_SPACES_BUCKET_NAME,key).toString();
    }


    public void deleteFile(String url) throws Exception {
        String decodedUrl=null;
        decodedUrl= URLDecoder.decode(url, StandardCharsets.UTF_8);
        String path = new URL(decodedUrl).getPath().substring(1);
        s3Client.deleteObject(
              new DeleteObjectRequest(DO_SPACES_BUCKET_NAME,path)) ;
    }

    private File convertFromMultiPartToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(multipartFile.getOriginalFilename());
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(multipartFile.getBytes());
        fos.close();
        return file;
    }

}
