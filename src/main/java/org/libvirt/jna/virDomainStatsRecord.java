package org.libvirt.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

/**
 * JNA mapping for the virDomainStatsRecord structure
 */
public class virDomainStatsRecord extends Structure {
    DomainPointer dom;
    TypedParameterPointer params;
    int nparams;

    private static final List<String> fields = Arrays.asList(
            "dom", "params", "nparams");

    @Override
    protected List<String> getFieldOrder() {
        return fields;
    }
}
