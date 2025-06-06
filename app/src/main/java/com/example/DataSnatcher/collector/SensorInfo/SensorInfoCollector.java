package com.example.DataSnatcher.collector.SensorInfo;

import android.content.Context;

import com.example.DataSnatcher.collector.IInfoCollector;

import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class SensorInfoCollector implements IInfoCollector {
    private final Context context;

    public SensorInfoCollector(Context context){
        this.context = context;
    }

    @Override
    public String getCategory() {
        return "传感器信息";
    }

    @Override
    public JSONObject collect() {
        CountDownLatch latch = new CountDownLatch(2);
        SensorDeviceInfo info3 = new SensorDeviceInfo();

        info3.setContext(context);
        info3.setLatch(latch);

        JSONObject info3Res = info3.getInfo();
        CompletableFuture<Void> future = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
                System.out.println("finish all sensors");
            }
        });
        return info3Res;
    }
}
