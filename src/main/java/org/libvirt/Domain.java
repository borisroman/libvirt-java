package org.libvirt;

import static org.libvirt.ErrorHandler.processError;
import static org.libvirt.ErrorHandler.processErrorIfZero;
import static org.libvirt.Library.libvirt;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.libvirt.event.IOErrorListener;
import org.libvirt.event.LifecycleListener;
import org.libvirt.event.PMSuspendListener;
import org.libvirt.event.PMWakeupListener;
import org.libvirt.event.RebootListener;
import org.libvirt.jna.CString;
import org.libvirt.jna.DomainPointer;
import org.libvirt.jna.DomainSnapshotPointer;
import org.libvirt.jna.Libvirt;
import org.libvirt.jna.SizeT;
import org.libvirt.jna.virDomainBlockInfo;
import org.libvirt.jna.virDomainBlockStats;
import org.libvirt.jna.virDomainInfo;
import org.libvirt.jna.virDomainInterfaceStats;
import org.libvirt.jna.virDomainJobInfo;
import org.libvirt.jna.virDomainMemoryStats;
import org.libvirt.jna.virSchedParameter;
import org.libvirt.jna.virVcpuInfo;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.IntByReference;

/**
 * A virtual machine defined within libvirt.
 */
public class Domain {

    public static enum virDomainState {
        /**
         * no state
         */
        VIR_DOMAIN_NOSTATE,
        /**
         * the domain is running
         */
        VIR_DOMAIN_RUNNING,
        /**
         * the domain is blocked on resource
         */
        VIR_DOMAIN_BLOCKED,
        /**
         * the domain is paused by user
         */
        VIR_DOMAIN_PAUSED,
        /**
         * the domain is being shut down
         */
        VIR_DOMAIN_SHUTDOWN,
        /**
         * the domain is shut off
         */
        VIR_DOMAIN_SHUTOFF,
        /**
         * the domain is crashed
         */
        VIR_DOMAIN_CRASHED,
        /**
         * the domain is suspended by guest power management
         */
        VIR_DOMAIN_PMSUSPENDED
    }

    public static enum virDomainNostateReason {
        VIR_DOMAIN_NOSTATE_UNKNOWN
    }

    public static enum virDomainRunningReason {
        /**
         * unknown running state
         */
        VIR_DOMAIN_RUNNING_UNKNOWN,
        /**
         *  normal startup from boot
         */
        VIR_DOMAIN_RUNNING_BOOTED,
        /**
         *  migrated from another host
         */
        VIR_DOMAIN_RUNNING_MIGRATED,
        /**
         * restored from a state file
         */
        VIR_DOMAIN_RUNNING_RESTORED,
        /**
         * restored from snapshot
         */
        VIR_DOMAIN_RUNNING_FROM_SNAPSHOT,
        /**
         * returned from paused state
         */
        VIR_DOMAIN_RUNNING_UNPAUSED,
        /**
         * returned from migration
         */
        VIR_DOMAIN_RUNNING_MIGRATION_CANCELED,
        /**
         * returned from failed save process
         */
        VIR_DOMAIN_RUNNING_SAVE_CANCELED,
        /**
         * returned from pmsuspended due to wakeup event
         */
        VIR_DOMAIN_RUNNING_WAKEUP,
        /**
         * resumed from crashed
         */
        VIR_DOMAIN_RUNNING_CRASHED
    }

    public static enum virDomainBlockedReason {
        /**
         * the reason is unknown
         */
        VIR_DOMAIN_BLOCKED_UNKNOWN
    }

    public static enum virDomainPausedReason {
        /**
         * the reason is unknown
         */
        VIR_DOMAIN_PAUSED_UNKNOWN,
        /**
         * paused on user request
         */
        VIR_DOMAIN_PAUSED_USER,
        /**
         * paused for offline migration
         */
        VIR_DOMAIN_PAUSED_MIGRATION,
        /**
         * paused for save
         */
        VIR_DOMAIN_PAUSED_SAVE,
        /**
         * paused for offline core dump
         */
        VIR_DOMAIN_PAUSED_DUMP,
        /**
         * paused due to a disk I/O error
         */
        VIR_DOMAIN_PAUSED_IOERROR,
        /**
         * paused due to a watchdog event
         1*/
        VIR_DOMAIN_PAUSED_WATCHDOG,
        /**
         * paused after restoring from snapshot
         */
        VIR_DOMAIN_PAUSED_FROM_SNAPSHOT,
        /**
         * paused during shutdown process
         */
        VIR_DOMAIN_PAUSED_SHUTTING_DOWN,
        /**
         * paused while creating a snapshot
         */
        VIR_DOMAIN_PAUSED_SNAPSHOT,
        /**
         * paused due to a guest crash
         */
        VIR_DOMAIN_PAUSED_CRASHED,
        /**
         * the domain is being started
         */
        VIR_DOMAIN_PAUSED_STARTING_UP
    }

    public static enum virDomainShutdownReason {
        /**
         * the reason is unknown
         */
        VIR_DOMAIN_SHUTDOWN_UNKNOWN,
        /**
         * shutting down on user request
         */
        VIR_DOMAIN_SHUTDOWN_USER
    }

    public static enum virDomainShutoffReason {
        /**
         * the reason is unknown
         */
        VIR_DOMAIN_SHUTOFF_UNKNOWN,
        /**
         * normal shutdown
         */
        VIR_DOMAIN_SHUTOFF_SHUTDOWN,
        /**
         * forced poweroff
         */
        VIR_DOMAIN_SHUTOFF_DESTROYED,
        /**
         * domain crashed
         */
        VIR_DOMAIN_SHUTOFF_CRASHED,
        /**
         * migrated to another host
         */
        VIR_DOMAIN_SHUTOFF_MIGRATED,
        /**
         * saved to a file
         */
        VIR_DOMAIN_SHUTOFF_SAVED,
        /**
         * domain failed to start
         */
        VIR_DOMAIN_SHUTOFF_FAILED,
        /**
         * restored from a snapshot which was taken while domain was shutoff
         */
        VIR_DOMAIN_SHUTOFF_FROM_SNAPSHOT

    }

    public static enum virDomainCrashedReason {
        /**
         * crashed for unknown reason
         */
        VIR_DOMAIN_CRASHED_UNKNOWN,
        /**
         * domain panicked
         */
        VIR_DOMAIN_CRASHED_PANICKED
    }

    public static enum virDomainPMSuspendedReason {
        VIR_DOMAIN_PMSUSPENDED_UNKNOWN
    }

    public static enum virDomainPMSuspendedDiskReason {
        VIR_DOMAIN_PMSUSPENDED_DISK_UNKNOWN
    }

    public static enum virDomainControlState {
        /**
         * operational, ready to accept commands
         */
        VIR_DOMAIN_CONTROL_OK,
        /**
         * background job is running (can be
         * monitored by virDomainGetJobInfo); only
         * limited set of commands may be allowed
         */
        VIR_DOMAIN_CONTROL_JOB,
        /**
         * occupied by a running command
         */
        VIR_DOMAIN_CONTROL_OCCUPIED,
        /**
         * unusable, domain cannot be fully operated,
         * possible reason is provided in the details field
         */
        VIR_DOMAIN_CONTROL_ERROR,
    }

    public static enum virDomainControlErrorReason {
        /**
         * server didn't provide a reason
         */
        VIR_DOMAIN_CONTROL_ERROR_REASON_NONE,
        /**
         * unknown reason for the error
         */
        VIR_DOMAIN_CONTROL_ERROR_REASON_UNKNOWN,
        /**
         * monitor connection is broken
         */
        VIR_DOMAIN_CONTROL_ERROR_REASON_MONITOR,
        /**
         * error caused due to internal failure in libvirt
         */
        VIR_DOMAIN_CONTROL_ERROR_REASON_INTERNAL
    }

    public static final class virDomainModificationImpact {
        /**
         * Affect current domain state.
         */
        static final int VIR_DOMAIN_AFFECT_CURRENT = 0;
        /**
         * Affect running domain state.
         */
        static final int VIR_DOMAIN_AFFECT_LIVE    = (1 << 0);
        /**
         * Affect persistent domain state.
         */
        static final int VIR_DOMAIN_AFFECT_CONFIG  = (1 << 1);
        /**
         * 1 << 2 is reserved for virTypedParameterFlags
         */
    }

    public static final class virDomainCreateFlags {
        /**
         * Default behavior
         */
        static final int VIR_DOMAIN_NONE               = 0;
        /**
         * Launch guest in paused state
         */
        static final int VIR_DOMAIN_START_PAUSED       = (1 << 0);
        /**
         * Automatically kill guest when virConnectPtr is closed
         */
        static final int VIR_DOMAIN_START_AUTODESTROY  = (1 << 1);
        /**
         * Avoid file system cache pollution
         */
        static final int VIR_DOMAIN_START_BYPASS_CACHE = (1 << 2);
        /**
         * Boot, discarding any managed save
         */
        static final int VIR_DOMAIN_START_FORCE_BOOT   = (1 << 3);
        /**
         * Validate the XML document against schema
         */
        static final int VIR_DOMAIN_START_VALIDATE     = (1 << 4);
    }

    public static enum virDomainMemoryStatTags {
        /**
         * The total amount of data read from swap space (in kB).
         */
        VIR_DOMAIN_MEMORY_STAT_SWAP_IN,
        /**
         * The total amount of memory written out to swap space (in kB).
         */
        VIR_DOMAIN_MEMORY_STAT_SWAP_OUT,
        /**
         * Page faults occur when a process makes a valid access to virtual memory
         * that is not available.  When servicing the page fault, if disk IO is
         * required, it is considered a major fault.  If not, it is a minor fault.
         * These are expressed as the number of faults that have occurred.
         */
        VIR_DOMAIN_MEMORY_STAT_MAJOR_FAULT,
        VIR_DOMAIN_MEMORY_STAT_MINOR_FAULT,
        /**
         * The amount of memory left completely unused by the system.  Memory that
         * is available but used for reclaimable caches should NOT be reported as
         * free.  This value is expressed in kB.
         */
        VIR_DOMAIN_MEMORY_STAT_UNUSED,
        /**
         * The total amount of usable memory as seen by the domain.  This value
         * may be less than the amount of memory assigned to the domain if a
         * balloon driver is in use or if the guest OS does not initialize all
         * assigned pages.  This value is expressed in kB.
         */
        VIR_DOMAIN_MEMORY_STAT_AVAILABLE,
        /**
         * Current balloon value (in KB).
         */
        VIR_DOMAIN_MEMORY_STAT_ACTUAL_BALLOON,
        /**
         * Resident Set Size of the process running the domain. This value is in kB
         */
        VIR_DOMAIN_MEMORY_STAT_RSS,
        /**
         * The number of statistics supported by this version of the interface.
         * To add new statistics, add them to the enum and increase this value.
         */
        VIR_DOMAIN_MEMORY_STAT_NR
    }

    public static final class virDomainCoreDumpFlags {
        /**
         * crash after dump
         */
        static final int VIR_DUMP_CRASH        = (1 << 0);
        /**
         * live dump
         */
        static final int VIR_DUMP_LIVE         = (1 << 1);
        /**
         * avoid file system cache pollution
         */
        static final int VIR_DUMP_BYPASS_CACHE = (1 << 2);
        /**
         * reset domain after dump finishes
         */
        static final int VIR_DUMP_RESET        = (1 << 3);
        /**
         * use dump-guest-memory
         */
        static final int VIR_DUMP_MEMORY_ONLY  = (1 << 4);
    }


    public static enum virDomainCoreDumpFormat {
        /**
         * dump guest memory in raw format
         */
        VIR_DOMAIN_CORE_DUMP_FORMAT_RAW,
        /**
         * kdump-compressed format, with zlib compression
         */
        VIR_DOMAIN_CORE_DUMP_FORMAT_KDUMP_ZLIB,
        /**
         * kdump-compressed format, with lzo compression
         */
        VIR_DOMAIN_CORE_DUMP_FORMAT_KDUMP_LZO,
        /**
         * kdump-compressed format, with snappy compression
         */
        VIR_DOMAIN_CORE_DUMP_FORMAT_KDUMP_SNAPPY
    }

