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
     * virDomainSnapshotListFlags:
     *
     * Flags valid for virDomainSnapshotNum(),
     * virDomainSnapshotListNames(), virDomainSnapshotNumChildren(), and
     * virDomainSnapshotListChildrenNames(), virDomainListAllSnapshots(),
     * and virDomainSnapshotListAllChildren().  Note that the interpretation
     * of flag (1<<0) depends on which function it is passed to; but serves
     * to toggle the per-call default of whether the listing is shallow or
     * recursive.  Remaining bits come in groups; if all bits from a group are
     * 0, then that group is not used to filter results.  
     */
    static final class virDomainSnapshotListFlags {
        static final int VIR_DOMAIN_SNAPSHOT_LIST_ROOTS       = (1 << 0); /* Filter by snapshots
                                                            with no parents, when
                                                            listing a domain */
        static final int VIR_DOMAIN_SNAPSHOT_LIST_DESCENDANTS = (1 << 0); /* List all descendants,
                                                            not just children, when
                                                            listing a snapshot */

        /* For historical reasons, groups do not use contiguous bits.  */

        static final int VIR_DOMAIN_SNAPSHOT_LIST_LEAVES      = (1 << 2); /* Filter by snapshots
                                                            with no children */
        static final int VIR_DOMAIN_SNAPSHOT_LIST_NO_LEAVES   = (1 << 3); /* Filter by snapshots
                                                            that have children */
        static final int VIR_DOMAIN_SNAPSHOT_LIST_METADATA    = (1 << 1); /* Filter by snapshots
                                                            which have metadata */
        static final int VIR_DOMAIN_SNAPSHOT_LIST_NO_METADATA = (1 << 4); /* Filter by snapshots
                                                            with no metadata */
        static final int VIR_DOMAIN_SNAPSHOT_LIST_INACTIVE    = (1 << 5); /* Filter by snapshots
                                                            taken while guest was
                                                            shut off */
        static final int VIR_DOMAIN_SNAPSHOT_LIST_ACTIVE      = (1 << 6); /* Filter by snapshots
                                                            taken while guest was
                                                            active, and with
                                                            memory state */
        static final int VIR_DOMAIN_SNAPSHOT_LIST_DISK_ONLY   = (1 << 7); /* Filter by snapshots
                                                            taken while guest was
                                                            active, but without
                                                            memory state */
        static final int VIR_DOMAIN_SNAPSHOT_LIST_INTERNAL    = (1 << 8); /* Filter by snapshots
                                                            stored internal to
                                                            disk images */
        static final int VIR_DOMAIN_SNAPSHOT_LIST_EXTERNAL    = (1 << 9); /* Filter by snapshots
                                                            that use files external
                                                            to disk images */
    }
    

    static final class virDomainSnapshotRevertFlags {
        static final int VIR_DOMAIN_SNAPSHOT_REVERT_RUNNING = (1 << 0); /* Run after revert */
        static final int VIR_DOMAIN_SNAPSHOT_REVERT_PAUSED  = (1 << 1); /* Pause after revert */
        static final int VIR_DOMAIN_SNAPSHOT_REVERT_FORCE   = (1 << 2); /* Allow risky reverts */
    }


    /**
     *  Delete a snapshot
     */
    static final class virDomainSnapshotDeleteFlags {
        static final int VIR_DOMAIN_SNAPSHOT_DELETE_CHILDREN      = (1 << 0); /* Also delete children */
        static final int VIR_DOMAIN_SNAPSHOT_DELETE_METADATA_ONLY = (1 << 1); /* Delete just metadata */
        static final int VIR_DOMAIN_SNAPSHOT_DELETE_CHILDREN_ONLY = (1 << 2); /* Delete just children */
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
