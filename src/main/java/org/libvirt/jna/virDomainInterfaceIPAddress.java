package org.libvirt.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

/**
 * JNA mapping for the virDomainInterfaceIPAddress structure
 */
public class virDomainInterfaceIPAddress extends Structure {
    /**
     * virIPAddrType
     */
    int type;
    /**
     * IP address
     */
    String addr;
    /**
     * IP address prefix
     */
    int prefix;

    private static final List<String> fields = Arrays.asList(
            "type", "addr", "prefix");

    @Override
    protected List<String> getFieldOrder() {
        return fields;
    }
}