    public static final class virDomainMigrateFlags {
        /**
         * live migration
         */
        static final int VIR_MIGRATE_LIVE              = (1 << 0);
        /**
         * direct source -> dest host control channel
         */
        static final int VIR_MIGRATE_PEER2PEER         = (1 << 1);
        /**
         * Note the less-common spelling that we're stuck with:
         * VIR_MIGRATE_TUNNELLED should be VIR_MIGRATE_TUNNELED
         *
         * tunnel migration data over libvirtd connection
         */
        static final int VIR_MIGRATE_TUNNELLED         = (1 << 2);
        /**
         * persist the VM on the destination
         */
        static final int VIR_MIGRATE_PERSIST_DEST      = (1 << 3);
        /**
         * undefine the VM on the source
         */
        static final int VIR_MIGRATE_UNDEFINE_SOURCE   = (1 << 4);
        /**
         * pause on remote side
         */
        static final int VIR_MIGRATE_PAUSED            = (1 << 5);
        /**
         * migration with non-shared storage with full disk copy
         */
        static final int VIR_MIGRATE_NON_SHARED_DISK   = (1 << 6);
        /**
         * migration with non-shared storage with incremental copy
         * (same base image shared between source and destination)
         */
        static final int VIR_MIGRATE_NON_SHARED_INC    = (1 << 7);
        /**
         * protect for changing domain configuration through the
         * whole migration process; this will be used automatically
         * when supported
         */
        static final int VIR_MIGRATE_CHANGE_PROTECTION = (1 << 8);
        /**
         * force migration even if it is considered unsafe
         */
        static final int VIR_MIGRATE_UNSAFE            = (1 << 9);
    }

    public static final class virDomainShutdownFlagValues {
        /**
         * hypervisor choice
         */
        static final int VIR_DOMAIN_SHUTDOWN_DEFAULT        = 0;
        /**
         * Send ACPI event
         */
        static final int VIR_DOMAIN_SHUTDOWN_ACPI_POWER_BTN = (1 << 0);
        /**
         * Use guest agent
         */
        static final int VIR_DOMAIN_SHUTDOWN_GUEST_AGENT    = (1 << 1);
        /**
         * Use initctl
         */
        static final int VIR_DOMAIN_SHUTDOWN_INITCTL        = (1 << 2);
        /**
         * Send a signal
         */
        static final int VIR_DOMAIN_SHUTDOWN_SIGNAL         = (1 << 3);
        /**
         * Use paravirt guest control
         */
        static final int VIR_DOMAIN_SHUTDOWN_PARAVIRT       = (1 << 4);
    }

    public static final class virDomainRebootFlagValues {
        /**
         * hypervisor choice
         */
        static final int VIR_DOMAIN_REBOOT_DEFAULT        = 0;
        /**
         * Send ACPI event
         */
        static final int VIR_DOMAIN_REBOOT_ACPI_POWER_BTN = (1 << 0);
        /**
         * Use guest agent
         */
        static final int VIR_DOMAIN_REBOOT_GUEST_AGENT    = (1 << 1);
        /**
         * Use initctl
         */
        static final int VIR_DOMAIN_REBOOT_INITCTL        = (1 << 2);
        /**
         * Send a signal
         */
        static final int VIR_DOMAIN_REBOOT_SIGNAL         = (1 << 3);
        /**
         * Use paravirt guest control
         */
        static final int VIR_DOMAIN_REBOOT_PARAVIRT       = (1 << 4);
    }

    public static final class virDomainDestroyFlagsValues {
        /**
         * Default behavior - could lead to data loss!!
         */
        static final int VIR_DOMAIN_DESTROY_DEFAULT  = 0;
        /**
         * only SIGTERM, no SIGKILL
         */
        static final int VIR_DOMAIN_DESTROY_GRACEFUL = (1 << 0);
    }

    public static final class virDomainSaveRestoreFlags {
        /**
         * Avoid file system cache pollution
         */
        static final int VIR_DOMAIN_SAVE_BYPASS_CACHE = (1 << 0);
        /**
         * Favor running over paused
         */
        static final int VIR_DOMAIN_SAVE_RUNNING      = (1 << 1);
        /**
         * Favor paused over running
         */
        static final int VIR_DOMAIN_SAVE_PAUSED       = (1 << 2);
    }

    public static final class virDomainMemoryModFlags {
        /**
         * See virDomainModificationImpact for these flags.
         */
        static final int VIR_DOMAIN_MEM_CURRENT = virDomainModificationImpact.VIR_DOMAIN_AFFECT_CURRENT;
        static final int VIR_DOMAIN_MEM_LIVE    = virDomainModificationImpact.VIR_DOMAIN_AFFECT_LIVE;
        static final int VIR_DOMAIN_MEM_CONFIG  = virDomainModificationImpact.VIR_DOMAIN_AFFECT_CONFIG;

        /**
         * affect Max rather than current
         */
        static final int VIR_DOMAIN_MEM_MAXIMUM = (1 << 2);
    }

    public static enum virDomainNumatuneMemMode {
        VIR_DOMAIN_NUMATUNE_MEM_STRICT,
        VIR_DOMAIN_NUMATUNE_MEM_PREFERRED,
        VIR_DOMAIN_NUMATUNE_MEM_INTERLEAVE
    }

    public static enum virDomainMetadataType {
        /**
         * Operate on <description>
         */
        VIR_DOMAIN_METADATA_DESCRIPTION,
        /**
         * Operate on <title>
         */
        VIR_DOMAIN_METADATA_TITLE,
        /**
         * Operate on <metadata>
         */
        VIR_DOMAIN_METADATA_ELEMENT
    }

    public static final class virDomainXMLFlags {
        /**
         * dump security sensitive information too
         */
        static final int VIR_DOMAIN_XML_SECURE     = (1 << 0);
        /**
         * dump inactive domain information
         */
        static final int VIR_DOMAIN_XML_INACTIVE   = (1 << 1);
        /**
         * update guest CPU requirements according to host CPU
         */
        static final int VIR_DOMAIN_XML_UPDATE_CPU = (1 << 2);
        /**
         * dump XML suitable for migration
         */
        static final int VIR_DOMAIN_XML_MIGRATABLE = (1 << 3);
    }

    public static final class virDomainBlockResizeFlags {
        /**
         * size is in bytes instead of KiB
         */
        public static final int BYTES = (1 << 0);
    }

    public static final class virDomainMemoryFlags {
        /**
         * addresses are virtual addresses
         */
        public static final int VIR_MEMORY_VIRTUAL  = (1 << 0);
        /**
         * addresses are physical addresses
         */
        public static final int VIR_MEMORY_PHYSICAL = (1 << 1);
    }

    public static final class virDomainDefineFlags {
        /**
         * Validate the XML document against schema
         */
        public static final int VIR_DOMAIN_DEFINE_VALIDATE = (1 << 0);
    }

    public static final class virDomainUndefineFlagsValues {
        /**
         * Also remove any managed save
         */
        public static final int VIR_DOMAIN_UNDEFINE_MANAGED_SAVE       = (1 << 0);
        /**
         * If last use of domain, then also remove any snapshot metadata
         */
        public static final int VIR_DOMAIN_UNDEFINE_SNAPSHOTS_METADATA = (1 << 1);
        /**
         * Also remove any nvram file
         */
        public static final int VIR_DOMAIN_UNDEFINE_NVRAM              = (1 << 2);
    }

    public static final class virConnectListAllDomainsFlags {
        public static final int VIR_CONNECT_LIST_DOMAINS_ACTIVE         = (1 << 0);
        public static final int VIR_CONNECT_LIST_DOMAINS_INACTIVE       = (1 << 1);
        public static final int VIR_CONNECT_LIST_DOMAINS_PERSISTENT     = (1 << 2);
        public static final int VIR_CONNECT_LIST_DOMAINS_TRANSIENT      = (1 << 3);
        public static final int VIR_CONNECT_LIST_DOMAINS_RUNNING        = (1 << 4);
        public static final int VIR_CONNECT_LIST_DOMAINS_PAUSED         = (1 << 5);
        public static final int VIR_CONNECT_LIST_DOMAINS_SHUTOFF        = (1 << 6);
        public static final int VIR_CONNECT_LIST_DOMAINS_OTHER          = (1 << 7);
        public static final int VIR_CONNECT_LIST_DOMAINS_MANAGEDSAVE    = (1 << 8);
        public static final int VIR_CONNECT_LIST_DOMAINS_NO_MANAGEDSAVE = (1 << 9);
        public static final int VIR_CONNECT_LIST_DOMAINS_AUTOSTART      = (1 << 10);
        public static final int VIR_CONNECT_LIST_DOMAINS_NO_AUTOSTART   = (1 << 11);
        public static final int VIR_CONNECT_LIST_DOMAINS_HAS_SNAPSHOT   = (1 << 12);
        public static final int VIR_CONNECT_LIST_DOMAINS_NO_SNAPSHOT    = (1 << 13);
    }

    public static enum virVcpuState {
        /**
         * the virtual CPU is offline
         */
        VIR_VCPU_OFFLINE,
        /**
         * the virtual CPU is running
         */
        VIR_VCPU_RUNNING,
        /**
         * the virtual CPU is blocked on resource
         */
        VIR_VCPU_BLOCKED
    }

    public static final class virDomainVcpuFlags {
        /**
         * See virDomainModificationImpact for these flags.
         */
        public static final int VIR_DOMAIN_VCPU_CURRENT = virDomainModificationImpact.VIR_DOMAIN_AFFECT_CURRENT;
        public static final int VIR_DOMAIN_VCPU_LIVE    = virDomainModificationImpact.VIR_DOMAIN_AFFECT_LIVE;
        public static final int VIR_DOMAIN_VCPU_CONFIG  = virDomainModificationImpact.VIR_DOMAIN_AFFECT_CONFIG;

        /**
         * Max rather than current count
         */
        public static final int VIR_DOMAIN_VCPU_MAXIMUM = (1 << 2);
        /**
         * Modify state of the cpu in the guest
         */
        public static final int VIR_DOMAIN_VCPU_GUEST   = (1 << 3);
    }

    public static final class virDomainDeviceModifyFlags {
        /**
         * See virDomainModificationImpact for these flags.
         */
        public static final int VIR_DOMAIN_DEVICE_MODIFY_CURRENT = virDomainModificationImpact.VIR_DOMAIN_AFFECT_CURRENT;
        public static final int VIR_DOMAIN_DEVICE_MODIFY_LIVE    = virDomainModificationImpact.VIR_DOMAIN_AFFECT_LIVE;
        public static final int VIR_DOMAIN_DEVICE_MODIFY_CONFIG  = virDomainModificationImpact.VIR_DOMAIN_AFFECT_CONFIG;

        /**
         * Forcibly modify device (ex. force eject a cdrom)
         */
        public static final int VIR_DOMAIN_DEVICE_MODIFY_FORCE = (1 << 2);
    }

    public static final class virDomainStatsTypes {
        /**
         * return domain state
         */
        public static final int VIR_DOMAIN_STATS_STATE     = (1 << 0);
        /**
         * return domain CPU info
         */
        public static final int VIR_DOMAIN_STATS_CPU_TOTAL = (1 << 1);
        /**
         * return domain balloon info
         */
        public static final int VIR_DOMAIN_STATS_BALLOON   = (1 << 2);
        /**
         * return domain virtual CPU info
         */
        public static final int VIR_DOMAIN_STATS_VCPU      = (1 << 3);
        /**
         * return domain interfaces info
         */
        public static final int VIR_DOMAIN_STATS_INTERFACE = (1 << 4);
        /**
         * return domain block info
         */
        public static final int VIR_DOMAIN_STATS_BLOCK     = (1 << 5);
    }

