package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.value.ObjectValue;

/**
 * VM field operations.
 *
 * @author xDark
 */
public interface FieldOperations {

	/**
	 * Sets reference value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @param desc     Field descriptor.
	 * @param value    Value to set.
	 */
	void putReference(ObjectValue instance, InstanceClass klass, String name, String desc, ObjectValue value);

	/**
	 * Sets reference value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param name     Field name.
	 * @param desc     Field descriptor.
	 * @param value    Value to set.
	 */
	void putReference(ObjectValue instance, String name, String desc, ObjectValue value);

	/**
	 * Sets long value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putLong(ObjectValue instance, InstanceClass klass, String name, long value);

	/**
	 * Sets long value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putLong(ObjectValue instance, String name, long value);

	/**
	 * Sets double value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putDouble(ObjectValue instance, InstanceClass klass, String name, double value);

	/**
	 * Sets double value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putDouble(ObjectValue instance, String name, double value);

	/**
	 * Sets int value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putInt(ObjectValue instance, InstanceClass klass, String name, int value);

	/**
	 * Sets int value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putInt(ObjectValue instance, String name, int value);

	/**
	 * Sets float value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putFloat(ObjectValue instance, InstanceClass klass, String name, float value);

	/**
	 * Sets float value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putFloat(ObjectValue instance, String name, float value);

	/**
	 * Sets char value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putChar(ObjectValue instance, InstanceClass klass, String name, char value);

	/**
	 * Sets char value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putChar(ObjectValue instance, String name, char value);

	/**
	 * Sets short value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putShort(ObjectValue instance, InstanceClass klass, String name, short value);

	/**
	 * Sets short value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putShort(ObjectValue instance, String name, short value);

	/**
	 * Sets byte value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putByte(ObjectValue instance, InstanceClass klass, String name, byte value);

	/**
	 * Sets byte value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putByte(ObjectValue instance, String name, byte value);

	/**
	 * Sets boolean value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putBoolean(ObjectValue instance, InstanceClass klass, String name, boolean value);

	/**
	 * Sets boolean value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putBoolean(ObjectValue instance, String name, boolean value);

	/**
	 * Gets reference value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @param desc     Field desc.
	 * @return field value.
	 */
	ObjectValue getReference(ObjectValue instance, InstanceClass klass, String name, String desc);

	/**
	 * Gets reference value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param name     Field name.
	 * @param desc     Field desc.
	 * @return field value.
	 */
	ObjectValue getReference(ObjectValue instance, String name, String desc);

	/**
	 * Gets long value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @return field value.
	 */
	long getLong(ObjectValue instance, InstanceClass klass, String name);

	/**
	 * Gets long value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param name     Field name.
	 * @return field value.
	 */
	long getLong(ObjectValue instance, String name);

	/**
	 * Gets double value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @return field value.
	 */
	double getDouble(ObjectValue instance, InstanceClass klass, String name);

	/**
	 * Gets double value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param name     Field name.
	 * @return field value.
	 */
	double getDouble(ObjectValue instance, String name);

	/**
	 * Gets int value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @return field value.
	 */
	int getInt(ObjectValue instance, InstanceClass klass, String name);

	/**
	 * Gets int value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param name     Field name.
	 * @return field value.
	 */
	int getInt(ObjectValue instance, String name);

	/**
	 * Gets float value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @return field value.
	 */
	float getFloat(ObjectValue instance, InstanceClass klass, String name);

	/**
	 * Gets float value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param name     Field name.
	 * @return field value.
	 */
	float getFloat(ObjectValue instance, String name);

	/**
	 * Gets char value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @return field value.
	 */
	char getChar(ObjectValue instance, InstanceClass klass, String name);

	/**
	 * Gets char value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param name     Field name.
	 * @return field value.
	 */
	char getChar(ObjectValue instance, String name);

	/**
	 * Gets short value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @return field value.
	 */
	short getShort(ObjectValue instance, InstanceClass klass, String name);

