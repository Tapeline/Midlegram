package midp.tapeline.midlegram.logging;

public class Logger {

    private final boolean isDebug;

    public Logger(boolean isDebug) {
        this.isDebug = isDebug;
    }

    public void error(String message) {
        System.out.print("ERROR | ");
        System.err.println(message);
    }

    public void error(Exception exc) {
        System.out.print("ERROR | ");
        System.err.println(exc.toString() + "\nStack trace:");
        exc.printStackTrace();
    }

    public void error(String message, Exception exc) {
        error(message);
        error(exc);
    }

    public void warn(String message) {
        System.out.print("WARN   | ");
        System.out.println(message);
    }

    public void info(String message) {
        System.out.print("INFO   | ");
        System.out.println(message);
    }

    public void debug(String message) {
        if (!isDebug) return;
        System.out.print("DEBUG  | ");
        System.out.println(message);
    }

}