    public static final class virConnectGetAllDomainStatsFlags {
        public static final int VIR_CONNECT_GET_ALL_DOMAINS_STATS_ACTIVE        = virConnectListAllDomainsFlags.VIR_CONNECT_LIST_DOMAINS_ACTIVE;
        public static final int VIR_CONNECT_GET_ALL_DOMAINS_STATS_INACTIVE      = virConnectListAllDomainsFlags.VIR_CONNECT_LIST_DOMAINS_INACTIVE;
        public static final int VIR_CONNECT_GET_ALL_DOMAINS_STATS_PERSISTENT    = virConnectListAllDomainsFlags.VIR_CONNECT_LIST_DOMAINS_PERSISTENT;
        public static final int VIR_CONNECT_GET_ALL_DOMAINS_STATS_TRANSIENT     = virConnectListAllDomainsFlags.VIR_CONNECT_LIST_DOMAINS_TRANSIENT;
        public static final int VIR_CONNECT_GET_ALL_DOMAINS_STATS_RUNNING       = virConnectListAllDomainsFlags.VIR_CONNECT_LIST_DOMAINS_RUNNING;
        public static final int VIR_CONNECT_GET_ALL_DOMAINS_STATS_PAUSED        = virConnectListAllDomainsFlags.VIR_CONNECT_LIST_DOMAINS_PAUSED;
        public static final int VIR_CONNECT_GET_ALL_DOMAINS_STATS_SHUTOFF       = virConnectListAllDomainsFlags.VIR_CONNECT_LIST_DOMAINS_SHUTOFF;
        public static final int VIR_CONNECT_GET_ALL_DOMAINS_STATS_OTHER         = virConnectListAllDomainsFlags.VIR_CONNECT_LIST_DOMAINS_OTHER;
        /**
         * include backing chain for block stats
         */
        public static final int VIR_CONNECT_GET_ALL_DOMAINS_STATS_BACKING       = (1 << 30);
        /**
         * enforce requested stats
         */
        public static final int VIR_CONNECT_GET_ALL_DOMAINS_STATS_ENFORCE_STATS = (1 << 31);
    }

    public static enum virDomainBlockJobType {
        VIR_DOMAIN_BLOCK_JOB_TYPE_UNKNOWN,
        /**
         * Block Pull (virDomainBlockPull, or virDomainBlockRebase without
         * flags), job ends on completion
         */
        VIR_DOMAIN_BLOCK_JOB_TYPE_PULL,
        /**
         * Block Copy (virDomainBlockCopy, or virDomainBlockRebase with
         * flags), job exists as long as mirroring is active
         */
        VIR_DOMAIN_BLOCK_JOB_TYPE_COPY,
        /**
         * Block Commit (virDomainBlockCommit without flags), job ends on
         * completion
         */
        VIR_DOMAIN_BLOCK_JOB_TYPE_COMMIT,
        /**
         * Active Block Commit (virDomainBlockCommit with flags), job
         * exists as long as sync is active
         */
        VIR_DOMAIN_BLOCK_JOB_TYPE_ACTIVE_COMMIT
    }

    public static final class virDomainBlockJobAbortFlags {
        public static final int VIR_DOMAIN_BLOCK_JOB_ABORT_ASYNC = (1 << 0);
        public static final int VIR_DOMAIN_BLOCK_JOB_ABORT_PIVOT = (1 << 1);
    }

    public static final class virDomainBlockJobInfoFlags {
        /**
         * bandwidth in bytes/s instead of MiB/s
         */
        public static final int VIR_DOMAIN_BLOCK_JOB_INFO_BANDWIDTH_BYTES = (1 << 0);
    }

    public static final class virDomainBlockJobSetSpeedFlags {
        /**
         * bandwidth in bytes/s instead of MiB/s
         */
        public static final int VIR_DOMAIN_BLOCK_JOB_SPEED_BANDWIDTH_BYTES = (1 << 0);
    }

    public static final class virDomainBlockPullFlags {
        /**
         * bandwidth in bytes/s instead of MiB/s
         */
        public static final int VIR_DOMAIN_BLOCK_PULL_BANDWIDTH_BYTES = (1 << 6);
    }

    public static final class virDomainBlockRebaseFlags {
        /**
         * Limit copy to top of source backing chain
         */
        public static final int VIR_DOMAIN_BLOCK_REBASE_SHALLOW         = (1 << 0);
        /**
         * Reuse existing external file for a copy
         */
        public static final int VIR_DOMAIN_BLOCK_REBASE_REUSE_EXT       = (1 << 1);
        /**
         * Make destination file raw
         */
        public static final int VIR_DOMAIN_BLOCK_REBASE_COPY_RAW        = (1 << 2);
        /**
         * Start a copy job
         */
        public static final int VIR_DOMAIN_BLOCK_REBASE_COPY            = (1 << 3);
        /**
         * Keep backing chain referenced using relative names
         */
        public static final int VIR_DOMAIN_BLOCK_REBASE_RELATIVE        = (1 << 4);
        /**
         * Treat destination as block device instead of file
         */
        public static final int VIR_DOMAIN_BLOCK_REBASE_COPY_DEV        = (1 << 5);
        /**
         * bandwidth in bytes/s instead of MiB/s
         */
        public static final int VIR_DOMAIN_BLOCK_REBASE_BANDWIDTH_BYTES = (1 << 6);
    }

    public static final class virDomainBlockCopyFlags {
        /**
         * Limit copy to top of source backing chain
         */
        public static final int VIR_DOMAIN_BLOCK_COPY_SHALLOW   = (1 << 0);
        /**
         * Reuse existing external file for a copy
         */
        public static final int VIR_DOMAIN_BLOCK_COPY_REUSE_EXT = (1 << 1);
    }

    public static final class virDomainBlockCommitFlags {
        /**
         * NULL base means next backing file, not whole chain
         */
        public static final int VIR_DOMAIN_BLOCK_COMMIT_SHALLOW         = (1 << 0);
        /**
         * Delete any files that are now invalid after their contents have been committed
         */
        public static final int VIR_DOMAIN_BLOCK_COMMIT_DELETE          = (1 << 1);
        /**
         * Allow a two-phase commit when top is the active layer
         */
        public static final int VIR_DOMAIN_BLOCK_COMMIT_ACTIVE          = (1 << 2);
        /**
         * keep the backing chain referenced using relative names
         */
        public static final int VIR_DOMAIN_BLOCK_COMMIT_RELATIVE        = (1 << 3);
        /**
         * bandwidth in bytes/s instead of MiB/s
         */
        public static final int VIR_DOMAIN_BLOCK_COMMIT_BANDWIDTH_BYTES = (1 << 4);
    }

    public static enum virDomainDiskErrorCode {
        /**
         * no error
         */
        VIR_DOMAIN_DISK_ERROR_NONE,
        /**
         * unspecified I/O error
         */
        VIR_DOMAIN_DISK_ERROR_UNSPEC,
        /**
         * no space left on the device
         */
        VIR_DOMAIN_DISK_ERROR_NO_SPACE
    }

    public static enum virKeycodeSet {
        VIR_KEYCODE_SET_LINUX,
        VIR_KEYCODE_SET_XT,
        VIR_KEYCODE_SET_ATSET1,
        VIR_KEYCODE_SET_ATSET2,
        VIR_KEYCODE_SET_ATSET3,
        VIR_KEYCODE_SET_OSX,
        VIR_KEYCODE_SET_XT_KBD,
        VIR_KEYCODE_SET_USB,
        VIR_KEYCODE_SET_WIN32,
        VIR_KEYCODE_SET_RFB
    }

    public static enum virDomainProcessSignal {
        /**
         * No constant in POSIX/Linux
         */
        VIR_DOMAIN_PROCESS_SIGNAL_NOP,
        /**
         * SIGHUP
         */
        VIR_DOMAIN_PROCESS_SIGNAL_HUP,
        /**
         * SIGINT
         */
        VIR_DOMAIN_PROCESS_SIGNAL_INT,
        /**
         * SIGQUIT
         */
        VIR_DOMAIN_PROCESS_SIGNAL_QUIT,
        /**
         * SIGILL
         */
        VIR_DOMAIN_PROCESS_SIGNAL_ILL,
        /**
         * SIGTRAP
         */
        VIR_DOMAIN_PROCESS_SIGNAL_TRAP,
        /**
         * SIGABRT
         */
        VIR_DOMAIN_PROCESS_SIGNAL_ABRT,
        /**
         * SIGBUS
         */
        VIR_DOMAIN_PROCESS_SIGNAL_BUS,
        /**
         * SIGFPE
         */
        VIR_DOMAIN_PROCESS_SIGNAL_FPE,
        /**
         * SIGKILL
         */
        VIR_DOMAIN_PROCESS_SIGNAL_KILL,
        /**
         * SIGUSR1
         */
        VIR_DOMAIN_PROCESS_SIGNAL_USR1,
        /**
         * SIGSEGV
         */
        VR_DOMAIN_PROCESS_SIGNAL_SEGV,
        /**
         * SIGUSR2
         */
        VIR_DOMAIN_PROCESS_SIGNAL_USR2,
        /**
         * SIGPIPE
         */
        VIR_DOMAIN_PROCESS_SIGNAL_PIPE,
        /**
         * SIGALRM
         */
        VIR_DOMAIN_PROCESS_SIGNAL_ALRM,
        /**
         * SIGTERM
         */
        VIR_DOMAIN_PROCESS_SIGNAL_TERM,
        /**
         * Not in POSIX (SIGSTKFLT on Linux )
         */
        VIR_DOMAIN_PROCESS_SIGNAL_STKFLT,
        /**
         * SIGCHLD
         */
        VIR_DOMAIN_PROCESS_SIGNAL_CHLD,
        /**
         * SIGCONT
         */
        VIR_DOMAIN_PROCESS_SIGNAL_CONT,
        /**
         * SIGSTOP
         */
        VIR_DOMAIN_PROCESS_SIGNAL_STOP,
        /**
         * SIGTSTP
         */
        VIR_DOMAIN_PROCESS_SIGNAL_TSTP,
        /**
         * SIGTTIN
         */
        VIR_DOMAIN_PROCESS_SIGNAL_TTIN,
        /**
         * SIGTTOU
         */
        VIR_DOMAIN_PROCESS_SIGNAL_TTOU,
        /**
         * SIGURG
         */
        VIR_DOMAIN_PROCESS_SIGNAL_URG,
        /**
         * SIGXCPU
         */
        VIR_DOMAIN_PROCESS_SIGNAL_XCPU,
        /**
         * SIGXFSZ
         */
        VIR_DOMAIN_PROCESS_SIGNAL_XFSZ,
        /**
         * SIGVTALRM
         */
        VIR_DOMAIN_PROCESS_SIGNAL_VTALRM,
        /**
         * SIGPROF
         */
        VIR_DOMAIN_PROCESS_SIGNAL_PROF,
        /**
         * Not in POSIX (SIGWINCH on Linux)
         */
        VIR_DOMAIN_PROCESS_SIGNAL_WINCH,
        /**
         * SIGPOLL (also known as SIGIO on Linux)
         */
        VIR_DOMAIN_PROCESS_SIGNAL_POLL,
        /**
         * Not in POSIX (SIGPWR on Linux)
         */
        VIR_DOMAIN_PROCESS_SIGNAL_PWR,
        /**
         * SIGSYS (also known as SIGUNUSED on Linux)
         */
        VIR_DOMAIN_PROCESS_SIGNAL_SYS,
        /**
         * SIGRTMIN
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT0,
        /**
         * SIGRTMIN + 1
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT1,
        /**
         * SIGRTMIN + 2
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT2,
        /**
         * SIGRTMIN + 3
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT3,
        /**
         * SIGRTMIN + 4
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT4,
        /**
         * SIGRTMIN + 5
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT5,
        /**
         * SIGRTMIN + 6
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT6,
        /**
         * SIGRTMIN + 7
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT7,
        /**
         * SIGRTMIN + 8
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT8,
        /**
         * SIGRTMIN + 9
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT9,
        /**
         * SIGRTMIN + 10
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT10,
        /**
         * SIGRTMIN + 11
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT11,
        /**
         * SIGRTMIN + 12
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT12,
        /**
         * SIGRTMIN + 13
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT13,
        /**
         * SIGRTMIN + 14
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT14,
        /**
         * SIGRTMIN + 15
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT15,
        /**
         * SIGRTMIN + 16
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT16,
        /**
         * SIGRTMIN + 17
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT17,
        /**
         * SIGRTMIN + 18
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT18,
        /**
         * SIGRTMIN + 19
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT19,
        /**
         * SIGRTMIN + 20
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT20,
        /**
         * SIGRTMIN + 21
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT21,
        /**
         * SIGRTMIN + 22
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT22,
        /**
         * SIGRTMIN + 23
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT23,
        /**
         * SIGRTMIN + 24
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT24,
        /**
         * SIGRTMIN + 25
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT25,
        /**
         * SIGRTMIN + 26
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT26,
        /**
         * SIGRTMIN + 27
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT27,
        /**
         * SIGRTMIN + 28
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT28,
        /**
         * SIGRTMIN + 29
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT29,
        /**
         * SIGRTMIN + 30
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT30,
        /**
         * SIGRTMIN + 31
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT31,
        /**
         * SIGRTMIN + 32 / SIGRTMAX
         */
        VIR_DOMAIN_PROCESS_SIGNAL_RT32
    }

