package dev.xdark.ssvm.mirror.type;

import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.member.area.ClassArea;

/**
 * Class linking bridge.
 *
 * @author xDark
 */
public interface ClassLinkage {

	/**
	 * @param superClass Super class to set.
	 */
	void setSuperClass(InstanceClass superClass);

	/**
	 * @param interfaces Interfaces to set.
	 */
	void setInterfaces(InstanceClass[] interfaces);

	/**
	 * @param fieldArea Field area to set.
	 */
	void setVirtualFieldArea(ClassArea<JavaField> fieldArea);

	/**
	 * @param fieldArea Field area to set.
	 */
	void setStaticFieldArea(ClassArea<JavaField> fieldArea);

	/**
	 * @param methodArea Method area to set.
	 */
	void setMethodArea(ClassArea<JavaMethod> methodArea);

	/**
	 * @param occupiedInstanceSpace How many bytes each instance of this class occupies.
	 */
	void setOccupiedInstanceSpace(long occupiedInstanceSpace);

	/**
	 * @param occupiedStaticSpace How many bytes required to store all static fields of the class.
	 */
	void setOccupiedStaticSpace(long occupiedStaticSpace);
}
