package com.aliyun.openservices.log.logback;


import com.aliyun.openservices.log.common.auth.CredentialsProvider;

/**
 * Customized CredentialsProviderBuilder
 * <p/>
 * Example:
 * <pre>
 * {@code
 *    class ExampleBuilder implements CredentialsProviderBuilder{
 *      private String param1;
 *      private long paramField2;
 *
 *      public CredentialsProvider getCredentialsProvider(){
 *          return new YourCredentialsProvider(param1, paramField2);
 *      }
 *      public void setParam1(String param1) {
 *          this.param1 = param1;
 *      }
 *      public void setParamField2(long paramField2) {
 *          this.paramField2 = paramField2;
 *      }
 *    }
 * }
 * </pre>
 */
public interface CredentialsProviderBuilder {
    /**
     * getCredentialsProvider should return a new instance of {@link CredentialsProvider}
     * for each call.
     *
     * @return the returned {@link CredentialsProvider} must be thread-safe,
     * and cache credentials to avoid update credentials too frequently.
     *
     * @throws Exception if fail to create new CredentialsProvider instance
     */
    CredentialsProvider getCredentialsProvider() throws Exception;
}