    public static enum virDomainEventType {
        VIR_DOMAIN_EVENT_DEFINED,
        VIR_DOMAIN_EVENT_UNDEFINED,
        VIR_DOMAIN_EVENT_STARTED,
        VIR_DOMAIN_EVENT_SUSPENDED,
        VIR_DOMAIN_EVENT_RESUMED,
        VIR_DOMAIN_EVENT_STOPPED,
        VIR_DOMAIN_EVENT_SHUTDOWN,
        VIR_DOMAIN_EVENT_PMSUSPENDED,
        VIR_DOMAIN_EVENT_CRASHED
    }

    public static enum virDomainEventDefinedDetailType {
        /**
         * Newly created config file
         */
        VIR_DOMAIN_EVENT_DEFINED_ADDED,
        /**
         * Changed config file
         */
        VIR_DOMAIN_EVENT_DEFINED_UPDATED,
        /**
         * Domain was renamed
         */
        VIR_DOMAIN_EVENT_DEFINED_RENAMED
    }

    public static enum virDomainEventUndefinedDetailType {
        /**
         * Deleted the config file
         */
        VIR_DOMAIN_EVENT_UNDEFINED_REMOVED,
        /**
         * Domain was renamed
         */
        VIR_DOMAIN_EVENT_UNDEFINED_RENAMED
    }

    public static enum virDomainEventStartedDetailType {
        /**
         * Normal startup from boot
         */
        VIR_DOMAIN_EVENT_STARTED_BOOTED,
        /**
         * Incoming migration from another host
         */
        VIR_DOMAIN_EVENT_STARTED_MIGRATED,
        /**
         * Restored from a state file
         */
        VIR_DOMAIN_EVENT_STARTED_RESTORED,
        /**
         * Restored from snapshot
         */
        VIR_DOMAIN_EVENT_STARTED_FROM_SNAPSHOT,
        /**
         * Started due to wakeup event
         */
        VIR_DOMAIN_EVENT_STARTED_WAKEUP
    }

    public static enum virDomainEventSuspendedDetailType {
        /**
         * Normal suspend due to admin pause
         */
        VIR_DOMAIN_EVENT_SUSPENDED_PAUSED,
        /**
         * Suspended for offline migration
         */
        VIR_DOMAIN_EVENT_SUSPENDED_MIGRATED,
        /**
         * Suspended due to a disk I/O error
         */
        VIR_DOMAIN_EVENT_SUSPENDED_IOERROR,
        /**
         * Suspended due to a watchdog firing
         */
        VIR_DOMAIN_EVENT_SUSPENDED_WATCHDOG,
        /**
         * Restored from paused state file
         */
        VIR_DOMAIN_EVENT_SUSPENDED_RESTORED,
        /**
         * Restored from paused snapshot
         */
        VIR_DOMAIN_EVENT_SUSPENDED_FROM_SNAPSHOT,
        /**
         * suspended after failure during libvirt API call
         */
        VIR_DOMAIN_EVENT_SUSPENDED_API_ERROR
    }

    public static enum virDomainEventResumedDetailType {
        /**
         * Normal resume due to admin unpause
         */
        VIR_DOMAIN_EVENT_RESUMED_UNPAUSED,
        /**
         * Resumed for completion of migration
         */
        VIR_DOMAIN_EVENT_RESUMED_MIGRATED,
        /**
         * Resumed from snapshot
         */
        VIR_DOMAIN_EVENT_RESUMED_FROM_SNAPSHOT
    }

    public static enum virDomainEventStoppedDetailType {
        /**
         * Normal shutdown
         */
        VIR_DOMAIN_EVENT_STOPPED_SHUTDOWN,
        /**
         * Forced poweroff from host
         */
        VIR_DOMAIN_EVENT_STOPPED_DESTROYED,
        /**
         * Guest crashed
         */
        VIR_DOMAIN_EVENT_STOPPED_CRASHED,
        /**
         * Migrated off to another host
         */
        VIR_DOMAIN_EVENT_STOPPED_MIGRATED,
        /**
         * Saved to a state file
         */
        VIR_DOMAIN_EVENT_STOPPED_SAVED,
        /**
         * Host emulator/mgmt failed
         */
        VIR_DOMAIN_EVENT_STOPPED_FAILED,
        /**
         * offline snapshot loaded
         */
        VIR_DOMAIN_EVENT_STOPPED_FROM_SNAPSHOT
    }

    public static enum virDomainEventShutdownDetailType {
        /**
         * Guest finished shutdown sequence
         */
        VIR_DOMAIN_EVENT_SHUTDOWN_FINISHED
    }

    public static enum virDomainEventPMSuspendedDetailType {
        /**
         * Guest was PM suspended to memory
         */
        VIR_DOMAIN_EVENT_PMSUSPENDED_MEMORY,
        /**
         * Guest was PM suspended to disk
         */
        VIR_DOMAIN_EVENT_PMSUSPENDED_DISK
    }

    public static enum virDomainEventCrashedDetailType {
        /**
         * Guest was panicked
         */
        VIR_DOMAIN_EVENT_CRASHED_PANICKED
    }

    public static enum virDomainJobType {
        /**
         * No job is active
         */
        VIR_DOMAIN_JOB_NONE,
        /**
         * Job with a finite completion time
         */
        VIR_DOMAIN_JOB_BOUNDED,
        /**
         * Job without a finite completion time
         */
        VIR_DOMAIN_JOB_UNBOUNDED,
        /**
         * Job has finished, but isn't cleaned up
         */
        VIR_DOMAIN_JOB_COMPLETED,
        /**
         * Job hit error, but isn't cleaned up
         */
        VIR_DOMAIN_JOB_FAILED,
        /**
         * Job was aborted, but isn't cleaned up
         */
        VIR_DOMAIN_JOB_CANCELLED
    }

    public static final class virDomainGetJobStatsFlags {
        /**
         * return stats of a recently completed job
         */
        public static final int VIR_DOMAIN_JOB_STATS_COMPLETED = (1 << 0);
    }


    public static enum virDomainEventWatchdogAction {
        /**
         * No action, watchdog ignored
         */
        VIR_DOMAIN_EVENT_WATCHDOG_NONE,
        /**
         * Guest CPUs are paused
         */
        VIR_DOMAIN_EVENT_WATCHDOG_PAUSE,
        /**
         * Guest CPUs are reset
         */
        VIR_DOMAIN_EVENT_WATCHDOG_RESET,
        /**
         * Guest is forcibly powered off
         */
        VIR_DOMAIN_EVENT_WATCHDOG_POWEROFF,
        /**
         * Guest is requested to gracefully shutdown
         */
        VIR_DOMAIN_EVENT_WATCHDOG_SHUTDOWN,
        /**
         * No action, a debug message logged
         */
        VIR_DOMAIN_EVENT_WATCHDOG_DEBUG,
        /**
         * Inject a non-maskable interrupt into guest
         */
        VIR_DOMAIN_EVENT_WATCHDOG_INJECTNMI
    }

    public static enum virDomainEventIOErrorAction {
        /**
         * No action, IO error ignored
         */
        VIR_DOMAIN_EVENT_IO_ERROR_NONE,
        /**
         * Guest CPUs are paused
         */
        VIR_DOMAIN_EVENT_IO_ERROR_PAUSE,
        /**
         * IO error reported to guest OS
         */
        VIR_DOMAIN_EVENT_IO_ERROR_REPORT
    }

    public static enum virDomainEventGraphicsPhase {
        /**
         * Initial socket connection established
         */
        VIR_DOMAIN_EVENT_GRAPHICS_CONNECT,
        /**
         * Authentication & setup completed
         */
        VIR_DOMAIN_EVENT_GRAPHICS_INITIALIZE,
        /**
         * Final socket disconnection
         */
        VIR_DOMAIN_EVENT_GRAPHICS_DISCONNECT
    }

    public static enum virDomainEventGraphicsAddressType {
        /**
         * IPv4 address
         */
        VIR_DOMAIN_EVENT_GRAPHICS_ADDRESS_IPV4,
        /**
         * IPv6 address
         */
        VIR_DOMAIN_EVENT_GRAPHICS_ADDRESS_IPV6,
        /**
         * UNIX socket path
         */
        VIR_DOMAIN_EVENT_GRAPHICS_ADDRESS_UNIX
    }

    public static enum virConnectDomainEventBlockJobStatus {
        VIR_DOMAIN_BLOCK_JOB_COMPLETED,
        VIR_DOMAIN_BLOCK_JOB_FAILED,
        VIR_DOMAIN_BLOCK_JOB_CANCELED,
        VIR_DOMAIN_BLOCK_JOB_READY
    }

    public static enum virConnectDomainEventDiskChangeReason {
        /**
         * oldSrcPath is set
         */
        VIR_DOMAIN_EVENT_DISK_CHANGE_MISSING_ON_START,
        VIR_DOMAIN_EVENT_DISK_DROP_MISSING_ON_START
    }

    public static enum virDomainEventTrayChangeReason {
        VIR_DOMAIN_EVENT_TRAY_CHANGE_OPEN,
        VIR_DOMAIN_EVENT_TRAY_CHANGE_CLOSE,
    }

    public static enum virConnectDomainEventAgentLifecycleState {
        /**
         * agent connected
         */
        VIR_CONNECT_DOMAIN_EVENT_AGENT_LIFECYCLE_STATE_CONNECTED,
        /**
         * agent disconnected
         */
        VIR_CONNECT_DOMAIN_EVENT_AGENT_LIFECYCLE_STATE_DISCONNECTED
    }

    public static enum virConnectDomainEventAgentLifecycleReason {
        /**
         * unknown state change reason
         */
        VIR_CONNECT_DOMAIN_EVENT_AGENT_LIFECYCLE_REASON_UNKNOWN,
        /**
         * state changed due to domain start
         */
        VIR_CONNECT_DOMAIN_EVENT_AGENT_LIFECYCLE_REASON_DOMAIN_STARTED,
        /**
         * channel state changed
         */
        VIR_CONNECT_DOMAIN_EVENT_AGENT_LIFECYCLE_REASON_CHANNEL

    }

    public static enum virDomainEventID {
        /**
         * virConnectDomainEventCallback
         */
        VIR_DOMAIN_EVENT_ID_LIFECYCLE,
        /**
         * virConnectDomainEventGenericCallback
         */
        VIR_DOMAIN_EVENT_ID_REBOOT,
        /**
         * virConnectDomainEventRTCChangeCallback
         */
        VIR_DOMAIN_EVENT_ID_RTC_CHANGE,
        /**
         * virConnectDomainEventWatchdogCallback
         */
        VIR_DOMAIN_EVENT_ID_WATCHDOG,
        /**
         * virConnectDomainEventIOErrorCallback
         */
        VIR_DOMAIN_EVENT_ID_IO_ERROR,
        /**
         * virConnectDomainEventGraphicsCallback
         */
        VIR_DOMAIN_EVENT_ID_GRAPHICS,
        /**
         * virConnectDomainEventIOErrorReasonCallback
         */
        VIR_DOMAIN_EVENT_ID_IO_ERROR_REASON,
        /**
         * virConnectDomainEventGenericCallback
         */
        VIR_DOMAIN_EVENT_ID_CONTROL_ERROR,
        /**
         * virConnectDomainEventBlockJobCallback
         */
        VIR_DOMAIN_EVENT_ID_BLOCK_JOB,
        /**
         * virConnectDomainEventDiskChangeCallback
         */
        VIR_DOMAIN_EVENT_ID_DISK_CHANGE,
        /**
         * virConnectDomainEventTrayChangeCallback
         */
        VIR_DOMAIN_EVENT_ID_TRAY_CHANGE,
        /**
         * virConnectDomainEventPMWakeupCallback
         */
        VIR_DOMAIN_EVENT_ID_PMWAKEUP,
        /**
         * virConnectDomainEventPMSuspendCallback
         */
        VIR_DOMAIN_EVENT_ID_PMSUSPEND,
        /**
         * virConnectDomainEventBalloonChangeCallback
         */
        VIR_DOMAIN_EVENT_ID_BALLOON_CHANGE,
        /**
         * virConnectDomainEventPMSuspendDiskCallback
         */
        VIR_DOMAIN_EVENT_ID_PMSUSPEND_DISK,
        /**
         * virConnectDomainEventDeviceRemovedCallback
         */
        VIR_DOMAIN_EVENT_ID_DEVICE_REMOVED,
        /**
         * virConnectDomainEventBlockJobCallback
         */
        VIR_DOMAIN_EVENT_ID_BLOCK_JOB_2,
        /**
         * virConnectDomainEventTunableCallback
         */
        VIR_DOMAIN_EVENT_ID_TUNABLE,
        /**
         * virConnectDomainEventAgentLifecycleCallback
         */
        VIR_DOMAIN_EVENT_ID_AGENT_LIFECYCLE,
        /**
         * virConnectDomainEventDeviceAddedCallback
         */
        VIR_DOMAIN_EVENT_ID_DEVICE_ADDED
    }

