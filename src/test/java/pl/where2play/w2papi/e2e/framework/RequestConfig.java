package pl.where2play.w2papi.e2e.framework;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for API requests.
 * Used to configure query parameters, path parameters, and request body.
 */
@Data
@Builder
public class RequestConfig {

    @Builder.Default
    private Map<String, Object> queryParams = new HashMap<>();

    @Builder.Default
    private Map<String, Object> pathParams = new HashMap<>();

    private Object body;

    /**
     * Creates a new RequestConfig with default empty maps.
     *
     * @return the new RequestConfig
     */
    public static RequestConfig empty() {
        return RequestConfig.builder().build();
    }

    /**
     * Creates a new RequestConfig with the given body and default empty maps.
     *
     * @param body the request body
     * @return the new RequestConfig
     */
    public static RequestConfig withBody(Object body) {
        return RequestConfig.builder().body(body).build();
    }

    /**
     * Creates a new RequestConfig with the given query parameters and default empty path parameters map.
     *
     * @param queryParams the query parameters
     * @return the new RequestConfig
     */
    public static RequestConfig withQueryParams(Map<String, Object> queryParams) {
        return RequestConfig.builder().queryParams(queryParams).build();
    }

    /**
     * Creates a new RequestConfig with the given path parameters and default empty query parameters map.
     *
     * @param pathParams the path parameters
     * @return the new RequestConfig
     */
    public static RequestConfig withPathParams(Map<String, Object> pathParams) {
        return RequestConfig.builder().pathParams(pathParams).build();
    }

    /**
     * Adds a query parameter to the existing query parameters.
     *
     * @param key the parameter key
     * @param value the parameter value
     * @return this RequestConfig for method chaining
     */
    public RequestConfig addQueryParam(String key, Object value) {
        this.queryParams.put(key, value);
        return this;
    }

    /**
     * Adds a path parameter to the existing path parameters.
     *
     * @param key the parameter key
     * @param value the parameter value
     * @return this RequestConfig for method chaining
     */
    public RequestConfig addPathParam(String key, Object value) {
        this.pathParams.put(key, value);
        return this;
    }

    /**
     * Sets the request body.
     *
     * @param body the request body
     * @return this RequestConfig for method chaining
     */
    public RequestConfig setBody(Object body) {
        this.body = body;
        return this;
    }
}