/**
 * Package that holds all the message types that are used in the communication
 * with a Freenet Node.
 *
 * <h2>Usage</h2>
 *
 * This library was designed to implement the full range of the Freenet Client
 * Protocol, Version 2.0. At the moment the library provides a rather low-level
 * approach, wrapping each FCP message into its own object but some kind of
 * high-level client that does not require any interfaces to be implemented
 * will probably provided as well.
 *
 * First, create a connection to the node:
 *
 * <pre>
 * FcpConnection fcpConnection = new FcpConnection();
 * </pre>
 *
 * Now implement the {@link net.pterodactylus.fcp.FcpListener} interface
 * and handle all incoming events.
 *
 * <pre>
 * public class MyClass implements FcpListener {
 *
 * 	public void receivedProtocolError(FcpConnection fcpConnection, ProtocolError protocolError) {
 * 		…
 * 	}
 *
 * 	// implement all further methods here
 *
 * }
 * </pre>
 */

package net.pterodactylus.fcp;

