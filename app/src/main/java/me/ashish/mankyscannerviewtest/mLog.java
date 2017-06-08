package me.ashish.mankyscannerviewtest;

import android.util.Log;


public class mLog {
     boolean isEnabled = true;

    private static mLog logInstance;

    public static mLog getInstance(){
        if(logInstance != null){
            return logInstance;
        } else {
            logInstance = new mLog();
            return logInstance;
        }
    }

    public  void MyLog(String TAG, String Header,String Messsage) {
        if (isEnabled) {
            Log.d(TAG, Header+" = "+Messsage);
        }

    }
}
