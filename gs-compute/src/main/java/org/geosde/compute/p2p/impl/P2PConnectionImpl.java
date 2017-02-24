package org.geosde.compute.p2p.impl;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.geosde.compute.p2p.P2PConnection;
import org.geosde.compute.p2p.P2PMessage;

/**
 * Socket-based implementation of the {@link P2PConnection} interface.
 * 
 * @author Michael Pantazoglou
 *
 */
public class P2PConnectionImpl implements P2PConnection {
	
	private Socket socket;
	
	/**
	 * Constructor.
	 * 
	 * @param socket the connection socket
	 */
	public P2PConnectionImpl(Socket socket) {
		super();
		this.socket = socket;
	}

	@Override
	public void send(P2PMessage msg) throws Exception {
		
		// send message
		ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		out.writeObject(msg);
		out.flush();
		
		// receive dummy reply
		try {
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			in.readObject();
		} catch (EOFException ignore) {}
	}

	@Override
	public P2PMessage receive() throws Exception {
		
		// receive message from sender
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		Object message = in.readObject();
		
		// send back a dummy reply
		try {
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(new P2PMessage());
			out.flush();
		} catch (EOFException ignore) {}
		
		return (P2PMessage) message;
	}

	@Override
	public void close() throws Exception {
		if (socket != null) {
			socket.shutdownInput();
			socket.shutdownOutput();
			socket.close();
		}
	}

}
