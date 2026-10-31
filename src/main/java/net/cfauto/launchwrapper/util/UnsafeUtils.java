package net.cfauto.launchwrapper.util;


import net.cfauto.launchwrapper.Launch;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Stolen from MCPHacker's launchwrapper, a util to access unsafe.
 * Exposes a bunch of methods for tweakers to
 */
@SuppressWarnings({"sunapi", "unused", "removal"/*For java 18+*/})
final class UnsafeUtils implements IUnsafeUtils {
    private final sun.misc.Unsafe theUnsafe = Objects.requireNonNull(getUnsafe());

    UnsafeUtils() {}

    private sun.misc.Unsafe getUnsafe() {
        try {
            final Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            return (sun.misc.Unsafe)unsafeField.get(null);
        } catch (Exception e) {
            logUnsafe(e);
            return null;
        }
    }

    // Static set

    @Override
    public void setStaticBoolean(final Field field, boolean value) {
        final Object staticFieldBase = theUnsafe.staticFieldBase(field);
        final long staticFieldOffset = theUnsafe.staticFieldOffset(field);
        theUnsafe.putBoolean(staticFieldBase, staticFieldOffset, value);
    }

    @Override
    public void setStaticByte(final Field field, byte value) {
        final Object staticFieldBase = theUnsafe.staticFieldBase(field);
        final long staticFieldOffset = theUnsafe.staticFieldOffset(field);
        theUnsafe.putByte(staticFieldBase, staticFieldOffset, value);
    }

    @Override
    public void setStaticShort(final Field field, short value) {
        final Object staticFieldBase = theUnsafe.staticFieldBase(field);
        final long staticFieldOffset = theUnsafe.staticFieldOffset(field);
        theUnsafe.putShort(staticFieldBase, staticFieldOffset, value);
    }

    @Override
    public void setStaticChar(final Field field, char value) {
        final Object staticFieldBase = theUnsafe.staticFieldBase(field);
        final long staticFieldOffset = theUnsafe.staticFieldOffset(field);
        theUnsafe.putChar(staticFieldBase, staticFieldOffset, value);
    }

    @Override
    public void setStaticInt(final Field field, int value) {
        final Object staticFieldBase = theUnsafe.staticFieldBase(field);
        final long staticFieldOffset = theUnsafe.staticFieldOffset(field);
        theUnsafe.putInt(staticFieldBase, staticFieldOffset, value);
    }

    @Override
    public void setStaticLong(final Field field, long value) {
        final Object staticFieldBase = theUnsafe.staticFieldBase(field);
        final long staticFieldOffset = theUnsafe.staticFieldOffset(field);
        theUnsafe.putLong(staticFieldBase, staticFieldOffset, value);
    }

    @Override
    public void setStaticFloat(final Field field, float value) {
        final Object staticFieldBase = theUnsafe.staticFieldBase(field);
        final long staticFieldOffset = theUnsafe.staticFieldOffset(field);
        theUnsafe.putFloat(staticFieldBase, staticFieldOffset, value);
    }

    @Override
    public void setStaticDouble(final Field field, double value) {
        final Object staticFieldBase = theUnsafe.staticFieldBase(field);
        final long staticFieldOffset = theUnsafe.staticFieldOffset(field);
        theUnsafe.putDouble(staticFieldBase, staticFieldOffset, value);
    }

    @Override
    public void setStaticObject(final Field field, Object value) {
        final Object staticFieldBase = theUnsafe.staticFieldBase(field);
        final long staticFieldOffset = theUnsafe.staticFieldOffset(field);
        theUnsafe.putObject(staticFieldBase, staticFieldOffset, value);
    }

    // Non-static set

    @Override
    public void setBoolean(final Object base, final Field field, boolean value) {
        final long fieldOffset = theUnsafe.objectFieldOffset(field);
        theUnsafe.putBoolean(base, fieldOffset, value);
    }

    @Override
    public void setByte(final Object base, final Field field, byte value) {
        final long fieldOffset = theUnsafe.objectFieldOffset(field);
        theUnsafe.putByte(base, fieldOffset, value);
    }

    @Override
    public void setShort(final Object base, final Field field, short value) {
        final long fieldOffset = theUnsafe.objectFieldOffset(field);
        theUnsafe.putShort(base, fieldOffset, value);
    }

    @Override
    public void setChar(final Object base, final Field field, char value) {
        final long fieldOffset = theUnsafe.objectFieldOffset(field);
        theUnsafe.putChar(base, fieldOffset, value);
    }

    @Override
    public void setInt(final Object base, final Field field, int value) {
        final long fieldOffset = theUnsafe.objectFieldOffset(field);
        theUnsafe.putInt(base, fieldOffset, value);
    }

    @Override
    public void setLong(final Object base, final Field field, long value) {
        final long fieldOffset = theUnsafe.objectFieldOffset(field);
        theUnsafe.putLong(base, fieldOffset, value);
    }

    @Override
    public void setFloat(final Object base, final Field field, float value) {
        final long fieldOffset = theUnsafe.objectFieldOffset(field);
        theUnsafe.putFloat(base, fieldOffset, value);
    }

    @Override
    public void setDouble(final Object base, final Field field, double value) {
        final long fieldOffset = theUnsafe.objectFieldOffset(field);
        theUnsafe.putDouble(base, fieldOffset, value);
    }

    @Override
    public void setObject(final Object base, final Field field, Object value) {
        final long fieldOffset = theUnsafe.objectFieldOffset(field);
        theUnsafe.putObject(base, fieldOffset, value);
    }

