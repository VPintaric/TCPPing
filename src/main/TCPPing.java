package main;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import catcher.Catcher;

import pitcher.Pitcher;

import sun.security.util.HostnameChecker;

public class TCPPing {
	private static enum Type { PITCHER, CATCHER };
	private static String program_usage = "usage: java TCPPing <-p | -c> [-port <port_number>] " +
					                      "[-bind <ip_address>] [-mps <rate>] " + 
					                      "[-size <size_in_bytes>] [hostname]";
	
	// Variables containing program settings set to default
	// values in case some option isn't given
	private static Type type = null; // this has no default value, throw an error if type isn't set
	private static int port = 8888;
	private static String ip_address = null;
	private static int messages_per_sec = 1;
	private static int message_size = 300;
	private static String host_name = "localhost";
	
	private static void parseArgs(String[] args){
		boolean hostNameSet = false;
		
		for(int i = 0; i < args.length; i++){
			String arg = args[i];
			
			if(arg.startsWith("-")){
				switch(arg){
					case "-p":
						type = Type.PITCHER;
						break;
					case "-c":
						type = Type.CATCHER;
						break;
					case "-port":
						port = Integer.parseInt(args[++i]);
						if(port <= 0 || port > 65535){
							System.err.println("Invalid port number, port number should be in range [1, 65535]");
							System.exit(3);
						}
						break;
					case "-bind":
						ip_address = args[++i];
						break;
					case "-mps":
						messages_per_sec = Integer.parseInt(args[++i]);
						if(messages_per_sec <= 0){
							System.err.println("Invalid messages per second rate, should be an integer greater than zero");
							System.exit(4);
						}
						break;
					case "-size":
						message_size = Integer.parseInt(args[++i]);
						if(message_size < 50 || message_size > 3000){
							System.err.println("Invalid message size, should be an integer in range [50, 3000]");
							System.exit(5);
						}
						break;
					default:
						System.err.println("Ignoring unknown option: '" + arg + "'");
				}
			}
			else{
				if(hostNameSet){
					System.err.println("Multiple host names given!\n" + program_usage);
					System.exit(1);
				}
				hostNameSet = true;
				host_name = arg;
			}
		}
		
		if(type == null){
			System.out.println("Type option has to be given ('-p' or '-c')");
			System.exit(5);
		}
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception{
		parseArgs(args);
		
		System.out.println("TYPE       = " + type);
		System.out.println("PORT       = " + port);
		System.out.println("IP ADDRESS = " + ip_address);
		System.out.println("MPS        = " + messages_per_sec);
		System.out.println("SIZE       = " + message_size);
		System.out.println("HOST NAME  = " + host_name);
		
		if(type == Type.PITCHER){
			Pitcher p = new Pitcher(port, messages_per_sec, message_size, host_name);
			p.run();
		}
		else if(type == Type.CATCHER){
			Catcher c = new Catcher(port, ip_address);
			c.run();
		}
	}

}
