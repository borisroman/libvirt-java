package org.libvirt.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class virDomainBlockInfo extends Structure {
    /**
     * Logical size in bytes of the image (how much storage the guest will see)
     */
    public long capacity;
    /**
     * Host storage in bytes occupied by the image (such as highest
     * allocated extent if there are no holes, similar to 'du')
     */
    public long allocation;
    /**
     * Host physical size in bytes of the image container (last
     * offset, similar to 'ls')
     */
    public long physical;

    private static final List<String> fields = Arrays.asList(
            "capacity", "allocation", "physical");

    @Override
    protected List<String> getFieldOrder() {
        return fields;
    }
}
