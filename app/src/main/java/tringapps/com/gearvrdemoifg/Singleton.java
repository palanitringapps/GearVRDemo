package tringapps.com.gearvrdemoifg;

public class Singleton {
    private static final Singleton ourInstance = new Singleton();
    private boolean isScreenCaptureStarted=false;

    public static Singleton getInstance() {
        return ourInstance;
    }

    private Singleton() {
    }

    public boolean isScreenCaptureStarted() {
        return isScreenCaptureStarted;
    }

    public void setScreenCaptureStarted(boolean screenCaptureStarted) {
        isScreenCaptureStarted = screenCaptureStarted;
    }
}
