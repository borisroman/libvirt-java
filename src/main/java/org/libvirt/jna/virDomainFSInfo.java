package org.libvirt.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class virDomainFSInfo extends Structure {
    /**
     * Path to mount point
     */
    String mountpoint;
    /**
     * Device name in the guest (e.g. "sda1")
     */
    String name;
    /**
     * Filesystem type
     */
    String fstype;
    /**
     * Number of elements in devAlias
     */
    SizeT ndevAlias;
    /**
     * Array of disk device aliases
     */
    String[] devAlias;

    private static final List<String> fields = Arrays.asList(
            "mountpoint", "name", "fstype", "ndevAlias", "devAlias");

    @Override
    protected List<String> getFieldOrder() {
        return fields;
    }
}
