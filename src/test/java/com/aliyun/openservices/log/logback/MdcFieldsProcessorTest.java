package com.aliyun.openservices.log.logback;

import org.junit.Test;

import static org.junit.Assert.*;

public class MdcFieldsProcessorTest {

    @Test
    public void testNullConfiguration() {
        MdcFieldsProcessor processor = new MdcFieldsProcessor(null);
        assertNull(processor.getLogFieldName("anyKey"));
    }

    @Test
    public void testEmptyConfiguration() {
        MdcFieldsProcessor processor = new MdcFieldsProcessor("");
        assertNull(processor.getLogFieldName("anyKey"));

        processor = new MdcFieldsProcessor("   ");
        assertNull(processor.getLogFieldName("anyKey"));
    }

    @Test
    public void testCaptureAllFields() {
        MdcFieldsProcessor processor = new MdcFieldsProcessor("*");

        // All fields should be captured with original names
        assertEquals("traceId", processor.getLogFieldName("traceId"));
        assertEquals("userId", processor.getLogFieldName("userId"));
        assertEquals("spanId", processor.getLogFieldName("spanId"));
        assertEquals("anyField", processor.getLogFieldName("anyField"));
    }

    @Test
    public void testCaptureSpecifiedFields() {
        MdcFieldsProcessor processor = new MdcFieldsProcessor("traceId,userId,spanId");

        // Only specified fields should be captured
        assertEquals("traceId", processor.getLogFieldName("traceId"));
        assertEquals("userId", processor.getLogFieldName("userId"));
        assertEquals("spanId", processor.getLogFieldName("spanId"));

        // Other fields should not be captured
        assertNull(processor.getLogFieldName("otherId"));
        assertNull(processor.getLogFieldName("randomField"));
    }

    @Test
    public void testCaptureWithRename() {
        MdcFieldsProcessor processor = new MdcFieldsProcessor("traceId=trace_id,userId=user_id,spanId");

        // Renamed fields
        assertEquals("trace_id", processor.getLogFieldName("traceId"));
        assertEquals("user_id", processor.getLogFieldName("userId"));

        // Field without rename
        assertEquals("spanId", processor.getLogFieldName("spanId"));

        // Non-configured field should not be captured
        assertNull(processor.getLogFieldName("otherId"));
    }

    @Test
    public void testCaptureAllWithPartialRename() {
        MdcFieldsProcessor processor = new MdcFieldsProcessor("*,traceId=trace_id,userId=user_id");

        // Renamed fields
        assertEquals("trace_id", processor.getLogFieldName("traceId"));
        assertEquals("user_id", processor.getLogFieldName("userId"));

        // Other fields should be captured with original names
        assertEquals("spanId", processor.getLogFieldName("spanId"));
        assertEquals("requestId", processor.getLogFieldName("requestId"));
        assertEquals("anyOtherField", processor.getLogFieldName("anyOtherField"));
    }

    @Test
    public void testWithSpaces() {
        // Trim only happens at the beginning for the whole string, not for individual
        // fields
        // Spaces within field names or after split are preserved
        MdcFieldsProcessor processor = new MdcFieldsProcessor(" traceId=trace_id,userId=user_id,spanId ");

        // Leading/trailing spaces of the whole string are trimmed
        assertEquals("trace_id", processor.getLogFieldName("traceId"));
        assertEquals("user_id", processor.getLogFieldName("userId"));
        assertEquals("spanId", processor.getLogFieldName("spanId"));

        assertNull(processor.getLogFieldName("otherId"));
    }

    @Test
    public void testSpacesInFieldNames() {
        // Spaces within field names after split are NOT trimmed
        MdcFieldsProcessor processor = new MdcFieldsProcessor("key with space=renamed, another key");

        // Field names with spaces are preserved
        assertEquals("renamed", processor.getLogFieldName("key with space"));
        // Note: " another key" has a leading space because of split(",")
        assertEquals(" another key", processor.getLogFieldName(" another key"));

        // Without exact spacing won't match
        assertNull(processor.getLogFieldName("key"));
        assertNull(processor.getLogFieldName("another key"));
    }

    @Test
    public void testMdcKeyWithEquals() {
        // Test MDC key name containing equals sign (using lastIndexOf)
        // Format: "mdcKey=renamedKey" where mdcKey can contain '=' but renamedKey
        // cannot
        MdcFieldsProcessor processor = new MdcFieldsProcessor("key=value=renamed_key,normal=normal_key");

        // Should split at the last '=', supporting MDC key names with '='
        assertEquals("renamed_key", processor.getLogFieldName("key=value"));
        assertEquals("normal_key", processor.getLogFieldName("normal"));

        // These should not match
        assertNull(processor.getLogFieldName("key"));
        assertNull(processor.getLogFieldName("value"));
    }

