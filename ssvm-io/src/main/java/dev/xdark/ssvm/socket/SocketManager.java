package dev.xdark.ssvm.socket;

import dev.xdark.ssvm.value.ObjectValue;

/**
 * Socket manager.
 *
 * @author xDark
 */
public interface SocketManager {

	/**
	 * @return {@code true} if IPv6 protocol is supported.
	 */
	boolean isIPv6Supported();

	/**
	 * Creates new socket descriptor.
	 *
	 * @param stream Whether the socket is a connected stream socket {@code true},
	 *               or an unconnected one.
	 * @param v6Only Whether only IPv6 may be used.
	 * @return Socket file descriptor.
	 */
	int createSocket(boolean stream, boolean v6Only);

	/**
	 * Attempts to bind the socket.
	 *
	 * @param fd            Socket descriptor.
	 * @param localAddress  Local address.
	 * @param localPort     Local port.
	 * @param exclusiveBind Whether the socket is to be bound exclusively.
	 */
	void bind(int fd, ObjectValue localAddress, int localPort, boolean exclusiveBind);

	/**
	 * @param fd      Socket descriptor.
	 * @param backlog The amount of time to listen for connections.
	 */
	void listen(int fd, int backlog);

	/**
	 * Closes the socket.
	 *
	 * @param fd Socket descriptor.
	 */
	void close(int fd);
}
