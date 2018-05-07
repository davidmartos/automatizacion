package automatizacion.controllers;

import automatizacion.services.IStorageService;
import automatizacion.task.ProcessFilesTask;
import java.util.Arrays;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * FileUploadController
 * 
 * Rest controller file uploading.
 *
 * @author David Martos Grande
 */
@RestController
public class FileUploadController {

    private static final Logger logger = LogManager.getLogger(FileUploadController.class);

    private final IStorageService storageService;
    
    private final ProcessFilesTask pft;

    @Autowired
    public FileUploadController(IStorageService storageService, ProcessFilesTask pft) {
        this.storageService = storageService;
        this.pft = pft;
    }

    /**
     * Upload files and text.
     *
     * @param password - password to authenticate (text)
     * @param idPlantilla - id plantilla (text)
     * @param files - files
     * @param fileNames
     * @return HttpResponseEntity
     */
    @PostMapping(name = "/upload/multiple", consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadMultipleFiles(
            @RequestPart("password") @NotNull String password,
            @RequestPart("idPlantilla") @NotNull String idPlantilla,
            @RequestPart("files") @NotNull MultipartFile[] files,
            @RequestPart("fileNames") @NotNull String fileNames) {

        if (password.equals("HRvBwWYaWtybYtLRFj6ysyCiFiebJsgo")) {

            logger.info("Inicio recepción de ficheros. Plantilla " + idPlantilla);

            return storageService.saveUploadedFiles(idPlantilla, Arrays.asList(files), fileNames);

        }
        logger.error("Se ha realizado una petición pero la contraseña no es correcta.");
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    @GetMapping(name = "/turnontask")
    public ResponseEntity<?> turnOnTask() {
        try {
            pft.execute(null);
        } catch (JobExecutionException ex) {
            java.util.logging.Logger.getLogger(FileUploadController.class.getName()).log(Level.SEVERE, null, ex);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(HttpStatus.OK);
    }
}
