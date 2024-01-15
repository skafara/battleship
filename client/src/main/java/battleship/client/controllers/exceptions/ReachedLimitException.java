package battleship.client.controllers.exceptions;

/**
 * Reached Limit Exception
 */
public class ReachedLimitException extends RuntimeException {

    private final int limit;

    /**
     * Transparently constructs
     * @param limit Limit
     */
    public ReachedLimitException(int limit) {
        this.limit = limit;
    }

    /**
     * Return the limit
     * @return Limit
     */
    public int getLimit() {
        return limit;
    }

}
