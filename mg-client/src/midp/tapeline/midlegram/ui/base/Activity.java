package midp.tapeline.midlegram.ui.base;

public interface Activity {

    void onCreate();
    void onResume();
    void onPause();
    void onDestroy();
    void setOwnedBy(UI ui);

}

