package org.libvirt.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.NativeLong;
import com.sun.jna.Structure;

/**
 * JNA mapping for the virDomainBlockJobInfo structure
 */
public class virDomainBlockJobInfo extends Structure {
    /**
     * virDomainBlockJobType
     */
    int type;
    /**
     * either bytes/s or MiB/s, according to flags
     */
    NativeLong bandwidth;

    /**
     * The following fields provide an indication of block job progress.  @cur
     * indicates the current position and will be between 0 and @end.  @end is
     * the final cursor position for this operation and represents completion.
     * To approximate progress, divide @cur by @end.
     */
    long cur;
    long end;

    private static final List<String> fields = Arrays.asList(
            "type", "bandwidth", "cur", "end");

    @Override
    protected List<String> getFieldOrder() {
        return fields;
    }
}
