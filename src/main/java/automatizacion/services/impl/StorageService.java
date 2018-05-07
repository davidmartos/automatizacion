package automatizacion.services.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import automatizacion.configuration.PathProperties;
import automatizacion.exceptions.StorageException;
import automatizacion.services.IStorageService;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * StorageService
 *
 * @author David Martos Grande
 */
@Service
public class StorageService implements IStorageService {

    private static final Logger logger = LogManager.getLogger(StorageService.class);

    @Autowired
    public PathProperties pathProp;

    private Path subPath;

    @Override
    public void init() {
        //<editor-fold defaultstate="collapsed" desc="Code">
        try {
            if (!Files.exists(pathProp.getPendientes())) {
                Files.createDirectory(pathProp.getPendientes());
            }
            if (!Files.exists(pathProp.getFinalizadas())) {
                Files.createDirectory(pathProp.getFinalizadas());
            }
        } catch (IOException e) {
            logger.error("Could not initialize storage");
            throw new StorageException("Could not initialize storage", e);
        }
        //</editor-fold>
    }

    /**
     * Saves file passed by parameter
     *
     * @param file - file to save
     * @throws IOException
     */
    @Override
    public void saveUploadedFile(MultipartFile file, String fileName) throws IOException {
        //<editor-fold defaultstate="collapsed" desc="Code">
        if (file.isEmpty()) {
            logger.error("Failed to store empty file " + file.getOriginalFilename());
            throw new StorageException();
        }
        Files.copy(file.getInputStream(), subPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        //</editor-fold>
    }

    /**
     * Saves files passed by parameter in directory.
     *
     * @param idPlantilla - name of directory
     * @param files - files to save
     * @param fileNames - names of files to save
     * @return ResponseEntity - HttpStatus
     */
    @Override
    public ResponseEntity<?> saveUploadedFiles(String idPlantilla, List<MultipartFile> files, String fileNames) {
        //<editor-fold defaultstate="collapsed" desc="Code">
        try {

            subPath = pathProp.getPendientes().resolve(idPlantilla);

            if (!Files.exists(subPath)) {
                Files.createDirectory(subPath);
            }

            List<String> fileNamesList = new Gson().fromJson(fileNames, new TypeToken<ArrayList<String>>(){}.getType());

            for (int i = 0; i < files.size(); i++) {
                saveUploadedFile(files.get(i), fileNamesList.get(i));
            }

            return new ResponseEntity(HttpStatus.OK);

        } catch (StorageException e) {
            logger.error("Error al guardar alguno de los ficheros.");
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (FileAlreadyExistsException e) {
            logger.error("Error al guardar alguno de los ficheros: ya existe.");
            return new ResponseEntity(HttpStatus.CONFLICT);
        } catch (IOException e) {
            logger.error("Error.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        //</editor-fold>
    }

    /**
     * Load all directories found in path passed by parameter.
     *
     * @param path - path to search
     * @return - list of paths
     */
    @Override
    public List<Path> loadAllFromPath(Path path) {
        //<editor-fold defaultstate="collapsed" desc="Code">
        List<Path> result = null;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {

            result = new ArrayList<>();

            for (Path entry : stream) {
                result.add(entry);
            }

        } catch (IOException e) {
            logger.error("Failed to read stored files");
            throw new StorageException("Failed to read stored files", e);
        }
        return result;
        //</editor-fold>
    }

    @Override
    public List<Path> getFilesByExtension(List<Path> dir) {
        //<editor-fold defaultstate="collapsed" desc="Code">
        List<Path> list = null;
        if (dir != null) {
            list = new ArrayList<>();
            for (Path path : dir) {
                if (FilenameUtils.getExtension(path.getFileName().toString()).equals("txt")) {
                    list.add(path);
                }
            }
        }
        return list;
        //</editor-fold>
    }

    @Override
    public boolean deleteAllFromPaths(Path... list) {
        //<editor-fold defaultstate="collapsed" desc="Code">
        if (list != null) {
            for (Path path : list) {
                if (!FileSystemUtils.deleteRecursively(path.toFile())) {
                    return false;
                }
            }
        }
        return true;
        //</editor-fold>
    }

}
