package me.ashish.mankyscannerviewtest;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.Result;

/**
 * Created by Ashish on 5/24/2017.
 */

public class MankyScannerView extends MankyScannerViewHelper {


    public MankyScannerView(Context context) {
        super(context);
    }

    public MankyScannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MankyScannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    void onReceivedResult(Result result) {
        mLog.getInstance().MyLog("KKKK", "CODE RESULT", result.getText());
    }
}
