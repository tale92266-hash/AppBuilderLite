package com.nobi.appbuilder.dataflow;

import android.content.Intent;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data a screen returns to its caller. Only string key/value pairs —
 * no large images or arbitrary objects travel through Intent extras.
 */
public class ScreenResult {

    public static final int RESULT_OK = -1;
    public static final int RESULT_CANCELED = 0;

    private int resultCode = RESULT_CANCELED;
    private final Map<String, String> data = new LinkedHashMap<>();

    public ScreenResult() {
    }

    public ScreenResult(int resultCode) {
        this.resultCode = resultCode;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void put(String key, String value) {
        if (key != null && value != null) {
            data.put(key, value);
        }
    }

    public String get(String key) {
        return data.get(key);
    }

    public Intent toIntent() {
        Intent intent = new Intent();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            intent.putExtra("result_" + entry.getKey(), entry.getValue());
        }
        return intent;
    }

    public static ScreenResult fromIntent(Intent intent) {
        ScreenResult result = new ScreenResult();
        if (intent == null || intent.getExtras() == null) {
            return result;
        }
        for (String key : intent.getExtras().keySet()) {
            if (key != null && key.startsWith("result_")) {
                Object value = intent.getExtras().get(key);
                if (value != null) {
                    result.put(key.substring("result_".length()),
                            String.valueOf(value));
                }
            }
        }
        return result;
    }
}
