package org.libvirt.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

/**
 * JNA mapping for the virDomainDiskError structure
 */
public class virDomainDiskError extends Structure {
    /**
     * disk target
     */
    String disk;
    /**
     * virDomainDiskErrorCode
     */
    int error;

    private static final List<String> fields = Arrays.asList(
            "disk", "error");

    @Override
    protected List<String> getFieldOrder() {
        return fields;
    }
}
