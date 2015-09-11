package org.libvirt;

import org.libvirt.Domain.virDomainState;
import org.libvirt.jna.virDomainInfo;

/**
 * This object is returned by Domain.getInfo()
 *
 * @author stoty
 *
 */
public class DomainInfo {

    /**
     * the running state, one of virDomainFlag
     */
    public virDomainState state;
    /**
     * the maximum memory in KBytes allowed
     */
    public long maxMem;
    /**
     * the memory in KBytes used by the domain
     */
    public long memory;
    /**
     * the number of virtual CPUs for the domain
     */
    public int nrVirtCpu;

    /**
     * the CPU time used in nanoseconds
     */
    public long cpuTime;

    public DomainInfo() {

    }

    public DomainInfo(virDomainInfo info) {
        cpuTime = info.cpuTime;
        maxMem = info.maxMem.longValue();
        memory = info.memory.longValue();
        nrVirtCpu = info.nrVirtCpu;
        state = virDomainState.values()[info.state];
    }

    @Override
    public String toString() {
        return String.format("state:%s%nmaxMem:%d%nmemory:%d%nnrVirtCpu:%d%ncpuTime:%d%n", state, maxMem, memory, nrVirtCpu, cpuTime);
    }
}
