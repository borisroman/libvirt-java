package org.libvirt.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

/**
 * JNA mapping for the virDomainInterface structure
 */
public class virDomainInterface extends Structure {
    /**
     * Interface name
     */
    String name;
    /**
     * Hardware address, may be NULL
     */
    String hwaddr;
    /**
     * Number of items in @addrs
     */
    int naddrs;
    /**
     * Array of IP addresses
     */
    virDomainIPAddressPointer addrs;

    private static final List<String> fields = Arrays.asList(
            "name", "hwaddr", "naddrs", "addrs");

    @Override
    protected List<String> getFieldOrder() {
        return fields;
    }
}
