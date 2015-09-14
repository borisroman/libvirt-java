package org.libvirt.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class virDomainEventGraphicsSubject extends Structure {
    /**
     * Number of identities in array
     */
    int nidentity;
    /**
     * Array of identities for subject
     */
    DomainEventGraphicsSubjectIdentityPointer identities;

    private static final List<String> fields = Arrays.asList(
            "nidentity", "identities");

    @Override
    protected List<String> getFieldOrder() {
        return fields;
    }
}
