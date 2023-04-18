package dev.xdark.ssvm.jvm;

import java.util.List;

/**
 * VM management interface.
 *
 * @see sun.management.VMManagementImpl
 */
public interface ManagementInterface {

	/**
	 * @return management API version.
	 */
	String getVersion();

	/**
	 * @return VM startup time.
	 */
	long getStartupTime();

	/**
	 * @return The input arguments <quote>passed</quote> to
	 * the VM which does not include the arguments to the main method.
	 */
	List<String> getInputArguments();
}