    public static final class virDomainConsoleFlags {
        /**
         * abort a (possibly) active console connection to force a new connection
         */
        public static final int VIR_DOMAIN_CONSOLE_FORCE = (1 << 0);
        /**
         * check if the console driver supports safe console operations
         */
        public static final int VIR_DOMAIN_CONSOLE_SAFE  = (1 << 1);
    }

    public static final class virDomainChannelFlags {
        /**
         * abort a (possibly) active channel connection to force a new connection
         */
        public static final int VIR_DOMAIN_CHANNEL_FORCE = (1 << 0);
    }

    public static final class virDomainOpenGraphicsFlags {
        public static final int VIR_DOMAIN_OPEN_GRAPHICS_SKIPAUTH = (1 << 0);
    }

    public static final class virDomainSetTimeFlags {
        /**
         * Re-sync domain time from domain's RTC
         */
        public static final int VIR_DOMAIN_TIME_SYNC = (1 << 0);
    }

    public static enum virSchedParameterType {
        VIR_DOMAIN_SCHED_FIELD_INT,
        VIR_DOMAIN_SCHED_FIELD_UINT,
        VIR_DOMAIN_SCHED_FIELD_LLONG,
        VIR_DOMAIN_SCHED_FIELD_ULLONG,
        VIR_DOMAIN_SCHED_FIELD_DOUBLE,
        VIR_DOMAIN_SCHED_FIELD_BOOLEAN
    }

    public static enum virBlkioParameterType {
        VIR_DOMAIN_BLKIO_PARAM_INT,
        VIR_DOMAIN_BLKIO_PARAM_UINT,
        VIR_DOMAIN_BLKIO_PARAM_LLONG,
        VIR_DOMAIN_BLKIO_PARAM_ULLONG,
        VIR_DOMAIN_BLKIO_PARAM_DOUBLE,
        VIR_DOMAIN_BLKIO_PARAM_BOOLEAN,
    }

    public static enum virMemoryParameterType {
        VIR_DOMAIN_MEMORY_PARAM_INT,
        VIR_DOMAIN_MEMORY_PARAM_UINT,
        VIR_DOMAIN_MEMORY_PARAM_LLONG,
        VIR_DOMAIN_MEMORY_PARAM_ULLONG,
        VIR_DOMAIN_MEMORY_PARAM_DOUBLE,
        VIR_DOMAIN_MEMORY_PARAM_BOOLEAN
    }

    public static enum virDomainInterfaceAddressesSource {
        /**
         * Parse DHCP lease file
         */
        VIR_DOMAIN_INTERFACE_ADDRESSES_SRC_LEASE,
        /**
         * Query qemu guest agent
         */
        VIR_DOMAIN_INTERFACE_ADDRESSES_SRC_AGENT
    }

    public static final class virDomainSetUserPasswordFlags {
        /**
         * the password is already encrypted
         */
        public static final int VIR_DOMAIN_PASSWORD_ENCRYPTED = (1 << 0);
    }


