package org.springframework.ai.moonshotai.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.function.Consumer;

public class ApiUtils {

    public final static String DEFAULT_BASE_URL = "https://api.moonshot.cn/v1";

    public final static Float DEFAULT_TEMPERATURE = 0.95f;

    public final static Float DEFAULT_TOP_P = 1.0f;
    public static Consumer<HttpHeaders> getJsonContentHeaders(String apiKey) {
        return (headers) -> {
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
        };
    };


}
