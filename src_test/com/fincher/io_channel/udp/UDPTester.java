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
	
//	private static final File SOURCE_UNICAST_CONFIG = new File("javaSrcTest/ec2bmc/ifc/io_channel/udp/test_unicast_config.xml");
//	private static final File DEST_UNICAST_CONFIG = new File("data/test_unicast_config.xml");
//	private static final File SCHEMA_FILE = new File("data/io_channel.xsd");
	
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
	
//	/** Method name is self explanatory */
//	public void testUnicastFromConfig() {				
//		try {
//			UDPChannel output = UDPChannel.createUDPChannel((OutputUDPIOChannelType)configMap.get("output"), MessageFormatEnum.EC2BMC_INTERNAL);
//			
//			UDPChannel input = UDPChannel.createUDPChannel((InputUDPIOChannelType)configMap.get("input"),
//					MessageFormatEnum.EC2BMC_INTERNAL,
//					new QueueMessageHandler<MessageBuffer>(messageQueue));
//			
//			test(input, output, new TestDataFactory());
//		}
//		catch (Throwable t) {
//			t.printStackTrace();
//			fail();
//		}
//	}
//	
//	/** Method name is self explanatory */
//	public void testMulticastFromConfig() {
//		try {			
//			UDPMulticastChannel output = 
//				UDPMulticastChannel.createOutputUDPMulticastChannel((UDPMulticastIOChannelType)configMap.get("output_multicast"), MessageFormatEnum.EC2BMC_INTERNAL);
//			
//			UDPMulticastChannel input = 
//				UDPMulticastChannel.createInputUDPMulticastIOChannel((UDPMulticastIOChannelType)configMap.get("input_multicast"), 
//						MessageFormatEnum.EC2BMC_INTERNAL,
//						new QueueMessageHandler<MessageBuffer>(messageQueue));						
//			
//			test(input, output, new TestDataFactory());
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//	}
}
