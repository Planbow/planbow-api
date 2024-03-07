package com.planbow.utility;

import com.planbow.documents.planboard.Attachments;
import com.planbow.documents.planboard.MetaData;
import com.planbow.documents.planboard.Planboard;
import com.planbow.repository.PlanboardApiRepository;
import com.planbow.services.FileStorageServices;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.time.Instant;
import static com.planbow.utility.PlanbowUtility.DIRECTORY_BOARDS;
import static com.planbow.utility.PlanbowUtility.DIRECTORY_ROOT;


public class FileProcessor implements Runnable{

    private final FileStorageServices fileStorageServices;
    private final PlanboardApiRepository planboardApiRepository ;
    private final Planboard planboard;
    private final MultipartFile multipartFile;

    public FileProcessor(Planboard planboard, MultipartFile multipartFile,FileStorageServices fileStorageServices,PlanboardApiRepository planboardApiRepository) {
        this.planboard = planboard;
        this.multipartFile = multipartFile;
        this.fileStorageServices = fileStorageServices;
        this.planboardApiRepository = planboardApiRepository;
    }

    @Override
    public void run() {

        if (multipartFile != null && !multipartFile.isEmpty()) {
            Attachments attachment = new Attachments();
            attachment.setPlanboardId(planboard.getId());
            attachment.setType(Attachments.TYPE_ROOT);
            attachment.setActive(true);
            attachment.setUploadedOn(Instant.now());
            String folder =DIRECTORY_ROOT+File.separator+ planboard.getUserId()+File.separator + DIRECTORY_BOARDS + File.separator + planboard.getId();
            attachment.setMetaData(prepareAttachmentMetaData(folder));
            String mediaUrl = fileStorageServices.uploadFile(folder, null, multipartFile);
            attachment.setMediaUrl(mediaUrl);
            planboardApiRepository.saveOrUpdateAttachment(attachment);
        }
    }

    private MetaData prepareAttachmentMetaData(String folder){
        MetaData metaData = new MetaData();
        String extension = FilenameUtils.getExtension(multipartFile.getOriginalFilename());
        metaData.setExtension(extension);
        metaData.setPath(folder);
        metaData.setFileName(multipartFile.getOriginalFilename());
        metaData.setSize(multipartFile.getSize());
        return metaData;
    }
}
