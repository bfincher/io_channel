package com.fincher.io_channel.tcp;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

import okio.Buffer;
import okio.BufferedSource;
import okio.Okio;

import org.apache.log4j.Logger;

import com.fincher.io_channel.ChannelException;

public class ReceiveRunnable extends AbstractReceiveRunnable {
	
	private static final Logger logger = Logger.getLogger(ReceiveRunnable.class);	
	
	/** The byte array used to receive data */		
	
	/** The length of headers that are used to determine the length of messages */
	private final int HEADER_LENGTH;				
	
	private final StreamIOIfc streamIo;	
	
	private final BufferedSource inputStream;
	
	/** Creates a new ReceiveRunnable object
	 * @param id The ID of this ReceiveRunnable
	 * @param socket The TCP Socket
	 * @throws ChannelException
	 */
	public ReceiveRunnable(String id,
			Socket socket,
			StreamIOIfc streamIo,
			TCPChannel parent) throws ChannelException {
		super(id, socket, parent);
		this.streamIo = streamIo;
		HEADER_LENGTH = streamIo.getHeaderLength();
		
		try {
			this.inputStream = Okio.buffer(Okio.source(socket.getInputStream()));
		} catch (IOException e) {
			throw new ChannelException(e);
		}
	}									
	
	/** The body of the thread.  Reads data from the socket and places the received messages on a queue
	 */
	@Override
	public void run() {
		try {
			
			if (logger.isTraceEnabled())
				logger.trace(getId() + " Reading header (length = " + HEADER_LENGTH + ")");
			
			byte[] headerBytes;
			{
				Buffer buf = new Buffer();
				inputStream.readFully(buf, HEADER_LENGTH);
			
				headerBytes = buf.readByteString(HEADER_LENGTH).toByteArray();
			}
			
			final int MESSAGE_LENGTH = streamIo.getMessageLength(headerBytes);
			
			if (logger.isTraceEnabled())
				logger.trace(getId() + " message length = " + MESSAGE_LENGTH);
			
			int bytesToRead;
			Buffer buf = new Buffer();
			if (streamIo.headerPartOfMessage()) {
				buf.write(headerBytes);
				bytesToRead = MESSAGE_LENGTH - HEADER_LENGTH;
			} else {
				bytesToRead = MESSAGE_LENGTH;
			}											
			
			if (logger.isDebugEnabled())
				logger.debug("reading length " + bytesToRead);
			
			inputStream.readFully(buf, bytesToRead);						
			messageReceived(buf.readByteString(bytesToRead).toByteArray());
			buf.close();
		}
		catch (EOFException eofe) {
			logger.warn("end of stream reached");
			
			try {
				closeSocket();
			}
			catch (ChannelException ce) {
				logger.error(ce.getMessage(), ce);
			}
		}
		catch (IOException ioe) {
			logger.error(ioe.getMessage(), ioe);
			
			try {
				closeSocket();
			}
			catch (ChannelException ce) {
				logger.error(ce.getMessage(), ce);	
			}
		}		
	}		
}
