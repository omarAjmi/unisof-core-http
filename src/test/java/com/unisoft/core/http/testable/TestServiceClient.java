package com.unisoft.core.http.testable;

import com.unisoft.core.http.client.ServiceClient;
import com.unisoft.core.http.client.ServiceClientOptions;

public class TestServiceClient extends ServiceClient {
    protected TestServiceClient(ServiceClientOptions options) {
        super(options);
    }
}
