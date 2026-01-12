package com.aliyun.openservices.log.logback;

import java.util.HashMap;

/**
 * Processor for MDC fields configuration and mapping.
 * Supports formats:
 * - "*": capture all MDC fields with original names
 * - "a=b,c=d,e": capture only a, c, e; rename a to b, c to d, e keeps original
 * name
 * - "*,a=b,c=d": capture all MDC fields; rename a to b, c to d, others keep
 * original names
 */
public class MdcFieldsProcessor {

    private boolean captureAll = false;
    private final HashMap<String, String> renameMap = new HashMap<>();

    /**
     * Create a processor from mdcFields configuration string.
     * 
     * @param mdcFields MDC fields configuration
     */
    public MdcFieldsProcessor(String mdcFields) {
        if (mdcFields == null) {
            return;
        }
        mdcFields = mdcFields.trim();
        if (mdcFields.isEmpty()) {
            return;
        }

        String[] fields = mdcFields.split(",");

        for (String field : fields) {
            if (field.isEmpty()) {
                continue;
            }

            if ("*".equals(field)) {
                captureAll = true;
                continue;
            }

            int equalsIndex = field.lastIndexOf('=');
            if (equalsIndex < 0) {
                // No equals sign, treat as regular field name
                renameMap.put(field, field);
            } else {
                String mdcName = field.substring(0, equalsIndex);
                String logFieldName = field.substring(equalsIndex + 1);
                if (!mdcName.isEmpty() && !logFieldName.isEmpty()) {
                    renameMap.put(mdcName, logFieldName);
                }
            }
        }
    }

    /**
     * Get the log field name for a given MDC key.
     * 
     * @param mdcKey The MDC field key
     * @return The log field name (after rename), or null if the field should not be
     *         captured
     */
    public String getLogFieldName(String mdcKey) {
        if (mdcKey == null || mdcKey.isEmpty()) {
            return null;
        }

        if (captureAll) {
            // Capture all fields, apply rename if specified
            return renameMap.getOrDefault(mdcKey, mdcKey);
        } else {
            // Only capture fields in renameMap
            return renameMap.get(mdcKey);
        }
    }
}
