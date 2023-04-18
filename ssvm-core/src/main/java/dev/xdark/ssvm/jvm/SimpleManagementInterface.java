package dev.xdark.ssvm.jvm;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Collections;
import java.util.List;

/**
 * Basic VM management interface.
 *
 * @author xDark
 */
@Getter
@RequiredArgsConstructor
public class SimpleManagementInterface implements ManagementInterface {

	private final String version;
	private final long startupTime;
	private final List<String> inputArguments;

	public SimpleManagementInterface() {
		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		version = bean.getSpecVersion();
		startupTime = System.currentTimeMillis();
		inputArguments = Collections.emptyList();
	}
}
