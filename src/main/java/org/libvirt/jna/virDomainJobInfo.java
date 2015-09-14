package org.libvirt.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class virDomainJobInfo extends Structure {
    /**
     * One of virDomainJobType
     */
    public int type;
    /**
     * Time is measured in milliseconds
     *
     * Always set
     */
    public long timeElapsed;
    /**
     * Only for VIR_DOMAIN_JOB_BOUNDED
     */
    public long timeRemaining;
    /**
     * Data is measured in bytes unless otherwise specified
     * and is measuring the job as a whole.
     *
     * For VIR_DOMAIN_JOB_UNBOUNDED, dataTotal may be less
     * than the final sum of dataProcessed + dataRemaining
     * in the event that the hypervisor has to repeat some
     * data, such as due to dirtied pages during migration.
     *
     * For VIR_DOMAIN_JOB_BOUNDED, dataTotal shall always
     * equal the sum of dataProcessed + dataRemaining.
     */
    public long dataTotal;
    public long dataProcessed;
    public long dataRemaining;
    /**
     * As above, but only tracking guest memory progress
     */
    public long memTotal;
    public long memProcessed;
    public long memRemaining;
    /**
     * As above, but only tracking guest disk file progress
     */
    public long fileTotal;
    public long fileProcessed;
    public long fileRemaining;

    private static final List<String> fields = Arrays.asList(
            "type", "timeElapsed", "timeRemaining", "dataTotal",
            "dataProcessed", "dataRemaining", "memTotal", "memProcessed",
            "memRemaining", "fileTotal", "fileProcessed", "fileRemaining");

    @Override
    protected List<String> getFieldOrder() {
        return fields;
    }
}
