package org.libvirt.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

/**
 * JNA mapping for the virDomainIOThreadInfo structure
 */
public class virDomainIOThreadInfo extends Structure {
    /**
     * IOThread ID
     */
    int iothread_id;
    /**
     * CPU map for thread. A pointer to an
    /* array of real CPUs (in 8-bit bytes)
     */
    byte[] cpumap;
    /**
     * cpumap size
     */
    int cpumaplen;

    private static final List<String> fields = Arrays.asList(
            "iothread_id", "cpumap", "cpumaplen");

    @Override
    protected List<String> getFieldOrder() {
        return fields;
    }
}
