package org.libvirt.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

/**
 * JNA mapping for the virDomainInterfaceStats structure
 */
public class virDomainInterfaceStats extends Structure {
    /**
     * This is a long long in the code, so a long mapping is correct
     */
    public long rx_bytes;
    /**
     * This is a long long in the code, so a long mapping is correct
     */
    public long rx_packets;
    /**
     * This is a long long in the code, so a long mapping is correct
     */
    public long rx_errs;
    /**
     * This is a long long in the code, so a long mapping is correct
     */
    public long rx_drop;
    /**
     * This is a long long in the code, so a long mapping is correct
     */
    public long tx_bytes;
    /**
     * This is a long long in the code, so a long mapping is correct
     */
    public long tx_packets;
    /**
     * This is a long long in the code, so a long mapping is correct
     */
    public long tx_errs;
    /**
     * This is a long long in the code, so a long mapping is correct
     */
    public long tx_drop;
    /**
     * This is a long long in the code, so a long mapping is correct
     */

    private static final List<String> fields = Arrays.asList(
            "rx_bytes", "rx_packets", "rx_errs", "rx_drop",
            "tx_bytes", "tx_packets", "tx_errs", "tx_drop");

    @Override
    protected List<String> getFieldOrder() {
        return fields;
    }
}
