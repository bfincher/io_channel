package com.fincher.iochannel.udp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicReference;

import org.awaitility.Awaitility;
import org.junit.AfterClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

import com.fincher.iochannel.ChannelException;
import com.fincher.iochannel.ChannelState;
import com.fincher.iochannel.IoChannelTesterBase;
import com.fincher.iochannel.IoType;
import com.fincher.iochannel.MessageBuffer;
import com.fincher.iochannel.TestDataFactoryIfc;
import com.fincher.iochannel.TestUtilities;
import com.fincher.iochannel.Utilities;

/**
 * Test UDP sockets
 * 
 * @author Brian Fincher
 *
 */
public class UdpTest extends IoChannelTesterBase<MessageBuffer> {

	/** Method name is self explainatory */
	@AfterClass
	public static void tearDown() {
		// DEST_UNICAST_CONFIG.delete();
	}

	private static class TestDataFactory implements TestDataFactoryIfc<MessageBuffer> {
		@Override
		public String toString(MessageBuffer data) {
			return new String(data.getBytes());
		}

		@Override
		public MessageBuffer getTestData(int iteration) {
			return new MessageBuffer(new String("Hello World " + iteration).getBytes());
		}
	}

	/**
	 * Method name is self explainatory
	 * 
	 */
	@Test
	public void testMulticast() throws UnknownHostException {
		InetSocketAddress localAddress0 = new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 0);
		InetSocketAddress localAddress5000 = new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 5000);
		InetSocketAddress multicastAddress = new InetSocketAddress(InetAddress.getByName("239.1.1.1"), 5000);

		UdpMulticastChannel output = UdpMulticastChannel.createOutputChannel("output", localAddress0, multicastAddress);

		UdpMulticastChannel input = UdpMulticastChannel.createInputChannel("input", messageQueue::add, localAddress5000,
				multicastAddress.getAddress());

		test(input, output, new TestDataFactory());
	}

	/**
	 * Method name is self explanatory
	 * 
	 */
	@Test
	public void testUnicast() throws Exception {
		InetSocketAddress localAddress0 = new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 0);
		InetSocketAddress localAddress5000 = new InetSocketAddress(InetAddress.getByName("localhost"), 5000);

		UdpChannel output = UdpChannel.createOutputChannel("output", localAddress0, localAddress5000);

		UdpChannel input = UdpChannel.createInputChannel("input", messageQueue::add, localAddress5000);
		test(input, output, new TestDataFactory());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testReadThrowsException() throws Exception {

		Utilities origUtilities = Utilities.getInstance();
		try {
			Utilities.setInstanceForTesting(new TestUtilities());
			Logger logger = Utilities.getInstance().getLogger(UdpChannel.class);
			Mockito.when(logger.isInfoEnabled()).thenReturn(true);

			String testExceptionText = "!!TEST EXCEPTION TEXT!!";

			TestUdpChannel channel = new TestUdpChannel();
			DatagramSocket socket = channel.createSocket();
			Mockito.doAnswer(new Answer<Void>() {
				@Override
				public Void answer(InvocationOnMock invocation) throws IOException {
					throw new IOException(testExceptionText);
				}
			}).when(socket).receive(Mockito.any());
			
			AtomicReference<IOException> loggedException = new AtomicReference<>();

			Mockito.doAnswer(new Answer<Void>() {
				@Override
				public Void answer(InvocationOnMock invocation) {
					loggedException.set(invocation.getArgument(1, IOException.class));
					return null;
				}
			}).when(logger).error(Mockito.anyString(), Mockito.any(IOException.class));

			channel.connect();
			
			Awaitility.await().until(() -> loggedException.get() != null);

			assertTrue(loggedException.get().getMessage().contains(testExceptionText));

			channel.close();
		} finally {
			Utilities.setInstanceForTesting(origUtilities);
		}
	}

	@Test(expected = IOException.class)
	public void testSendThrowsException() throws Exception {

		TestUdpChannel channel = new TestUdpChannel("id", IoType.OUTPUT_ONLY, new InetSocketAddress(1000),
				new InetSocketAddress(1001));

		DatagramSocket socket = channel.createSocket();
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws IOException {
				throw new IOException();
			}
		}).when(socket).send(Mockito.any());

		channel.connect();
		channel.send(new MessageBuffer(new byte[0]));
		channel.close();
	}

	@Test
	public void testIllegalStates() throws ChannelException, InterruptedException {
		UdpChannel channel = new TestUdpChannel();
		assertFalse(channel.isConnected());

		// test send on a channel not connected
		try {
			channel.send(null);
			fail("Should have got exception");
		} catch (IllegalStateException e) {
			assertEquals("id Cannot send on a channel that is not connected", e.getMessage());
		}

		channel.connect();
		assertTrue(channel.isConnected());

		// test send on an input only channel
		try {
			channel.send(null);
			fail("Should have got exception");
		} catch (IllegalStateException e) {
			assertEquals("id Cannot send on an input only channel", e.getMessage());
		}
		try {
			channel.setSocketOptions(new UdpSocketOptions());
			fail("Should have got exception");
		} catch (IllegalStateException e) {
			// expected
		}

		try {
			channel.connect();
			fail("Should have got exception");
		} catch (IllegalStateException e) {
			// expected
		}

		channel.close();
	}

	@Test
	public void testOffNominal() throws Exception {
		UdpChannel channel = UdpChannel.createInputChannel("id", null);
		channel.close();
		assertEquals(ChannelState.CLOSED, channel.getState());
	}

	private static class TestUdpChannel extends UdpChannel {

		final DatagramSocket socket;

		public TestUdpChannel(String id, IoType ioType, InetSocketAddress local, InetSocketAddress remote) {
			super(id, ioType, local, remote);
			socket = Mockito.mock(DatagramSocket.class);
		}

		public TestUdpChannel() {
			this("id", IoType.INPUT_ONLY, null, null);
		}

		@Override
		protected DatagramSocket createSocket() {
			return socket;
		}
	}
}
