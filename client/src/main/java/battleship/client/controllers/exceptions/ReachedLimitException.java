package battleship.client.controllers.exceptions;

public class ReachedLimitException extends RuntimeException {

    private final int limit;

    public ReachedLimitException(int limit) {
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }

}
