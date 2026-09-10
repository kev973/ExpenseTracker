package benoit.kevin.expensetracker.application.port.in;

/**
 * Raised when an authenticated user acts on something owned by somebody else.
 * The web layer turns it into a 403.
 */
public class NotOwnerException extends RuntimeException {
    public NotOwnerException() {
        super("not the owner");
    }
}
