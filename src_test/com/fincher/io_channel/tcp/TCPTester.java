package com.fincher.io_channel.tcp;

import static junit.framework.Assert.fail;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.junit.Test;

import com.fincher.io_channel.ChannelException;
import com.fincher.io_channel.IOChannelTesterBase;
import com.fincher.io_channel.MessageBuffer;
import com.fincher.io_channel.QueueMessageHandler;

/** A JUnit tester for TCP sockets */
public class TCPTester extends IOChannelTesterBase<MessageBuffer> {
	
	private abstract class TCPServerFactory {
		public abstract TCPServerChannel createTCPServer() throws ChannelException;
	}
	
//	private static final File SOURCE_CONFIG_FILE = new File("javaSrcTest/ec2bmc/ifc/io_channel/tcp/test_tcp_config.xml");
//	private static final File DEST_CONFIG_FILE = new File("data/test_tcp_config.xml");
//	private static final File SCHEMA_FILE = new File("data/io_channel.xsd");
	
	private BlockingQueue<MessageBuffer> queue1 = new LinkedBlockingQueue<MessageBuffer>();
	private BlockingQueue<MessageBuffer> queue2 = new LinkedBlockingQueue<MessageBuffer>();	
	
//	@Override 
//	public void setUp() throws Exception {
//		loadConfigMap(SOURCE_CONFIG_FILE, DEST_CONFIG_FILE, SCHEMA_FILE);
//	}
//	
//	@Override 
//	public void tearDown() {
//		DEST_CONFIG_FILE.delete();
//	}
	
	private void test(TCPClientChannel client1, 
			TCPClientChannel client2,
			TCPServerFactory tcpServerFactory) {
		try {
			TCPServerChannel server = tcpServerFactory.createTCPServer();
			server.connect();						
			client1.connect();			
			client2.connect();
			
			while (!client1.isConnected() || !client2.isConnected())
				Thread.sleep(100);
			
			for (int i = 0; i < 5; i++) {
				server.send (new MessageBuffer(SimpleStreamIO.prePendLength(new String("Hello World " + i).getBytes())));
				Thread.sleep(1000);
			}
			
			server.close();
			server = tcpServerFactory.createTCPServer();
			server.connect();
			
			Thread.sleep(1000);
			
			while (!client1.isConnected() || !client2.isConnected())
				Thread.sleep(100);
			
			System.out.println(client1.isConnected() + " " + client2.isConnected());
			
			for (int i = 5; i < 10; i++) {
				server.send (new MessageBuffer(SimpleStreamIO.prePendLength(new String("Hello World " + i).getBytes())));
				Thread.sleep(1000);
			}
			
			if (queue1.size() != 10)
				fail("Queue1 size = " + queue1.size());
			
			if (queue2.size() != 10)
				fail("Queue2 size = " + queue2.size());						
			
			System.out.println("client1's messages: ");
			for (MessageBuffer mb: queue1) {
				System.out.println("    " + new String(mb.getBytes()));
			}
			
			System.out.println("client2's messages: ");
			for (MessageBuffer mb: queue2) {
				System.out.println("    " + new String(mb.getBytes()));
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
	/** Test TCP sockets
	 * 
	 */
	@Test
	public void test() {
		
		Logger.getRootLogger().addAppender(new ConsoleAppender(new SimpleLayout()));
		
		try {
			
			final InetSocketAddress localAddress5000 = new InetSocketAddress(InetAddress.getLocalHost(), 5000);
			
			InetSocketAddress remoteAddress = new InetSocketAddress(InetAddress.getLocalHost(), 5000);
			
			InetSocketAddress localAddress0 = new InetSocketAddress(InetAddress.getLocalHost(), 0);
			TCPClientChannel client1 = new TCPClientChannel("client1", 
					new QueueMessageHandler<MessageBuffer>(queue1), 
					new SimpleStreamIO(), 
					localAddress0, 
					remoteAddress);
			
			TCPClientChannel client2 = new TCPClientChannel("client2",
					new QueueMessageHandler<MessageBuffer>(queue2), 
					new SimpleStreamIO(), 
					localAddress0, 
					remoteAddress);
			
			test(client1, client2, new TCPServerFactory() {
				
				@Override
				public TCPServerChannel createTCPServer() throws ChannelException {
					return new TCPServerChannel("server", new SimpleStreamIO(), localAddress5000);
				}
			});						
		} 
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}						
	}
	
//	/** Test TCP sockets loaded from configuration files */
//	public void testFromConfig() {
//		queue1.clear();
//		queue2.clear();
//
//
//		TCPClientChannel client1 = TCPClientChannel.createTCPClientChannel((TCPClientIOChannelType)configMap.get("input_1"),
//				MessageFormatEnum.EC2BMC_INTERNAL,
//				new QueueMessageHandler<MessageBuffer>(queue1), 
//				new SimpleStreamIO());
//
//		TCPClientChannel client2 = TCPClientChannel.createTCPClientChannel((TCPClientIOChannelType)configMap.get("input_2"), 
//				MessageFormatEnum.EC2BMC_INTERNAL,
//				new QueueMessageHandler<MessageBuffer>(queue2), 
//				new SimpleStreamIO());
//
//		test(client1, client2, new TCPServerFactory() {
//
//			@Override
//			public TCPServerChannel createTCPServer() throws ChannelException {
//				return TCPServerChannel.createTCPServerChannel((TCPIOChannelType)configMap.get("output"), 
//						MessageFormatEnum.EC2BMC_INTERNAL,
//						null, 
//						new SimpleStreamIO());
//			}
//		});		
//	}
}
