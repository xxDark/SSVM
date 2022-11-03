package dev.xdark.ssvm.io;

import dev.xdark.ssvm.TestUtil;
import dev.xdark.ssvm.VMTest;
import dev.xdark.ssvm.socket.SimpleSocketManager;
import dev.xdark.ssvm.socket.SocketApiInstaller;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

@Disabled("Socket implementation is not ready yet")
public class SocketTest {

	@Test
	public void doTest() {
		TestUtil.test(SocketTest.class, TestUtil.BOOTSTRAP, k -> {
			SocketApiInstaller.create(k.getVM())
				.socketManager(new SimpleSocketManager())
				.install();
		});
	}

	@VMTest
	private static void testSocketBind() throws IOException {
		ServerSocket socket = new ServerSocket();
		socket.bind(new InetSocketAddress(25565));
		socket.close();
	}

	@VMTest
	private static void testSocketConnect() throws IOException {
		Socket socket = new Socket();
		socket.connect(new InetSocketAddress("localhost", 25565));
		socket.close();
	}
}
