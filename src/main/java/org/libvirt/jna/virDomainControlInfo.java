package org.libvirt.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

/**
 * JNA mapping for the virDomainControlInfo structure
 *
 * Structure filled in by virDomainGetControlInfo and providing details about
 * current state of control interface to a domain.
 */
public class virDomainControlInfo extends Structure {
    /**
     * control state, one of virDomainControlState
     */
    public int state;
    /**
     * state details, currently 0 except for ERROR
     * state (one of virDomainControlErrorReason)
     */
    public int details;
    /**
     * for how long (in msec) control interface
     * has been in current state (except for OK
     * and ERROR states)
     */
    public long stateTime;

    private static final List<String> fields = Arrays.asList(
            "state", "details", "stateTime");

    @Override
    protected List<String> getFieldOrder() {
        return fields;
    }
}