    @Test
    public void testMdcKeyWithMultipleEquals() {
        // Test MDC key with multiple equals signs
        MdcFieldsProcessor processor = new MdcFieldsProcessor("a=b=c=d=final_key");

        // Should split at the last '=': mdcKey="a=b=c=d", renamedKey="final_key"
        assertEquals("final_key", processor.getLogFieldName("a=b=c=d"));
        assertNull(processor.getLogFieldName("a"));
        assertNull(processor.getLogFieldName("a=b"));
    }

    @Test
    public void testEmptyFieldName() {
        MdcFieldsProcessor processor = new MdcFieldsProcessor("traceId,,userId");

        // Empty fields should be ignored
        assertEquals("traceId", processor.getLogFieldName("traceId"));
        assertEquals("userId", processor.getLogFieldName("userId"));
    }

    @Test
    public void testNullKey() {
        MdcFieldsProcessor processor = new MdcFieldsProcessor("traceId,userId");

        assertNull(processor.getLogFieldName(null));
    }

    @Test
    public void testEmptyKey() {
        MdcFieldsProcessor processor = new MdcFieldsProcessor("traceId,userId");

        assertNull(processor.getLogFieldName(""));
    }

    @Test
    public void testComplexScenario() {
        // Test: capture all fields, rename some, keep others
        MdcFieldsProcessor processor = new MdcFieldsProcessor("*,THREAD_ID=thread_id,REQUEST_ID=request_id");

        // Renamed fields
        assertEquals("thread_id", processor.getLogFieldName("THREAD_ID"));
        assertEquals("request_id", processor.getLogFieldName("REQUEST_ID"));

        // Non-renamed fields should keep original names
        assertEquals("USER_ID", processor.getLogFieldName("USER_ID"));
        assertEquals("SESSION_ID", processor.getLogFieldName("SESSION_ID"));
    }

    @Test
    public void testOnlyRenameWithoutStar() {
        // Only capture and rename specific fields
        MdcFieldsProcessor processor = new MdcFieldsProcessor("THREAD_ID=tid,MDC_KEY=mdc");

        assertEquals("tid", processor.getLogFieldName("THREAD_ID"));
        assertEquals("mdc", processor.getLogFieldName("MDC_KEY"));

        // Other fields should not be captured
        assertNull(processor.getLogFieldName("USER_ID"));
        assertNull(processor.getLogFieldName("OTHER_KEY"));
    }

    @Test
    public void testMixedConfiguration() {
        // Mix of renamed and non-renamed fields
        MdcFieldsProcessor processor = new MdcFieldsProcessor("key1=renamed1,key2,key3=renamed3,key4");

        assertEquals("renamed1", processor.getLogFieldName("key1"));
        assertEquals("key2", processor.getLogFieldName("key2"));
        assertEquals("renamed3", processor.getLogFieldName("key3"));
        assertEquals("key4", processor.getLogFieldName("key4"));

        assertNull(processor.getLogFieldName("key5"));
    }

    @Test
    public void testStarPosition() {
        // Star can appear anywhere in the configuration
        MdcFieldsProcessor processor1 = new MdcFieldsProcessor("*,key1=renamed1");
        MdcFieldsProcessor processor2 = new MdcFieldsProcessor("key1=renamed1,*");
        MdcFieldsProcessor processor3 = new MdcFieldsProcessor("key1=renamed1,*,key2=renamed2");

        // All should capture all fields
        assertEquals("renamed1", processor1.getLogFieldName("key1"));
        assertEquals("other", processor1.getLogFieldName("other"));

        assertEquals("renamed1", processor2.getLogFieldName("key1"));
        assertEquals("other", processor2.getLogFieldName("other"));

        assertEquals("renamed1", processor3.getLogFieldName("key1"));
        assertEquals("renamed2", processor3.getLogFieldName("key2"));
        assertEquals("other", processor3.getLogFieldName("other"));
    }

    @Test
    public void testInvalidFormat() {
        // Test edge cases with equals sign - invalid formats should be ignored
        MdcFieldsProcessor processor = new MdcFieldsProcessor("=value,key=,=,validKey");

        // Invalid formats should be ignored
        // "=value" - equals at start, should be ignored
        assertNull(processor.getLogFieldName("=value"));

        // "key=" - equals at end, should be ignored
        assertNull(processor.getLogFieldName("key="));
        assertNull(processor.getLogFieldName("key"));

        // "=" - only equals, should be ignored
        assertNull(processor.getLogFieldName("="));

        // Valid key should work
        assertEquals("validKey", processor.getLogFieldName("validKey"));

        // Other keys should not be captured
        assertNull(processor.getLogFieldName("normalKey"));
    }

    @Test
    public void testCaseSensitivity() {
        MdcFieldsProcessor processor = new MdcFieldsProcessor("TraceId=trace_id,userId");

        // Keys are case-sensitive
        assertEquals("trace_id", processor.getLogFieldName("TraceId"));
        assertNull(processor.getLogFieldName("traceId")); // Different case
        assertNull(processor.getLogFieldName("TRACEID"));

        assertEquals("userId", processor.getLogFieldName("userId"));
        assertNull(processor.getLogFieldName("UserId"));
    }
}
