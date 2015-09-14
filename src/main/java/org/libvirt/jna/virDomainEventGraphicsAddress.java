package org.libvirt.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class virDomainEventGraphicsAddress extends Structure {
    /**
     * Address family, virDomainEventGraphicsAddressType
     */
    int family;
    /**
     * Address of node (eg IP address, or UNIX path)
     */
    String node;
    /**
     * Service name/number (eg TCP port, or NULL)
     */
    String service;

    private static final List<String> fields = Arrays.asList(
            "family", "node", "service");

    @Override
    protected List<String> getFieldOrder() {
        return fields;
    }
}
