package automatizacion.services;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * IStorageService
 *
 * @author David Martos Grande
 *
 */
public interface IStorageService {

    void init();

    void saveUploadedFile(MultipartFile file, String fileName) throws IOException;

    ResponseEntity<?> saveUploadedFiles(String idPlantilla, List<MultipartFile> files, String fileNames);

    List<Path> loadAllFromPath(Path path);

    List<Path> getFilesByExtension(List<Path> dir);
    
    boolean deleteAllFromPaths(Path... list);
}
