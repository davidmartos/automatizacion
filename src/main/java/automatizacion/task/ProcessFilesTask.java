package automatizacion.task;

import automatizacion.configuration.ConexionProperties;
import automatizacion.configuration.PathProperties;
import automatizacion.services.IStorageService;
import automatizacion.utils.HttpMultiPartUtil;
import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 *
 * ProcessFilesTask
 *
 * @author David Martos Grande
 */
@Service
public class ProcessFilesTask implements Job {

    //<editor-fold defaultstate="collapsed" desc="Properties">
    private static final Logger logger = LogManager.getLogger(ProcessFilesTask.class);

    @Autowired
    public PathProperties pathProp;

    @Autowired
    public ConexionProperties conexProp;

    @Autowired
    public IStorageService storageService;

    private HttpMultiPartUtil httpMultiPartUtil;
    //</editor-fold>

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        //<editor-fold defaultstate="collapsed" desc="Code">
        logger.info("Inicio tarea Procesar Ficheros.");

        processFiles();

        logger.info("Fin tarea Procesar Ficheros.");
        //</editor-fold>
    }

    private void processFiles() {
        //<editor-fold defaultstate="collapsed" desc="Code">
        Path p1 = pathProp.getPendientes();
        Path p2 = pathProp.getFinalizadas();

        if (p1 != null && p2 != null) {
            logger.info("Inicio busqueda ficheros en " + p1.toString());
            List<Path> toProduce = storageService.loadAllFromPath(p1);
            if (toProduce == null || toProduce.isEmpty()) {
                logger.info("No se han encontrado directorios en " + p1.toString());
                return;
            }
            logger.info(p1.toString() + " --> Se han encontrado " + toProduce.size() + " directorios (plantillas).");

            for (Path path : toProduce) {
                logger.info("Inicio directorio " + path.toString());
                List<Path> files = storageService.loadAllFromPath(path);
                if (files == null || files.isEmpty()) {
                    logger.info("No se han encontrado ficheros en " + path.toString());
                    return;
                }

                Path p = null;
                String appResponse = null;

                try {
                    p = p2.resolve(path.getFileName());
                    if (!Files.exists(p)) {
                        p = Files.createDirectory(p);
                    }

                    List<Path> txtFile = storageService.getFilesByExtension(files);
                    files.remove(txtFile.get(0));

                    for (Path file : files) {

                        logger.info("Inicio ejecución fichero en aplicación Rhinoceros --> " + file.toString());

                        ProcessBuilder pb = new ProcessBuilder();
                        Process pro = pb.command(Arrays.asList(
                                "C:\\Program Files\\Rhinoceros 5 (64-bit)\\System\\Rhino.exe",
                                "/nosplash /runscript=\"-_LoadScript C:\\plugin\\plugin_final.rvb "
                                + path.toString() + "\\"
                                + " " + p.toString() + "\\"
                                + " " + file.getFileName().toString()
                                + " " + txtFile.get(0).getFileName().toString()
                                + " " + file.getFileName().toString() + " \"")
                        ).start();

                        pro.waitFor(1, TimeUnit.MINUTES);

                        if (pro.isAlive()) {

                            appResponse = "La aplicacion Rhinoceros no ha respondido";
                            logger.error(appResponse);
                            pro.destroy();
                            break;

                        } else {

                            Path resultPath = p.resolve("result_" + file.getFileName().toString() + ".txt");
                            if (!Files.exists(resultPath)) {
                                logger.error("La aplicación rhinoceros no ha respondido a la petición");
                                throw new Exception();
                            }

                            appResponse = getContentFromFile(resultPath);
                            logger.info("La aplicación rhinoceros ha respondido --> " + (appResponse.isEmpty() ? "OK" : appResponse));

                            if (!appResponse.isEmpty()) {
                                break;
                            }
                        }

                    }

                    if (sendFiles(p, appResponse)) {
                        if (storageService.deleteAllFromPaths(path, p)) { //Delete files in pendientes y finalizadas
                            logger.info("Se han eliminado los directorios --> " + path.toString() + " y " + p.toString());
                        }
                    }

                } catch (IOException ex) {
                    logger.error("No se ha podido crear directorio " + path.getFileName() + " en " + p2.toString());
                } catch (Exception ex) {
                    logger.error("Se ha producido un error --> " + ex.getMessage());
                    if (storageService.deleteAllFromPaths(p)) {
                        logger.info("Se ha eliminado el directorio " + p.toString());
                    }
                }
            }
            try {
                Runtime.getRuntime().exec("taskkill /F /IM C:\\Program Files\\Rhinoceros 5 (64-bit)\\System\\Rhino.exe");
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(ProcessFilesTask.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //</editor-fold>
    }

    private boolean sendFiles(Path dir, String error) {
        //<editor-fold defaultstate="collapsed" desc="Code">

        logger.info("Inicio de envío de ficheros del directorio " + dir.toString() + ". " + (error.isEmpty() ? "" : "Error= " + error));

        httpMultiPartUtil = new HttpMultiPartUtil(conexProp.getApiMultiUploadTest());

        List<String> fileNames = new ArrayList();

        List<Path> elements = storageService.loadAllFromPath(dir); //Se cargan todos los elementos de la carpeta

        httpMultiPartUtil.addObject("password", "JznYByWlxx2OcuD3mR3Zy7ZlhdkkO8il");

        httpMultiPartUtil.addObject("idPlantilla", dir.getFileName().toString());

        httpMultiPartUtil.addObject("error", error);

        if (elements != null && !elements.isEmpty()) {
            for (Path element : elements) {
                if (!element.getFileName().toString().substring(element.getFileName().toString().lastIndexOf(".") + 1).toLowerCase().equalsIgnoreCase("txt")) {
                    httpMultiPartUtil.addObject("files", element.toFile());
                    fileNames.add(element.getFileName().toString().substring(0, element.getFileName().toString().lastIndexOf(".")));
                }
            }
            httpMultiPartUtil.addObject("fileNames", new Gson().toJson(fileNames));
        } else {
            httpMultiPartUtil.addObject("files", new File(""));
            httpMultiPartUtil.addObject("fileNames", "");
        }

        ResponseEntity<?> response = httpMultiPartUtil.finish();

        if (response.getStatusCode() != HttpStatus.OK) {
            logger.error("No se han podido enviar los ficheros del directorio " + dir.toString() + ". Error: " + response.getBody());
            return false;
        }

        logger.info("Directorio enviado al ERP. Plantilla: " + dir.getFileName().toString());
        return true;

        //</editor-fold>
    }

    private String getContentFromFile(Path path) throws IOException {
        //<editor-fold defaultstate="collapsed" desc="Code">
        List<String> lines = FileUtils.readLines(path.toFile(), StandardCharsets.UTF_8);
        FileUtils.deleteQuietly(path.toFile());
        if (lines != null) {
            String text = lines.get(0);
            int index = text.indexOf("=");
            return index == -1 ? "" : text.substring(index + 1, text.length());
        }
        return "Error en la aplicación Rhinoceros";
        //</editor-fold>
    }
}
