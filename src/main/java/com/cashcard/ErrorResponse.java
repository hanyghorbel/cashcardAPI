package com.cashcard;

import java.util.Map;

public record ErrorResponse(String error, int status, Map<String, String> details){}


