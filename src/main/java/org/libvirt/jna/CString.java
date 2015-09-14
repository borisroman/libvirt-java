package org.libvirt.jna;

import com.sun.jna.FromNativeContext;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;

/**
 * Represents an allocated C-String.
 * <p>
 * Either call {@link #toString} or {@link #free()}. Both methods make
 * sure to reclaim the memory allocated for the string by calling
 * Native.free.
 */
public class CString extends PointerType {
    private String string = null;

    public CString() {
        super();
    }

    public CString(Pointer p) {
        super(p);
    }

    /**
     * Returns a String representing the value of this C-String
     * <p>
     * Side-effect: frees the memory of the C-String.
     */
    @Override
    public String toString() {
        if (string == null) {
            final Pointer ptr = getPointer();

            if (ptr == null) return null;

            string = ptr.getString(0L, "UTF-8");

            free(ptr);
        }
        return string;
    }

    @Override
    public CString fromNative(Object nativeValue, FromNativeContext context) {
        if (nativeValue == null) {
            return null;
        }

        return new CString((Pointer)nativeValue);
    }

    private void free(Pointer ptr) {
        assert ptr != null;

        Native.free(Pointer.nativeValue(ptr));
        setPointer(null);
    }

    /**
     * Free the memory used by this C-String
     */
    public void free() {
        final Pointer ptr = getPointer();
        if (ptr != null) free(ptr);
    }
}
