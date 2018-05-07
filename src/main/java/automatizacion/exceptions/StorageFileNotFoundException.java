package automatizacion.exceptions;

/**
 * StorageFileNotFoundException
 *
 * @author David Martos Grande
 */
public class StorageFileNotFoundException extends StorageException {

    public StorageFileNotFoundException() {
        super();
    }

    public StorageFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
