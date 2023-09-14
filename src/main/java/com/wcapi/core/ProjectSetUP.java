package com.wcapi.core;

import io.restassured.http.ContentType;

public interface ProjectSetUP {

    //String APP_BASE_URL = "http://barrigarest.wcaquino.me"; in case using the http, port must be :80

    // url base to test
    String APP_BASE_URL = "https://barrigarest.wcaquino.me";

    // port of the app
    Integer APP_PORT = 443;

    // url base path
    String APP_BASE_PATH = " ";

    //Content type
    ContentType APP_CONTENT_TYPE = ContentType.JSON;

    // Max time to
    Long MAX_TIMEOUT = 5000L;


}
