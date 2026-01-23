package midp.tapeline.midlegram.uibase;

public interface Activity {

    void onCreate();
    void onResume();
    void onPause();
    void onDestroy();
    void setOwnedBy(UI ui);

}