    // Static get

    @Override
    public boolean getStaticBoolean(final Field field) {
        final Object staticFieldBase = theUnsafe.staticFieldBase(field);
        final long staticFieldOffset = theUnsafe.staticFieldOffset(field);
        return theUnsafe.getBoolean(staticFieldBase, staticFieldOffset);
    }

    @Override
    public byte getStaticByte(final Field field) {
        final Object staticFieldBase = theUnsafe.staticFieldBase(field);
        final long staticFieldOffset = theUnsafe.staticFieldOffset(field);
        return theUnsafe.getByte(staticFieldBase, staticFieldOffset);
    }

    @Override
    public short getStaticShort(final Field field) {
        final Object staticFieldBase = theUnsafe.staticFieldBase(field);
        final long staticFieldOffset = theUnsafe.staticFieldOffset(field);
        return theUnsafe.getShort(staticFieldBase, staticFieldOffset);
    }

    @Override
    public char getStaticChar(final Field field) {
        final Object staticFieldBase = theUnsafe.staticFieldBase(field);
        final long staticFieldOffset = theUnsafe.staticFieldOffset(field);
        return theUnsafe.getChar(staticFieldBase, staticFieldOffset);
    }

    @Override
    public int getStaticInt(final Field field) {
        final Object staticFieldBase = theUnsafe.staticFieldBase(field);
        final long staticFieldOffset = theUnsafe.staticFieldOffset(field);
        return theUnsafe.getInt(staticFieldBase, staticFieldOffset);
    }

    @Override
    public long getStaticLong(final Field field) {
        final Object staticFieldBase = theUnsafe.staticFieldBase(field);
        final long staticFieldOffset = theUnsafe.staticFieldOffset(field);
        return theUnsafe.getLong(staticFieldBase, staticFieldOffset);
    }

    @Override
    public float getStaticFloat(final Field field) {
        final Object staticFieldBase = theUnsafe.staticFieldBase(field);
        final long staticFieldOffset = theUnsafe.staticFieldOffset(field);
        return theUnsafe.getFloat(staticFieldBase, staticFieldOffset);
    }

    @Override
    public double getStaticDouble(final Field field) {
        final Object staticFieldBase = theUnsafe.staticFieldBase(field);
        final long staticFieldOffset = theUnsafe.staticFieldOffset(field);
        return theUnsafe.getDouble(staticFieldBase, staticFieldOffset);
    }

    @Override
    public Object getStaticObject(final Field field) {
        final Object staticFieldBase = theUnsafe.staticFieldBase(field);
        final long staticFieldOffset = theUnsafe.staticFieldOffset(field);
        return theUnsafe.getObject(staticFieldBase, staticFieldOffset);
    }

    // Non-static get

    @Override
    public boolean getBoolean(final Object base, final Field field) {
        final long fieldOffset = theUnsafe.objectFieldOffset(field);
        return theUnsafe.getBoolean(base, fieldOffset);
    }

    @Override
    public byte getByte(final Object base, final Field field) {
        final long fieldOffset = theUnsafe.objectFieldOffset(field);
        return theUnsafe.getByte(base, fieldOffset);
    }

    @Override
    public short getShort(final Object base, final Field field) {
        final long fieldOffset = theUnsafe.objectFieldOffset(field);
        return theUnsafe.getShort(base, fieldOffset);
    }

    @Override
    public char getChar(final Object base, final Field field) {
        final long fieldOffset = theUnsafe.objectFieldOffset(field);
        return theUnsafe.getChar(base, fieldOffset);
    }

    @Override
    public int getInt(final Object base, final Field field) {
        final long fieldOffset = theUnsafe.objectFieldOffset(field);
        return theUnsafe.getInt(base, fieldOffset);
    }

    @Override
    public long getLong(final Object base, final Field field) {
        final long fieldOffset = theUnsafe.objectFieldOffset(field);
        return theUnsafe.getLong(base, fieldOffset);
    }

    @Override
    public float getFloat(final Object base, final Field field) {
        final long fieldOffset = theUnsafe.objectFieldOffset(field);
        return theUnsafe.getFloat(base, fieldOffset);
    }

    @Override
    public double getDouble(final Object base, final Field field) {
        final long fieldOffset = theUnsafe.objectFieldOffset(field);
        return theUnsafe.getDouble(base, fieldOffset);
    }

    @Override
    public Object getObject(final Object base, final Field field) {
        final long fieldOffset = theUnsafe.objectFieldOffset(field);
        return theUnsafe.getObject(base, fieldOffset);
    }

    @Override
    public Object allocateInstance(Class<?> clazz) {
        try {
            return theUnsafe.allocateInstance(clazz);
        } catch (InstantiationException e) {
            logUnsafe(e);
        }
        return null;
    }

    @Override
    public void throwException(Throwable ee) {
        theUnsafe.throwException(ee);
    }

    @Override
    public void setMemory(long address, long bytes, byte value) {
        theUnsafe.setMemory(address, bytes, value);
    }

    @Override
    public void copyMemory(long srcAddress, long destAddress, long bytes) {
        theUnsafe.copyMemory(srcAddress, destAddress, bytes);
    }

    @Override
    public void freeMemory(long address) {
        theUnsafe.freeMemory(address);
    }

    @Override
    public long allocateMemory(long bytes) {
        return theUnsafe.allocateMemory(bytes);
    }

    private void logUnsafe(Exception e) {
        Launch.LOGGER.atFatal().log("Unsafe!", e);
    }
}
