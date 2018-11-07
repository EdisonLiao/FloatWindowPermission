package com.example.floatwindow.permission;

/**
 * created by edison 2018/11/4
 */
public interface IUsageRecord {

    String FLOATBALL_SHOW = "FLOATBALL_SHOW";
    String FLOATBALL_CLICK = "FLOATBALL_CLICK";
    String FLOATBALL_EMOJI = "FLOATBALL_EMOJI";
    String FLOATBALL_RESPONSE = "FLOATBALL_RESPONSE";
    String FLOATBALL_BACK_CLICK = "FLOATBALL_BACK_CLICK";
    String FLOATBALL_CLOSE = "FLOATBALL_CLOSE";
    String FLOATBALL_EMOJI_CLICK = "FLOATBALL_EMOJI_CLICK";
    String FLOATBALL_RESPONSE_CLICK = "FLOATBALL_RESPONSE_CLICK";
    String FLOATBALL_RESPONSE_ADD = "FLOATBALL_RESPONSE_ADD";

    void pv(String record);
    void pv(String record, String value);


}
