package com.fincher.io_channel.tcp;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.log4j.Logger;

import com.fincher.io_channel.ChannelException;

public class ReceiveRunnable extends AbstractReceiveRunnable {
	
	private static final Logger logger = Logger.getLogger(ReceiveRunnable.class);	
	
	/** The byte array used to receive data */
	private byte[] buf = new byte[4096];			
	
	/** The length of headers that are used to determine the length of messages */
	private final int HEADER_LENGTH;				
	
	private final StreamIOIfc streamIo;	
	
	private final InputStream inputStream;
	
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
			this.inputStream = socket.getInputStream();
		} catch (IOException e) {
			throw new ChannelException(e);
		}
	}								
	
	/** Read data from the socket
	 * @param buf The byte array used to store the result of the read
	 * @param offset The index into buf used to store the result of the read
	 * @param length The number of bytes that should be read
	 * @return The param buf if buf was sized sufficiently.  Otherwise, a later byte array 
	 * with the contents of buf copied into it
	 * @throws IOException
	 * @throws EOFException
	 */
	private byte[] read(byte[] buf, int offset, int length) throws IOException, EOFException {
		
		// increase size of buf if necessary
		{
			int bytesRemaining = buf.length - offset;
			int delta = length - bytesRemaining;
			if (delta > 0) {
				byte[] newBuf = new byte[buf.length + delta + 100];
				System.arraycopy(buf, 0, newBuf, 0, buf.length);
				buf = newBuf;
			}
		}
		
		int remaining = length;
		while (remaining > 0) {
			try {
				int bytesRead = inputStream.read(buf, offset, remaining);
			
				if (bytesRead == -1) {
					throw new EOFException();
				}
			
				offset += bytesRead;
				remaining -= bytesRead;
			} catch (SocketTimeoutException e) {}
		}
				
		return buf;
	}		
	
	/** The body of the thread.  Reads data from the socket and places the received messages on a queue
	 */
	@Override
	public void run() {
		try {
			
			if (logger.isTraceEnabled())
				logger.trace(getId() + " Reading header (length = " + HEADER_LENGTH + ")");
			buf = read(buf, 0, HEADER_LENGTH);
			
			final int MESSAGE_LENGTH = streamIo.getMessageLength(buf);
			
			if (logger.isTraceEnabled())
				logger.trace(getId() + " message length = " + MESSAGE_LENGTH);
			
			int bytesToRead;
			int offset;
			if (streamIo.headerPartOfMessage()) {
				bytesToRead = MESSAGE_LENGTH - HEADER_LENGTH;
				offset = HEADER_LENGTH;
			}
			else {
				bytesToRead = MESSAGE_LENGTH;
				offset = 0;
			}				
			
			if (logger.isDebugEnabled())
				logger.debug("reading length " + bytesToRead);
			
			buf = read(buf, offset, bytesToRead);
			
			messageReceived(buf, 0, bytesToRead + offset);			
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
