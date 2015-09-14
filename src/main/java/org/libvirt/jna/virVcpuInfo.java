package org.libvirt.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

/**
 * JNA mapping for the virVcpuInfo structure
 */
public class virVcpuInfo extends Structure {
    /**
     * Virtual CPU number
     */
    public int number;
    /**
     * Value from virVcpuState
     */
    public int state;
    /**
     * CPU time used, in nanoseconds
     */
    public long cpuTime;
    /**
     * Real CPU number, or -1 if offline
     */
    public int cpu;

    private static final List<String> fields = Arrays.asList(
            "number", "state", "cpuTime", "cpu");

    @Override
    protected List<String> getFieldOrder() {
        return fields;
    }
}