    /**
     * the native virDomainPtr.
     */
    DomainPointer VDP;

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((virConnect == null) ? 0 : virConnect.hashCode());
        try {
            result = prime * result + ((VDP == null) ? 0 : Arrays.hashCode(this.getUUID()));
        } catch (LibvirtException e) {
            throw new RuntimeException("libvirt error testing domain equality", e);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Domain))
            return false;
        Domain other = (Domain) obj;

        // return false when this domain belongs to
        // a different hypervisor than the other
        if (!this.virConnect.equals(other.virConnect))
            return false;

        if (VDP == null) return (other.VDP == null);

        if (VDP.equals(other.VDP)) return true;

        try {
            return Arrays.equals(getUUID(), other.getUUID());
        } catch (LibvirtException e) {
            throw new RuntimeException("libvirt error testing domain equality", e);
        }
    }

    /**
     * The Connect Object that represents the Hypervisor of this Domain
     */
    private final Connect virConnect;

    /**
     * Constructs a Domain object from a known native DomainPointer, and a
     * Connect object.
     *
     * @param virConnect
     *            the Domain's hypervisor
     * @param VDP
     *            the native virDomainPtr
     */
    Domain(Connect virConnect, DomainPointer VDP) {
        assert virConnect != null;

        this.virConnect = virConnect;
        this.VDP = VDP;
    }

    /**
     * Constructs a new Domain object increasing the reference count
     * on the DomainPointer.
     * <p>
     * This factory method is mostly useful with callback functions,
     * since the virDomainPtr passed is only valid for the duration of
     * execution of the callback.
     */
    static Domain constructIncRef(Connect virConnect, DomainPointer VDP) throws LibvirtException {
        processError(libvirt.virDomainRef(VDP));

        return new Domain(virConnect, VDP);
    }

    /**
     * Requests that the current background job be aborted at the soonest
     * opportunity. This will block until the job has either completed, or
     * aborted.
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainAbortJob">Libvirt
     *      Documentation</a>
     * @return <em>ignore</em> (always 0)
     * @throws LibvirtException
     */
    public int abortJob() throws LibvirtException {
        return processError(libvirt.virDomainAbortJob(VDP));
    }

    /**
     * Creates a virtual device attachment to backend.
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainAttachDevice">Libvirt
     *      Documentation</a>
     * @param xmlDesc
     *            XML description of one device
     * @throws LibvirtException
     */
    public void attachDevice(String xmlDesc) throws LibvirtException {
        processError(libvirt.virDomainAttachDevice(VDP, xmlDesc));
    }

    /**
     * Creates a virtual device attachment to backend.
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainAttachDeviceFlags">Libvirt
     *      Documentation</a>
     * @param xmlDesc
     *            XML description of one device
     * @param flags
     *            the an OR'ed set of virDomainDeviceModifyFlags
     * @throws LibvirtException
     */
    public void attachDeviceFlags(String xmlDesc, int flags) throws LibvirtException {
        processError(libvirt.virDomainAttachDeviceFlags(VDP, xmlDesc, flags));
    }

    /**
     * This function returns block device (disk) stats for block devices
     * attached to the domain.
     *
     * @param path
     *            the path to the block device
     * @return the info
     * @throws LibvirtException
     */
    public DomainBlockInfo blockInfo(String path) throws LibvirtException {
        virDomainBlockInfo info = new virDomainBlockInfo();
        processError(libvirt.virDomainGetBlockInfo(VDP, path, info, 0));
        return new DomainBlockInfo(info);
    }

    /**
     * Read the contents of a domain's disk device.
     * <p>
     * Typical uses for this are to determine if the domain has
     * written a Master Boot Record (indicating that the domain has
     * completed installation), or to try to work out the state of the
     * domain's filesystems.
     * <p>
     * (Note that in the local case you might try to open the block
     * device or file directly, but that won't work in the remote
     * case, nor if you don't have sufficient permission. Hence the
     * need for this call).
     * <p>
     * The disk parameter can either be an unambiguous source name of
     * the block device (the {@code <source file='...'/>} sub-element,
     * such as "/path/to/image"), or <em>(since 0.9.5)</em> the device
     * target shorthand (the {@code <target dev='...'/>} sub-element,
     * such as "xvda").
     * <p>
     * Valid names can be found by calling {@link #getXMLDesc} and
     * inspecting elements within {@code //domain/devices/disk}.
     * <p>
     * This method always reads the number of bytes remaining in the
     * buffer, that is, {@code buffer.remaining()} at the moment this
     * method is invoked. Upon return the buffer's position will be
     * equal to the limit, the limit itself will not have changed.
     *
     * @param  disk    the path to the block device, or device shorthand
     * @param  offset  the offset within block device
     * @param  buffer  the buffer receiving the data
     */
    public void blockPeek(String disk, long offset, ByteBuffer buffer) throws LibvirtException {
        SizeT size = new SizeT();

        // older libvirt has a limitation on the size of data
        // transferred per request in the remote driver. So, split
        // larger requests into 64K blocks.

        do {
            final int req = Math.min(65536, buffer.remaining());

            size.setValue(req);

            processError(libvirt.virDomainBlockPeek(this.VDP, disk, offset, size, buffer, 0));

            buffer.position(buffer.position() + req);
        } while (buffer.hasRemaining());

        assert buffer.position() == buffer.limit();
    }

    /**
     * Returns block device (disk) stats for block devices attached to this
     * domain. The path parameter is the name of the block device. Get this by
     * calling virDomainGetXMLDesc and finding the <target dev='...'> attribute
     * within //domain/devices/disk. (For example, "xvda"). Domains may have
     * more than one block device. To get stats for each you should make
     * multiple calls to this function. Individual fields within the
     * DomainBlockStats object may be returned as -1, which indicates that the
     * hypervisor does not support that particular statistic.
     *
     * @param path
     *            path to the block device
     * @return the statistics in a DomainBlockStats object
     * @throws LibvirtException
     */
    public DomainBlockStats blockStats(String path) throws LibvirtException {
        virDomainBlockStats stats = new virDomainBlockStats();
        processError(libvirt.virDomainBlockStats(VDP, path, stats, new SizeT(stats.size())));
        return new DomainBlockStats(stats);
    }

    /**
     * Resize a block device of domain while the domain is running.
     *
     * @param disk
     *           path to the block image, or shorthand (like vda)
     * @param size
     *           the new size of the block devices
     * @param flags
     *           bitwise OR'ed values of {@link BlockResizeFlags}
     * @throws LibvirtException
     */
    public void blockResize(String disk, long size, int flags) throws LibvirtException {
        processError(libvirt.virDomainBlockResize(VDP, disk, size, flags));
    }


    /**
     * Dumps the core of this domain on a given file for analysis. Note that for
     * remote Xen Daemon the file path will be interpreted in the remote host.
     *
     * @param to
     *            path for the core file
     * @param flags
     *            extra flags, currently unused
     * @throws LibvirtException
     */
    public void coreDump(String to, int flags) throws LibvirtException {
        processError(libvirt.virDomainCoreDump(VDP, to, flags));
    }

    /**
     * It returns the length (in bytes) required to store the complete CPU map
     * between a single virtual & all physical CPUs of a domain.
     */
    public int cpuMapLength(int maxCpus) {
        return (((maxCpus) + 7) / 8);
    }

    /**
     * Launches this defined domain. If the call succeed the domain moves from
     * the defined to the running domains pools.
     *
     * @return <em>ignore</em> (always 0)
     * @throws LibvirtException
     */
    public int create() throws LibvirtException {
        return processError(libvirt.virDomainCreate(VDP));
    }

    /**
     * Launches this defined domain with the provide flags.
     * If the call succeed the domain moves from
     * the defined to the running domains pools.
     *
     * @return <em>ignore</em> (always 0)
     * @throws LibvirtException
     */
    public int create(int flags) throws LibvirtException {
        return processError(libvirt.virDomainCreateWithFlags(VDP, flags));
    }

    /**
     * Destroys this domain object. The running instance is shutdown if not down
     * already and all resources used by it are given back to the hypervisor.
     * The data structure is freed and should not be used thereafter if the call
     * does not return an error. This function may requires priviledged access
     *
     * @throws LibvirtException
     */
    public void destroy() throws LibvirtException {
        processError(libvirt.virDomainDestroy(VDP));
    }

    /**
     * Destroys a virtual device attachment to backend.
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainDetachDevice">Libvirt
     *      Documentation</a>
     * @param xmlDesc
     *            XML description of one device
     * @throws LibvirtException
     */
    public void detachDevice(String xmlDesc) throws LibvirtException {
        processError(libvirt.virDomainDetachDevice(VDP, xmlDesc));
    }

    /**
     * Destroys a virtual device attachment to backend.
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainDetachDeviceFlags">Libvirt
     *      Documentation</a>
     * @param xmlDesc
     *            XML description of one device
     * @throws LibvirtException
     */
    public void detachDeviceFlags(String xmlDesc, int flags) throws LibvirtException {
        processError(libvirt.virDomainDetachDeviceFlags(VDP, xmlDesc, flags));
    }

    @Override
    protected void finalize() throws LibvirtException {
        free();
    }

    /**
     * Frees this domain object. The running instance is kept alive. The data
     * structure is freed and should not be used thereafter.
     *
     * @throws LibvirtException
     * @return number of references left (>= 0)
     */
    public int free() throws LibvirtException {
        int success = 0;
        if (VDP != null) {
            success = processError(libvirt.virDomainFree(VDP));
            VDP = null;
        }

        return success;
    }

    /**
     * Provides a boolean value indicating whether the domain is configured to
     * be automatically started when the host machine boots.
     *
     * @return the result
     * @throws LibvirtException
     */
    public boolean getAutostart() throws LibvirtException {
        IntByReference autoStart = new IntByReference();
        processError(libvirt.virDomainGetAutostart(VDP, autoStart));
        return autoStart.getValue() != 0 ? true : false;
    }

    /**
     * Provides the connection object associated with a domain.
     *
     * @return the Connect object
     */
    public Connect getConnect() {
        return virConnect;
    }

    /**
     * Gets the hypervisor ID number for the domain
     *
     * @return the hypervisor ID
     * @throws LibvirtException
     */
    public int getID() throws LibvirtException {
        return processError(libvirt.virDomainGetID(VDP));
    }

    /**
     * Extract information about a domain. Note that if the connection used to
     * get the domain is limited only a partial set of the information can be
     * extracted.
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainGetInfo">Libvirt
     *      Documentation</a>
     *
     * @return a DomainInfo object describing this domain
     * @throws LibvirtException
     */
    public DomainInfo getInfo() throws LibvirtException {
        virDomainInfo vInfo = new virDomainInfo();
        processError(libvirt.virDomainGetInfo(VDP, vInfo));
        return new DomainInfo(vInfo);
    }

    /**
     * Extract information about progress of a background job on a domain. Will
     * return an error if the domain is not active.
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainGetJobInfo">Libvirt
     *      Documentation</a>
     * @return a DomainJobInfo object describing this domain
     * @throws LibvirtException
     */
    public DomainJobInfo getJobInfo() throws LibvirtException {
        virDomainJobInfo vInfo = new virDomainJobInfo();
        processError(libvirt.virDomainGetJobInfo(VDP, vInfo));
        return new DomainJobInfo(vInfo);
    }

    /**
     * Retrieve the maximum amount of physical memory allocated to a domain.
     *
     * @return the memory in kilobytes
     * @throws LibvirtException
     */
    public long getMaxMemory() throws LibvirtException {
        // the memory size in kibibytes (blocks of 1024 bytes), or 0 in case of error.
        NativeLong returnValue = libvirt.virDomainGetMaxMemory(VDP);
        return processErrorIfZero(returnValue.longValue());
    }

    /**
     * Provides the maximum number of virtual CPUs supported for the guest VM.
     * If the guest is inactive, this is basically the same as
     * virConnectGetMaxVcpus. If the guest is running this will reflect the
     * maximum number of virtual CPUs the guest was booted with.
     *
     * @return the number of VCPUs
     * @throws LibvirtException
     */
    public int getMaxVcpus() throws LibvirtException {
        return processError(libvirt.virDomainGetMaxVcpus(VDP));
    }

    /**
     * Gets the public name for this domain
     *
     * @return the name, null if there is no name
     * @throws LibvirtException <em>never</em>
     */
    public String getName() throws LibvirtException {
        return libvirt.virDomainGetName(VDP);
    }

    /**
     * Gets the type of domain operation system.
     *
     * @return the type
     * @throws LibvirtException
     */
    public String getOSType() throws LibvirtException {
        return processError(libvirt.virDomainGetOSType(VDP)).toString();
    }

    /**
     * Gets the scheduler parameters.
     *
     * @return an array of SchedParameter objects
     * @throws LibvirtException
     */
    public SchedParameter[] getSchedulerParameters() throws LibvirtException {
        IntByReference nParams = new IntByReference();
        processError(libvirt.virDomainGetSchedulerType(VDP, nParams));

        int n = nParams.getValue();

        if (n > 0) {
            virSchedParameter[] nativeParams = new virSchedParameter[n];

            processError(libvirt.virDomainGetSchedulerParameters(VDP, nativeParams, nParams));
            n = nParams.getValue();

            SchedParameter[] returnValue = new SchedParameter[n];

            for (int x = 0; x < n; x++) {
                returnValue[x] = SchedParameter.create(nativeParams[x]);
            }
            return returnValue;
        } else {
            return new SchedParameter[] {};
        }
    }

    // getSchedulerType
    // We don't expose the nparams return value, it's only needed for the
    // SchedulerParameters allocations,
    // but we handle that in getSchedulerParameters internally.
    /**
     * Gets the scheduler type.
     *
     * @return the type of the scheduler
     * @throws LibvirtException
     */
    public String getSchedulerType() throws LibvirtException {
        return processError(libvirt.virDomainGetSchedulerType(VDP, null)).toString();
    }

    /**
     * Get the security label of an active domain.
     *
     * @return the SecurityLabel or {@code null} if the domain is not
     *         running under a security model
     * @throws LibvirtException
     */
    public SecurityLabel getSecurityLabel() throws LibvirtException {
        Libvirt.SecurityLabel seclabel = new Libvirt.SecurityLabel();

        processError(libvirt.virDomainGetSecurityLabel(this.VDP, seclabel));

        if (seclabel.label[0] == 0)
            return null;
        else
            return new SecurityLabel(seclabel);
    }

    /**
     * Get the UUID for this domain.
     *
     * @return the UUID as an unpacked int array
     * @throws LibvirtException
     * @see <a href="http://www.ietf.org/rfc/rfc4122.txt">rfc4122</a>
     */
    public int[] getUUID() throws LibvirtException {
        byte[] bytes = new byte[Libvirt.VIR_UUID_BUFLEN];
        processError(libvirt.virDomainGetUUID(VDP, bytes));
        return Connect.convertUUIDBytes(bytes);
    }

    /**
     * Gets the UUID for this domain as string.
     *
     * @return the UUID in canonical String format
     * @throws LibvirtException
     * @see <a href="http://www.ietf.org/rfc/rfc4122.txt">rfc4122</a>
     */
    public String getUUIDString() throws LibvirtException {
        byte[] bytes = new byte[Libvirt.VIR_UUID_STRING_BUFLEN];
        processError(libvirt.virDomainGetUUIDString(VDP, bytes));
        return Native.toString(bytes);
    }

    /**
     * Returns the cpumaps for this domain Only the lower 8 bits of each int in
     * the array contain information.
     *
     * @return a bitmap of real CPUs for all vcpus of this domain
     * @throws LibvirtException
     */
    public int[] getVcpusCpuMaps() throws LibvirtException {
        int[] returnValue = new int[0];
        int cpuCount = getMaxVcpus();

        if (cpuCount > 0) {
            NodeInfo nodeInfo = virConnect.nodeInfo();
            int maplength = cpuMapLength(nodeInfo.maxCpus());
            virVcpuInfo[] infos = new virVcpuInfo[cpuCount];
            returnValue = new int[cpuCount * maplength];
            byte[] cpumaps = new byte[cpuCount * maplength];
            processError(libvirt.virDomainGetVcpus(VDP, infos, cpuCount, cpumaps, maplength));
            for (int x = 0; x < cpuCount * maplength; x++) {
                returnValue[x] = cpumaps[x];
            }
        }
        return returnValue;
    }

    /**
     * Extracts information about virtual CPUs of this domain
     *
     * @return an array of VcpuInfo object describing the VCPUs
     * @throws LibvirtException
     */
    public VcpuInfo[] getVcpusInfo() throws LibvirtException {
        int cpuCount = getMaxVcpus();
        VcpuInfo[] returnValue = new VcpuInfo[cpuCount];
        virVcpuInfo[] infos = new virVcpuInfo[cpuCount];
        processError(libvirt.virDomainGetVcpus(VDP, infos, cpuCount, null, 0));
        for (int x = 0; x < cpuCount; x++) {
            returnValue[x] = new VcpuInfo(infos[x]);
        }
        return returnValue;
    }

    /**
     * Provides an XML description of the domain. The description may be reused
     * later to relaunch the domain with createLinux().
     *
     * @param flags
     *            not used
     * @return the XML description String
     * @throws LibvirtException
     * @see <a href="http://libvirt.org/format.html#Normal1" >The XML
     *      Description format </a>
     */
    public String getXMLDesc(int flags) throws LibvirtException {
        return processError(libvirt.virDomainGetXMLDesc(VDP, flags)).toString();
    }

    /**
     * Determine if the domain has a snapshot
     *
     * @see <a href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainHasCurrentSnapshot>Libvi
     *      r t Documentation</a>
     * @return 1 if running, 0 if inactive
     * @throws LibvirtException
     */
    public int hasCurrentSnapshot() throws LibvirtException {
        return processError(libvirt.virDomainHasCurrentSnapshot(VDP, 0));
    }

    /**
     * Determine if the domain has a managed save image
     *
     * @see <a href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainHasManagedSaveImage>Libvi
     *      r t Documentation</a>
     * @return 0 if no image is present, 1 if an image is present, and -1 in
     *         case of error
     * @throws LibvirtException
     */
    public int hasManagedSaveImage() throws LibvirtException {
        return processError(libvirt.virDomainHasManagedSaveImage(VDP, 0));
    }

    /**
     * Returns network interface stats for interfaces attached to this domain.
     * The path parameter is the name of the network interface. Domains may have
     * more than network interface. To get stats for each you should make
     * multiple calls to this function. Individual fields within the
     * DomainInterfaceStats object may be returned as -1, which indicates that
     * the hypervisor does not support that particular statistic.
     *
     * @param path
     *            path to the interface
     * @return the statistics in a DomainInterfaceStats object
     * @throws LibvirtException
     */
    public DomainInterfaceStats interfaceStats(String path) throws LibvirtException {
        virDomainInterfaceStats stats = new virDomainInterfaceStats();
        processError(libvirt.virDomainInterfaceStats(VDP, path, stats, new SizeT(stats.size())));
        return new DomainInterfaceStats(stats);
    }

    /**
     * Determine if the domain is currently running
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainIsActive">Libvirt
     *      Documentation</a>
     * @return 1 if running, 0 if inactive
     * @throws LibvirtException
     */
    public int isActive() throws LibvirtException {
        return processError(libvirt.virDomainIsActive(VDP));
    }

    /**
     * Determine if the domain has a persistent configuration which means it
     * will still exist after shutting down
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainIsPersistent">Libvirt
     *      Documentation</a>
     * @return 1 if persistent, 0 if transient
     * @throws LibvirtException
     */
    public int isPersistent() throws LibvirtException {
        return processError(libvirt.virDomainIsPersistent(VDP));
    }


    /**
     * Returns {@code true} if, and only if, this domain has been updated.
     */
    public boolean isUpdated() throws LibvirtException {
        return processError(libvirt.virDomainIsUpdated(this.VDP)) == 1;
    }

    /**
     * suspend a domain and save its memory contents to a file on disk.
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainManagedSave">Libvirt
     *      Documentation</a>
     * @return always 0
     * @throws LibvirtException
     */
    public int managedSave() throws LibvirtException {
        return processError(libvirt.virDomainManagedSave(VDP, 0));
    }

    /**
     * Remove any managed save images from the domain
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainManagedSaveRemove">Libvirt
     *      Documentation</a>
     * @return always 0
     * @throws LibvirtException
     */
    public int managedSaveRemove() throws LibvirtException {
        return processError(libvirt.virDomainManagedSaveRemove(VDP, 0));
    }

    /**
     * Read the contents of a domain's memory.
     * <p>
     * If mode is MemoryAddressMode.VIRTUAL the 'start' parameter is
     * interpreted as virtual memory address for whichever task
     * happens to be running on the domain at the moment. Although
     * this sounds haphazard it is in fact what you want in order to
     * read Linux kernel state, because it ensures that pointers in
     * the kernel image can be interpreted coherently.
     * <p>
     * This method always reads the number of bytes remaining in the
     * buffer, that is, {@code buffer.remaining()} at the moment this
     * method is invoked. Upon return the buffer's position will be
     * equal to the limit, the limit itself will not have changed.
     *
     * @param start  the start address of the memory to peek
     * @param mode   the mode which determines whether the given addresses
     *               are interpreted as virtual or physical addresses
     */
    public void memoryPeek(long start, ByteBuffer buffer, MemoryAddressMode mode) throws LibvirtException
    {
        SizeT size = new SizeT();

        // older libvirt has a limitation on the size of data
        // transferred per request in the remote driver. So, split
        // larger requests into 64K blocks.

        do {
            final int req = Math.min(65536, buffer.remaining());

            size.setValue(req);

            processError(libvirt.virDomainMemoryPeek(this.VDP, start, size, buffer, mode.getValue()));

            buffer.position(buffer.position() + req);
        } while (buffer.hasRemaining());

        assert buffer.position() == buffer.limit();
    }

    /**
     * This function provides memory statistics for the domain.
     *
     * @param number
     *            the number of stats to retrieve
     * @return the collection of stats
     * @throws LibvirtException
     */
    public MemoryStatistic[] memoryStats(int number) throws LibvirtException {
        virDomainMemoryStats[] stats = new virDomainMemoryStats[number];
        MemoryStatistic[] returnStats = null;
        int result = processError(libvirt.virDomainMemoryStats(VDP, stats, number, 0));
        returnStats = new MemoryStatistic[result];
        for (int x = 0; x < result; x++) {
            returnStats[x] = new MemoryStatistic(stats[x]);
        }
        return returnStats;
    }

    /**
     * Migrate this domain object from its current host to the destination host
     * given by dconn (a connection to the destination host).
     * <p>
     * Flags may be bitwise OR'ed values of
     * {@link org.libvirt.Domain.MigrateFlags MigrateFlags}.
     * <p>
     * If a hypervisor supports renaming domains during migration, then you may
     * set the dname parameter to the new name (otherwise it keeps the same name).
     * <p>
     * If this is not supported by the hypervisor, dname must be {@code null} or
     * else you will get an exception.
     * <p>
     * Since typically the two hypervisors connect directly to each other in order
     * to perform the migration, you may need to specify a path from the source
     * to the destination. This is the purpose of the uri parameter.
     * <p>
     * If uri is {@code null}, then libvirt will try to find the best method.
     * <p>
     * Uri may specify the hostname or IP address of the destination host as seen
     * from the source, or uri may be a URI giving transport, hostname, user,
     * port, etc. in the usual form.
     * <p>
     * Uri should only be specified if you want to migrate over a specific interface
     * on the remote host.
     * <p>
     * For Qemu/KVM, the URI should be of the form {@code "tcp://hostname[:port]"}.
     * <p>
     * This does not require TCP auth to be setup between the connections, since
     * migrate uses a straight TCP connection (unless using the PEER2PEER flag,
     * in which case URI should be a full fledged libvirt URI).
     * <p>
     * Refer also to driver documentation for the particular URIs supported.
     * <p>
     * The maximum bandwidth (in Mbps) that will be used to do
     * migration can be specified with the bandwidth parameter. If
     * set to 0, libvirt will choose a suitable default.
     * <p>
     * Some hypervisors do not support this feature and will return an
     * error if bandwidth is not 0. To see which features are
     * supported by the current hypervisor, see
     * Connect.getCapabilities, /capabilities/host/migration_features.
     * <p>
     * There are many limitations on migration imposed by the underlying technology
     * for example it may not be possible to migrate between different processors
     * even with the same architecture, or between different types of hypervisor.
     * <p>
     * If the hypervisor supports it, dxml can be used to alter
     * host-specific portions of the domain XML that will be used on
     * the destination.
     *
     * @param dconn
     *            destination host (a Connect object)
     * @param dxml
     *            (optional) XML config for launching guest on target
     * @param flags
     *            flags
     * @param dname
     *            (optional) rename domain to this at destination
     * @param uri
     *            (optional) dest hostname/URI as seen from the source host
     * @param bandwidth
     *            (optional) specify migration bandwidth limit in Mbps
     * @return the new domain object if the migration was
     *         successful. Note that the new domain object exists in
     *         the scope of the destination connection (dconn).
     * @throws LibvirtException if the migration fails
     */
    public Domain migrate(Connect dconn, long flags, String dxml, String dname, String uri, long bandwidth) throws LibvirtException {
        DomainPointer newPtr =
                processError(libvirt.virDomainMigrate2(VDP, dconn.VCP, dxml, new NativeLong(flags), dname, uri, new NativeLong(bandwidth)));
        return new Domain(dconn, newPtr);
    }

    /**
     * Migrate this domain object from its current host to the destination host
     * given by dconn (a connection to the destination host). Flags may be one
     * of more of the following: Domain.VIR_MIGRATE_LIVE Attempt a live
     * migration. If a hypervisor supports renaming domains during migration,
     * then you may set the dname parameter to the new name (otherwise it keeps
     * the same name). If this is not supported by the hypervisor, dname must be
     * NULL or else you will get an error. Since typically the two hypervisors
     * connect directly to each other in order to perform the migration, you may
     * need to specify a path from the source to the destination. This is the
     * purpose of the uri parameter.If uri is NULL, then libvirt will try to
     * find the best method. Uri may specify the hostname or IP address of the
     * destination host as seen from the source, or uri may be a URI giving
     * transport, hostname, user, port, etc. in the usual form. Uri should only
     * be specified if you want to migrate over a specific interface on the
     * remote host. For Qemu/KVM, the uri should be of the form
     * "tcp://hostname[:port]". This does not require TCP auth to be setup
     * between the connections, since migrate uses a straight TCP connection
     * (unless using the PEER2PEER flag, in which case URI should be a full
     * fledged libvirt URI). Refer also to driver documentation for the
     * particular URIs supported. If set to 0, libvirt will choose a suitable
     * default. Some hypervisors do not support this feature and will return an
     * error if bandwidth is not 0. To see which features are supported by the
     * current hypervisor, see Connect.getCapabilities,
     * /capabilities/host/migration_features. There are many limitations on
     * migration imposed by the underlying technology - for example it may not
     * be possible to migrate between different processors even with the same
     * architecture, or between different types of hypervisor.
     *
     * @param dconn
     *            destination host (a Connect object)
     * @param flags
     *            flags
     * @param dname
     *            (optional) rename domain to this at destination
     * @param uri
     *            (optional) dest hostname/URI as seen from the source host
     * @param bandwidth
     *            optional) specify migration bandwidth limit in Mbps
     * @return the new domain object if the migration was successful. Note that
     *         the new domain object exists in the scope of the destination
     *         connection (dconn).
     * @throws LibvirtException
     */
    public Domain migrate(Connect dconn, long flags, String dname, String uri, long bandwidth) throws LibvirtException {
        DomainPointer newPtr = processError(libvirt.virDomainMigrate(VDP, dconn.VCP, new NativeLong(flags), dname, uri, new NativeLong(bandwidth)));
        return new Domain(dconn, newPtr);
    }

    /**
     * Sets maximum tolerable time for which the domain is allowed to be paused
     * at the end of live migration.
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainMigrateSetMaxDowntime">LIbvirt
     *      Documentation</a>
     * @param downtime
     *            the time to be down
     * @return always 0
     * @throws LibvirtException
     */
    public int migrateSetMaxDowntime(long downtime) throws LibvirtException {
        return processError(libvirt.virDomainMigrateSetMaxDowntime(VDP, downtime, 0));
    }

    /**
     * Migrate the domain object from its current host to the destination
     * denoted by a given URI.
     * <p>
     * The destination is given either in dconnuri (if the
     * {@link MigrateFlags#VIR_MIGRATE_PEER2PEER PEER2PEER}
     * is flag set), or in miguri (if neither the
     * {@link MigrateFlags#VIR_MIGRATE_PEER2PEER PEER2PEER} nor the
     * {@link MigrateFlags#VIR_MIGRATE_TUNNELLED TUNNELLED} migration
     * flag is set in flags).
     *
     * @see <a
     * href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainMigrateToURI">
     * virDomainMigrateToURI</a>
     *
     * @param dconnuri
     *            (optional) URI for target libvirtd if @flags includes VIR_MIGRATE_PEER2PEER
     * @param miguri
     *            (optional) URI for invoking the migration, not if @flags includs VIR_MIGRATE_TUNNELLED
     * @param dxml
     *            (optional) XML config for launching guest on target
     * @param flags
     *            Controls the migrate
     * @param dname
     *            The name at the destnation
     * @param bandwidth
     *            Specify the migration bandwidth
     * @return 0 if successful
     * @throws LibvirtException
     */
    public int migrateToURI(String dconnuri, String miguri, String dxml, long flags, String dname, long bandwidth) throws LibvirtException {
        return processError(libvirt.virDomainMigrateToURI2(VDP, dconnuri, miguri,
                dxml, new NativeLong(flags),
                dname, new NativeLong(bandwidth)));
    }

    /**
     * Migrate the domain object from its current host to the destination host
     * given by duri.
     *
     * @see <a
     *       href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainMigrateToURI">
     *       virDomainMigrateToURI</a>
     *
     * @param uri
     *            The destination URI
     * @param flags
     *            Controls the migrate
     * @param dname
     *            The name at the destnation
     * @param bandwidth
     *            Specify the migration bandwidth
     * @return 0 if successful, -1 if not
     * @throws LibvirtException
     */
    public int migrateToURI(String uri, long flags, String dname, long bandwidth) throws LibvirtException {
        return processError(libvirt.virDomainMigrateToURI(VDP, uri, new NativeLong(flags), dname, new NativeLong(bandwidth)));
    }

    /**
     * Enter the given power management suspension target level.
     */
    public void PMsuspend(SuspendTarget target) throws LibvirtException {
        PMsuspendFor(target, 0, TimeUnit.SECONDS);
    }

    /**
     * Enter the given power management suspension target level for the given duration.
     */
    public void PMsuspendFor(SuspendTarget target, long duration, TimeUnit unit) throws LibvirtException {
        processError(libvirt.virDomainPMSuspendForDuration(this.VDP, target.ordinal(), unit.toSeconds(duration), 0));
    }

    /**
     * Immediately wake up a guest using power management.
     * <p>
     * Injects a <em>wakeup<em> into the guest that previously used
     * {@link #PMsuspend} or {@link #PMsuspendFor}, rather than
     * waiting for the previously requested duration (if any) to
     * elapse.
     */
    public void PMwakeup() throws LibvirtException {
        processError(libvirt.virDomainPMWakeup(this.VDP, 0));
    }

    /**
     * Dynamically changes the real CPUs which can be allocated to a virtual
     * CPU. This function requires priviledged access to the hypervisor.
     *
     * @param vcpu
     *            virtual cpu number
     * @param cpumap
     *            bit map of real CPUs represented by the the lower 8 bits of
     *            each int in the array. Each bit set to 1 means that
     *            corresponding CPU is usable. Bytes are stored in little-endian
     *            order: CPU0-7, 8-15... In each byte, lowest CPU number is
     *            least significant bit.
     * @throws LibvirtException
     */
    public void pinVcpu(int vcpu, int[] cpumap) throws LibvirtException {
        byte[] packedMap = new byte[cpumap.length];
        for (int x = 0; x < cpumap.length; x++) {
            packedMap[x] = (byte) cpumap[x];
        }
        processError(libvirt.virDomainPinVcpu(VDP, vcpu, packedMap, cpumap.length));
    }

    /**
     * Reboot this domain, the domain object is still usable there after but the
     * domain OS is being stopped for a restart. Note that the guest OS may
     * ignore the request.
     *
     * @param flags
     *            extra flags for the reboot operation, not used yet
     * @throws LibvirtException
     */
    public void reboot(int flags) throws LibvirtException {
        processError(libvirt.virDomainReboot(VDP, flags));
    }

    /**
     * Resume this suspended domain, the process is restarted from the state
     * where it was frozen by calling virSuspendDomain(). This function may
     * requires privileged access
     *
     * @throws LibvirtException
     */
    public void resume() throws LibvirtException {
        processError(libvirt.virDomainResume(VDP));
    }

    /**
     * Adds a callback to receive notifications of IOError domain events
     * occurring on this domain.
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virConnectDomainEventRegisterAny">Libvirt
     *      Documentation</a>
     * @param cb
     *            the IOErrorCallback instance
     * @throws LibvirtException on failure
     */
    public void addIOErrorListener(final IOErrorListener cb) throws LibvirtException {
        virConnect.domainEventRegister(this, cb);
    }

    /**
     * Adds the specified listener to receive reboot events for this domain.
     *
     * @param  l   the reboot listener
     * @throws     LibvirtException on failure
     *
     * @see Connect#addRebootListener
     * @see <a
     *       href="http://www.libvirt.org/html/libvirt-libvirt.html#virConnectDomainEventRegisterAny"
     *      >virConnectDomainEventRegisterAny</a>
     * @since 1.5.2
     */
    public void addRebootListener(final RebootListener l) throws LibvirtException {
        virConnect.domainEventRegister(this, l);
    }

    /**
     * Adds the specified listener to receive lifecycle events for this domain.
     *
     * @param  l   the lifecycle listener
     * @throws LibvirtException on failure
     *
     * @see Connect#addLifecycleListener
     * @see Connect#removeLifecycleListener
     * @see <a
     *       href="http://www.libvirt.org/html/libvirt-libvirt.html#virConnectDomainEventRegisterAny"
     *      >virConnectDomainEventRegisterAny</a>
     */
    public void addLifecycleListener(final LifecycleListener l) throws LibvirtException
    {
        virConnect.domainEventRegister(this, l);
    }

    /**
     * Adds the specified listener to receive PMWakeup events for this domain.
     *
     * @param  l  the PMWakeup listener
     * @throws    LibvirtException on failure
     *
     * @see Connect#removePMWakeupListener
     * @see Connect#addPMWakeupListener
     * @see <a
     *       href="http://www.libvirt.org/html/libvirt-libvirt.html#virConnectDomainEventRegisterAny"
     *      >virConnectDomainEventRegisterAny</a>
     *
     * @since 1.5.2
     */
    public void addPMWakeupListener(final PMWakeupListener l) throws LibvirtException
    {
        virConnect.domainEventRegister(this, l);
    }

    /**
     * Adds the specified listener to receive PMSuspend events for this domain.
     *
     * @param  l  the PMSuspend listener
     * @throws    LibvirtException on failure
     *
     * @see Connect#removePMSuspendListener
     * @see Connect#addPMSuspendListener
     * @see <a
     *       href="http://www.libvirt.org/html/libvirt-libvirt.html#virConnectDomainEventRegisterAny"
     *      >virConnectDomainEventRegisterAny</a>
     *
     * @since 1.5.2
     */
    public void addPMSuspendListener(final PMSuspendListener l) throws LibvirtException
    {
        virConnect.domainEventRegister(this, l);
    }

    /**
     * Reset a domain immediately without any guest OS shutdown.
     */
    public void reset() throws LibvirtException {
        processError(libvirt.virDomainReset(this.VDP, 0));
    }

    /**
     * Revert the domain to a given snapshot.
     *
     * @see <a href=
     *      "http://www.libvirt.org/html/libvirt-libvirt.html#virDomainRevertToSnapshot"
     *      >Libvirt Documentation</>
     * @param snapshot
     *            the snapshot to revert to
     * @return 0 if the creation is successful
     * @throws LibvirtException
     */
    public int revertToSnapshot(DomainSnapshot snapshot) throws LibvirtException {
        return processError(libvirt.virDomainRevertToSnapshot(snapshot.virDomainSnapshotPtr, 0));
    }

    /**
     * Suspends this domain and saves its memory contents to a file on disk.
     * After the call, if successful, the domain is not listed as running
     * anymore (this may be a problem). Use Connect.virDomainRestore() to
     * restore a domain after saving.
     *
     * @param to
     *            path for the output file
     * @throws LibvirtException
     */
    public void save(String to) throws LibvirtException {
        processError(libvirt.virDomainSave(VDP, to));
    }

    public String screenshot(Stream stream, int screen) throws LibvirtException {
        CString mimeType = libvirt.virDomainScreenshot(this.VDP, stream.getVSP(), screen, 0);
        processError(mimeType);
        stream.markReadable();
        return mimeType.toString();
    }

    /**
     * Configures the network to be automatically started when the host machine
     * boots.
     *
     * @param autostart
     * @throws LibvirtException
     */
    public void setAutostart(boolean autostart) throws LibvirtException {
        int autoValue = autostart ? 1 : 0;
        processError(libvirt.virDomainSetAutostart(VDP, autoValue));
    }

    /**
     * * Dynamically change the maximum amount of physical memory allocated to a
     * domain. This function requires priviledged access to the hypervisor.
     *
     * @param memory
     *            the amount memory in kilobytes
     * @throws LibvirtException
     */
    public void setMaxMemory(long memory) throws LibvirtException {
        processError(libvirt.virDomainSetMaxMemory(VDP, new NativeLong(memory)));
    }

    /**
     * Dynamically changes the target amount of physical memory allocated to
     * this domain. This function may requires priviledged access to the
     * hypervisor.
     *
     * @param memory
     *            in kilobytes
     * @throws LibvirtException
     */
    public void setMemory(long memory) throws LibvirtException {
        processError(libvirt.virDomainSetMemory(VDP, new NativeLong(memory)));
    }

    /**
     * Changes the scheduler parameters
     *
     * @param params
     *            an array of SchedParameter objects to be changed
     * @throws LibvirtException
     */
    public void setSchedulerParameters(SchedParameter[] params) throws LibvirtException {
        virSchedParameter[] input = new virSchedParameter[params.length];
        for (int x = 0; x < params.length; x++) {
            input[x] = SchedParameter.toNative(params[x]);
        }
        processError(libvirt.virDomainSetSchedulerParameters(VDP, input, params.length));
    }

    /**
     * Dynamically changes the number of virtual CPUs used by this domain. Note
     * that this call may fail if the underlying virtualization hypervisor does
     * not support it or if growing the number is arbitrary limited. This
     * function requires priviledged access to the hypervisor.
     *
     * @param nvcpus
     *            the new number of virtual CPUs for this domain
     * @throws LibvirtException
     */
    public void setVcpus(int nvcpus) throws LibvirtException {
        processError(libvirt.virDomainSetVcpus(VDP, nvcpus));
    }

    /**
     * Send key(s) to the guest.
     *
     * @param  codeset  the set of keycodes
     * @param  holdtime the duration that the keys will be held (in milliseconds)
     * @param  keys     the key codes to be send
     */
    public void sendKey(KeycodeSet codeset, int holdtime, int... keys) throws LibvirtException {
        processError(libvirt.virDomainSendKey(this.VDP, codeset.ordinal(),
                holdtime, keys, keys.length, 0));
    }

    /**
     * Shuts down this domain, the domain object is still usable there after but
     * the domain OS is being stopped. Note that the guest OS may ignore the
     * request. TODO: should we add an option for reboot, knowing it may not be
     * doable in the general case ?
     *
     * @throws LibvirtException
     */
    public void shutdown() throws LibvirtException {
        processError(libvirt.virDomainShutdown(VDP));
    }

    /**
     * Creates a new snapshot of a domain based on the snapshot xml contained in
     * xmlDesc.
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainSnapshotCreateXML">Libvirt
     *      Documentation</a>
     * @param xmlDesc
     *            string containing an XML description of the domain
     * @param flags
     *            flags for creating the snapshot, see the virDomainSnapshotCreateFlags for the flag options
     * @return the snapshot
     * @throws LibvirtException
     */
    public DomainSnapshot snapshotCreateXML(String xmlDesc, int flags) throws LibvirtException {
        DomainSnapshotPointer ptr = processError(libvirt.virDomainSnapshotCreateXML(VDP, xmlDesc, flags));
        return new DomainSnapshot(ptr);
    }

    /**
     * Creates a new snapshot of a domain based on the snapshot xml contained in
     * xmlDesc.
     * <p>
     * This is just a convenience method, it has the same effect
     * as calling {@code snapshotCreateXML(xmlDesc, 0);}.
     *
     * @see #snapshotCreateXML(String, int)
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainSnapshotCreateXML">Libvirt
     *      Documentation</a>
     * @param xmlDesc
     *            string containing an XML description of the domain
     * @return the snapshot, or null on Error
     * @throws LibvirtException
     */
    public DomainSnapshot snapshotCreateXML(String xmlDesc) throws LibvirtException {
        return snapshotCreateXML(xmlDesc, 0);
    }

    /**
     * Get the current snapshot for a domain, if any.
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainSnapshotCurrent">Libvirt
     *      Documentation</a>
     * @return the snapshot
     * @throws LibvirtException
     */
    public DomainSnapshot snapshotCurrent() throws LibvirtException {
        DomainSnapshotPointer ptr = processError(libvirt.virDomainSnapshotCurrent(VDP, 0));
        return new DomainSnapshot(ptr);
    }

    /**
     * Collect the list of domain snapshots for the given domain. With the option to pass flags
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainSnapshotListNames">Libvirt
     *      Documentation</a>
     * @return The list of names, or null if an error
     * @throws LibvirtException
     */
    public String[] snapshotListNames(int flags) throws LibvirtException {
        int num = snapshotNum();
        if (num > 0) {
            CString[] names = new CString[num];
            int got = processError(libvirt.virDomainSnapshotListNames(VDP, names, num, flags));

            return Library.toStringArray(names, got);
        } else {
            return Library.NO_STRINGS;
        }
    }

    /**
     * Collect the list of domain snapshots for the given domain.
     * <p>
     * This is just a convenience method, it has the same effect
     * as calling {@code snapshotListNames(0);}.
     *
     * @see #snapshotListNames(int)
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainSnapshotListNames">
     *        virDomainSnapshotListNames</a>
     * @return The list of names, or null if an error
     * @throws LibvirtException
     */
    public String[] snapshotListNames() throws LibvirtException {
        return snapshotListNames(0);
    }

    /**
     * Retrieve a snapshot by name
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainSnapshotLookupByName">Libvirt
     *      Documentation</a>
     * @param name
     *            the name
     * @return The located snapshot
     * @throws LibvirtException
     */
    public DomainSnapshot snapshotLookupByName(String name) throws LibvirtException {
        DomainSnapshotPointer ptr = processError(libvirt.virDomainSnapshotLookupByName(VDP, name, 0));
        return new DomainSnapshot(ptr);
    }

    /**
     * Provides the number of domain snapshots for this domain..
     *
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainSnapshotNum">Libvirt
     *      Documentation</a>
     */
    public int snapshotNum() throws LibvirtException {
        return processError(libvirt.virDomainSnapshotNum(VDP, 0));
    }

    /**
     * Suspends this active domain, the process is frozen without further access
     * to CPU resources and I/O but the memory used by the domain at the
     * hypervisor level will stay allocated. Use Domain.resume() to reactivate
     * the domain. This function requires priviledged access.
     *
     * @throws LibvirtException
     */
    public void suspend() throws LibvirtException {
        processError(libvirt.virDomainSuspend(VDP));
    }

    /**
     * undefines this domain but does not stop it if it is running
     *
     * @throws LibvirtException
     */
    public void undefine() throws LibvirtException {
        processError(libvirt.virDomainUndefine(VDP));
    }

    /**
     * Undefines this domain but does not stop if it it is running. With option for passing flags
     *
     * @see <a href="http://libvirt.org/html/libvirt-libvirt.html#virDomainUndefineFlags">Libvirt Documentation</a>
     * @param flags
     *            flags for undefining the domain. See virDomainUndefineFlagsValues for more information
     * @throws LibvirtException
     */
    public void undefine(int flags) throws LibvirtException {
        processError(libvirt.virDomainUndefineFlags(VDP, flags));
    }

    /**
     * Change a virtual device on a domain
     *
     * @see <a href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainUpdateDeviceFlags">Libvirt Documentation</a>
     * @param xml
     *            the xml to update with
     * @param flags
     *            controls the update
     * @return always 0
     * @throws LibvirtException
     */
    public int updateDeviceFlags(String xml, int flags) throws LibvirtException {
        return processError(libvirt.virDomainUpdateDeviceFlags(VDP, xml, flags));
    }

}
