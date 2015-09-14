package org.libvirt.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class virDomainEventGraphicsSubjectIdentity extends Structure {
    /**
     * Type of identity
     */
    String type;
    /**
     * Identity value
     */
    String name;

    private static final List<String> fields = Arrays.asList(
            "type", "name");

    @Override
    protected List<String> getFieldOrder() {
        return fields;
    }
}
