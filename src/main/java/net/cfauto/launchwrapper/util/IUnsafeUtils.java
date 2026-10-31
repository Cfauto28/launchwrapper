package net.cfauto.launchwrapper.util;

import java.lang.reflect.Field;

@SuppressWarnings("unused")
public interface IUnsafeUtils {

    void setStaticBoolean(final Field field, boolean value);

    void setStaticByte(final Field field, byte value);

    void setStaticShort(final Field field, short value);

    void setStaticChar(final Field field, char value);

    void setStaticInt(final Field field, int value);

    void setStaticLong(final Field field, long value);

    void setStaticFloat(final Field field, float value);

    void setStaticDouble(final Field field, double value);

    void setStaticObject(final Field field, Object value);

    // Non-static set

    void setBoolean(final Object base, final Field field, boolean value);

    void setByte(final Object base, final Field field, byte value);

    void setShort(final Object base, final Field field, short value);

    void setChar(final Object base, final Field field, char value);

    void setInt(final Object base, final Field field, int value);

    void setLong(final Object base, final Field field, long value);

    void setFloat(final Object base, final Field field, float value);

    void setDouble(final Object base, final Field field, double value);

    void setObject(final Object base, final Field field, Object value);

    // Static get

    boolean getStaticBoolean(final Field field);

    byte getStaticByte(final Field field);

    short getStaticShort(final Field field);

    char getStaticChar(final Field field);

    int getStaticInt(final Field field);

    long getStaticLong(final Field field);

    float getStaticFloat(final Field field);

    double getStaticDouble(final Field field);

    Object getStaticObject(final Field field);

    // Non-static get

    boolean getBoolean(final Object base, final Field field);

    byte getByte(final Object base, final Field field);

    short getShort(final Object base, final Field field);

    char getChar(final Object base, final Field field);

    int getInt(final Object base, final Field field);

    long getLong(final Object base, final Field field);

    float getFloat(final Object base, final Field field);

    double getDouble(final Object base, final Field field);

    Object getObject(final Object base, final Field field);

    //Other misc stuff
    Object allocateInstance(Class<?> clazz);

    void throwException(Throwable ee);

    void setMemory(long address, long bytes, byte value);

    void copyMemory(long srcAddress, long destAddress, long bytes);

    void freeMemory(long address);

    long allocateMemory(long bytes);
}
