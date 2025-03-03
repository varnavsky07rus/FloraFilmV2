package com.alaka_ala.florafilm.ui.util.local;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

public abstract class AsyncThreadBuilder {
    /**Если переопределить Handler то {@link AsyncThreadBuilder#finishHandler(Bundle)} не будет вызываться */
    public void setHandler(Handler handler) {
        this.handler = handler;
    }
    public Handler getHandler() {
        return handler;
    }

    private Handler handler;
    private final Runnable runnable;
    private final Thread thread;

    public AsyncThreadBuilder() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                finishHandler(msg.getData());
                return false;
            }
        });
        runnable = start(handler);
        thread = new Thread(runnable);

    }

    public abstract Runnable start(Handler finishHandler);

    public abstract void finishHandler(Bundle bundle);

    public void onStart() {
        thread.start();
    }

    public void onStop() {
        thread.interrupt();
        handler.removeCallbacks(runnable);
    }



}
