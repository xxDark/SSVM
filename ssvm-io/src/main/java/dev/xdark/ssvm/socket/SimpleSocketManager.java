package dev.xdark.ssvm.socket;

import dev.xdark.ssvm.value.ObjectValue;

/**
 * Basic implementation for socket manager.
 *
 * @author xDark
 */
public class SimpleSocketManager implements SocketManager {

	@Override
	public boolean isIPv6Supported() {
		return false;
	}

	@Override
	public int createSocket(boolean stream, boolean v6Only) {
		return 0;
	}

	@Override
	public void bind(int fd, ObjectValue localAddress, int localPort, boolean exclusiveBind) {
	}

	@Override
	public void listen(int fd, int backlog) {
	}

	@Override
	public void close(int fd) {
	}
}
