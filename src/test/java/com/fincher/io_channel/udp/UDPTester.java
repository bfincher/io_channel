package com.fincher.io_channel.udp;

import static junit.framework.Assert.fail;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fincher.io_channel.IOChannelTesterBase;
import com.fincher.io_channel.MessageBuffer;
import com.fincher.io_channel.QueueMessageHandler;
import com.fincher.io_channel.TestDataFactoryIfc;

/** Test UDP sockets
 * 
 * @author Brian Fincher
 *
 */
public class UDPTester extends IOChannelTesterBase<MessageBuffer> {
	
	
	private static InetSocketAddress localAddress0;
	private static InetSocketAddress localAddress5000;
	private static InetSocketAddress multicastAddress;
	
	/** Method name is self explainatory 
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUp() throws Exception {
//		loadConfigMap(SOURCE_UNICAST_CONFIG, DEST_UNICAST_CONFIG, SCHEMA_FILE);
		localAddress0 = new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 0);
		localAddress5000 = new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 5000);
		multicastAddress = new InetSocketAddress(InetAddress.getByName("239.1.1.1"), 5000);				
	}					
	
	/** Method name is self explainatory */
	@AfterClass
	public static void tearDown() {
//		DEST_UNICAST_CONFIG.delete();
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
	
	/** Method name is self explainatory
	 * 
	 */
	@Test
	public void testMulticast() {
		try {			
			UDPMulticastChannel output = new UDPMulticastChannel("output",
					localAddress0, multicastAddress);
			
			UDPMulticastChannel input = new UDPMulticastChannel("input",
					new QueueMessageHandler<MessageBuffer>(messageQueue), 
					localAddress5000, 
					InetAddress.getByName("239.1.1.1"));						
			
			test(input, output, new TestDataFactory());
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/** Method name is self explanatory
	 * 
	 */
	@Test
	public void testUnicast() {
		
		try {
			UDPChannel output = new UDPChannel("output",
					localAddress0, 
					localAddress5000);
			
			UDPChannel input = new UDPChannel("input", 
					new QueueMessageHandler<MessageBuffer>(messageQueue), 
					localAddress5000);
			test(input, output, new TestDataFactory());
		} 
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