	/**
	 * Gets short value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param name     Field name.
	 * @return field value.
	 */
	short getShort(ObjectValue instance, String name);

	/**
	 * Gets byte value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @return field value.
	 */
	byte getByte(ObjectValue instance, InstanceClass klass, String name);

	/**
	 * Gets byte value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param name     Field name.
	 * @return field value.
	 */
	byte getByte(ObjectValue instance, String name);

	/**
	 * Gets boolean value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @return field value.
	 */
	boolean getBoolean(ObjectValue instance, InstanceClass klass, String name);

	/**
	 * Gets boolean value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param name     Field name.
	 * @return field value.
	 */
	boolean getBoolean(ObjectValue instance, String name);

	/**
	 * Gets static reference value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to get value from.
	 * @param name  Field name.
	 * @param desc  Field descriptor.
	 * @return field value.
	 */
	ObjectValue getReference(InstanceClass klass, String name, String desc);

	/**
	 * Gets static long value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to get value from.
	 * @param name  Field name.
	 * @return field value.
	 */
	long getLong(InstanceClass klass, String name);

	/**
	 * Gets static double value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to get value from.
	 * @param name  Field name.
	 * @return field value.
	 */
	double getDouble(InstanceClass klass, String name);

	/**
	 * Gets static int value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to get value from.
	 * @param name  Field name.
	 * @return field value.
	 */
	int getInt(InstanceClass klass, String name);

	/**
	 * Gets static float value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to get value from.
	 * @param name  Field name.
	 * @return field value.
	 */
	float getFloat(InstanceClass klass, String name);

	/**
	 * Gets static char value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to get value from.
	 * @param name  Field name.
	 * @return field value.
	 */
	char getChar(InstanceClass klass, String name);

	/**
	 * Gets static short value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to get value from.
	 * @param name  Field name.
	 * @return field value.
	 */
	short getShort(InstanceClass klass, String name);

	/**
	 * Gets static byte value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to get value from.
	 * @param name  Field name.
	 * @return field value.
	 */
	byte getByte(InstanceClass klass, String name);

	/**
	 * Gets static boolean value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to get value from.
	 * @param name  Field name.
	 * @return field value.
	 */
	boolean getBoolean(InstanceClass klass, String name);

	/**
	 * Sets static reference value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to set value for.
	 * @param name  Field name.
	 * @param desc  Field descriptor.
	 * @param value Value to set.
	 */
	void putReference(InstanceClass klass, String name, String desc, ObjectValue value);

	/**
	 * Sets static long value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to set value for.
	 * @param name  Field name.
	 * @param value Value to set.
	 */
	void putLong(InstanceClass klass, String name, long value);

	/**
	 * Sets static double value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to set value for.
	 * @param name  Field name.
	 * @param value Value to set.
	 */
	void putDouble(InstanceClass klass, String name, double value);

	/**
	 * Sets static int value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to set value for.
	 * @param name  Field name.
	 * @param value Value to set.
	 */
	void putInt(InstanceClass klass, String name, int value);

	/**
	 * Sets static float value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to set value for.
	 * @param name  Field name.
	 * @param value Value to set.
	 */
	void putFloat(InstanceClass klass, String name, float value);

	/**
	 * Sets static char value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to set value for.
	 * @param name  Field name.
	 * @param value Value to set.
	 */
	void putChar(InstanceClass klass, String name, char value);

	/**
	 * Sets static short value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to set value for.
	 * @param name  Field name.
	 * @param value Value to set.
	 */
	void putShort(InstanceClass klass, String name, short value);

	/**
	 * Sets static byte value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to set value for.
	 * @param name  Field name.
	 * @param value Value to set.
	 */
	void putByte(InstanceClass klass, String name, byte value);

	/**
	 * Sets static boolean value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to set value for.
	 * @param name  Field name.
	 * @param value Value to set.
	 */
	void putBoolean(InstanceClass klass, String name, boolean value);
}
