package automatizacion.utils;

import java.io.File;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * HttpMultiPartUtil
 * 
 * Class receives http url, gets objects and send them to it.
 *
 * @author David Martos Grande
 */
public class HttpMultiPartUtil {

    private String requestUrl = null;

    private HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = null;
    private LinkedMultiValueMap<String, Object> map = null;
    private RestTemplate restTemplate = null;
    private HttpHeaders headers = null;

    /**
     * Initializes a new HTTP Post petition. Establishing content-type to
     * 'multipart/form-data'.
     *
     * @param requestUrl
     */
    public HttpMultiPartUtil(String requestUrl) {

        this.requestUrl = requestUrl;
        map = new LinkedMultiValueMap<>();
        restTemplate = new RestTemplate();
        headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    }

    /**
     * Adds object to petition.
     *
     * @param key - object name
     * @param object - object
     */
    public void addObject(String key, Object object) {

        if (object instanceof File) {
            FileSystemResource fileSystemResource = new FileSystemResource((File) object);
            map.add(key, fileSystemResource);
        }
        if (object instanceof String) {
            map.add(key, (String) object);
        }

    }

    /**
     * Gets executed to send header content to requestUrl.
     *
     * @return ResponseEntity
     * @throws RestClientException
     */
    public ResponseEntity<?> finish() {
        requestEntity = new HttpEntity<>(map, headers);
        return restTemplate.exchange(requestUrl, HttpMethod.POST, requestEntity, String.class);
    }

}
