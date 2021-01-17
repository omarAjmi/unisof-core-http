package com.unisoft.core.http.testable;

import com.unisoft.core.http.HttpPipeline;
import com.unisoft.core.http.client.ServiceClient;
import com.unisoft.core.http.client.ServiceClientOptions;
import com.unisoft.core.http.serialize.SerializerAdapter;

public class TestServiceClient extends ServiceClient {
    protected TestServiceClient(ServiceClientOptions options, SerializerAdapter serializerAdapter, HttpPipeline httpPipeline) {
        super(options, serializerAdapter, httpPipeline);
    }
}
