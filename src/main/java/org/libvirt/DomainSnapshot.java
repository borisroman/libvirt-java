package org.libvirt;

import org.libvirt.jna.DomainSnapshotPointer;
import static org.libvirt.Library.libvirt;
import static org.libvirt.ErrorHandler.processError;

import com.sun.jna.Pointer;

public class DomainSnapshot {

    static final class virDomainSnapshotCreateFlags {
        static final int VIR_DOMAIN_SNAPSHOT_CREATE_REDEFINE    = (1 << 0); /* Restore or alter
                                                                               metadata */
        static final int VIR_DOMAIN_SNAPSHOT_CREATE_CURRENT     = (1 << 1); /* With redefine, make
                                                                               snapshot current */
        static final int VIR_DOMAIN_SNAPSHOT_CREATE_NO_METADATA = (1 << 2); /* Make snapshot without
                                                                               remembering it */
        static final int VIR_DOMAIN_SNAPSHOT_CREATE_HALT        = (1 << 3); /* Stop running guest
                                                                               after snapshot */
        static final int VIR_DOMAIN_SNAPSHOT_CREATE_DISK_ONLY   = (1 << 4); /* disk snapshot, not
                                                                               system checkpoint */
        static final int VIR_DOMAIN_SNAPSHOT_CREATE_REUSE_EXT   = (1 << 5); /* reuse any existing
                                                                               external files */
        static final int VIR_DOMAIN_SNAPSHOT_CREATE_QUIESCE     = (1 << 6); /* use guest agent to
                                                                               quiesce all mounted
                                                                               file systems within
                                                                               the domain */
        static final int VIR_DOMAIN_SNAPSHOT_CREATE_ATOMIC      = (1 << 7); /* atomically avoid
                                                                               partial changes */
        static final int VIR_DOMAIN_SNAPSHOT_CREATE_LIVE        = (1 << 8); /* create the snapshot
                                                                               while the guest is
                                                                               running */
    }

    /**
     * the native virDomainSnapshotPtr.
     */
    DomainSnapshotPointer VDSP;

    /**
     * The Connect Object that represents the Hypervisor of this Domain Snapshot
     */
    private Connect virConnect;

    public DomainSnapshot(Connect virConnect, DomainSnapshotPointer VDSP) {
        this.VDSP = VDSP;
        this.virConnect = virConnect;
    }

    /**
     * Delete the Snapshot
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainSnapshotDelete">Libvirt
     *      Documentation</a>
     * @param flags
     *            controls the deletion
     * @return <em>ignore</em> (always 0)
     * @throws LibvirtException
     */
    public int delete(int flags) throws LibvirtException {
        int success = 0;
        if (VDSP != null) {
            success = processError(libvirt.virDomainSnapshotDelete(VDSP, flags));
            VDSP = null;
        }

        return success;
    }

    @Override
    protected void finalize() throws LibvirtException {
        free();
    }

    /**
     * Release the domain snapshot handle. The underlying snapshot continues to
     * exist.
     *
     * @throws LibvirtException
     * @return 0 on success
     */
    public int free() throws LibvirtException {
        int success = 0;
        if (VDSP != null) {
            success = processError(libvirt.virDomainSnapshotFree(VDSP));
            VDSP = null;
        }

        return success;
    }

    /**
     * Fetches an XML document describing attributes of the snapshot.
     *
     * @see <a href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainSnapshotGetXMLDesc">Libvirt Documentation</a>
     * @return the XML document
     */
    public String getXMLDesc() throws LibvirtException {
        return processError(libvirt.virDomainSnapshotGetXMLDesc(VDSP, 0)).toString();
    }
}
