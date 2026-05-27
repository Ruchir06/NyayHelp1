package com.nyayhelp.notificationservice.dto;

import java.util.Map;

public class EmailRequest {
    public String to;
    public String event;
    public Map<String, Object> data;
}
