package com.aliyun.openservices.log.logback.example;

import com.aliyun.openservices.log.common.auth.Credentials;
import com.aliyun.openservices.log.common.auth.CredentialsProvider;
import com.aliyun.openservices.log.common.auth.DefaultCredentials;
import com.aliyun.openservices.log.common.auth.StaticCredentialsProvider;
import com.aliyun.openservices.log.logback.CredentialsProviderBuilder;

public class ExampleCredentialsProviderBuilder implements CredentialsProviderBuilder {
    private String accessKeyId;
    private String accessKeySecret;
    private String securityToken;

    /**
     * getCredentialsProvider should return a new instance of {@link CredentialsProvider}
     * for each call.
     *
     * @return the returned {@link CredentialsProvider} must be thread-safe,
     * and cache credentials to avoid update credentials too frequently.
     */
    @Override
    public CredentialsProvider getCredentialsProvider() throws Exception {
        Credentials credentials =  new DefaultCredentials(accessKeyId, accessKeySecret, securityToken);
        return new StaticCredentialsProvider(credentials);
    }

    // Logback uses setter method to inject values that parsed from logback.xml
    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }
}
