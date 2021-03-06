package ch.epfl.sweng.opengm.parse;

public final class PFException extends Exception {

    private static final long serialVersionUID = 1L;

    public PFException() {
        super();
    }

    public PFException(String message) {
        super(message);
    }

    public PFException(Throwable throwable) {
        super(throwable);
    }

}
