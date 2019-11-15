package kr.koo.findme;

import android.util.Log;
import android.view.View;

import java.util.concurrent.Callable;

public class NFCButtonOnClickListener implements View.OnClickListener {
    String id;
    Callable<Void> callback;
    public NFCButtonOnClickListener(String id, Callable<Void> callback) {
        this.id = id;
        this.callback = callback;
    }

    @Override
    public void onClick(View v)
    {
        Log.v("test",id);
        try {
            callback.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
