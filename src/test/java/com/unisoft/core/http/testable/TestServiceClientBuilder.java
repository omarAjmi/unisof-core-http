package com.unisoft.core.http.testable;

import com.unisoft.core.http.client.ServiceClient;

public class TestServiceClientBuilder extends ServiceClient.Builder {

    @Override
    public ServiceClient build() {
        return new TestServiceClient(this.getOptions(), this.getSerializerAdapter(), this.getHttpPipeline());
    }
}
