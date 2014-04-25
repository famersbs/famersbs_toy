import org.vertx.java.core.Handler;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.net.NetSocket;
import org.vertx.java.platform.Verticle;
import org.vertx.java.core.buffer.Buffer;

import org.vertx.java.core.net.NetServer;
import org.vertx.java.core.net.NetClient;
import org.vertx.java.core.net.NetSocket;

import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;

import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;

import org.vertx.java.core.json.JsonObject;

public class proxy extends Verticle {

	private static String	target_ip = "192.168.0.170";
	private static int		target_port = 22;

	private NetServer server;
	private EventBus	eb;

	private static int seq_num = 0;

	/************************************
	 * Key registry and unregistry
	 ************************************/
	public String getConnectionKey(){
		++ seq_num;
		return "test_" + seq_num;
	}
	public void releaseConnectionKey( String key ){
	}


	public interface proxyCloser
	{
		void closeProxy();
	}

	public class AutoCloseListener implements Handler<Void>
	{
		private proxyCloser closeTarget;

		public AutoCloseListener( proxyCloser closeTarget ){
			this.closeTarget = closeTarget;
		}

		public void handle(Void v) {
			closeTarget.closeProxy();
		}
	}

	public class ProxyDataListener implements Handler<Buffer>
	{
		private NetSocket target;
		private String event_id;
		private String con_key;

		public ProxyDataListener( NetSocket target, String event_id, String con_key ){
			this.target = target;
			this.event_id = event_id;
			this.con_key = con_key;
		}
		public void handle(Buffer buffer) {
			eb.send(event_id, con_key );
			target.write(buffer);
		}
	}

	public class ClientListener implements AsyncResultHandler<NetSocket>, proxyCloser
	{

		private ServerListener server;
		private NetSocket client;
		private String		con_key;

		public ClientListener( ServerListener server, String con_key ){
			this.server = server;
			this.con_key = con_key;
		}
		
		public void handle( AsyncResult<NetSocket> socket ) {

			if( socket.succeeded() ){
				client = socket.result();
				server.setClientSocket( client );
				client.dataHandler( new ProxyDataListener( server.getServerSocket(), "proxy.recv.client", con_key ) );
				client.endHandler( new AutoCloseListener( this ) );

				
			}else{
				socket.cause().printStackTrace();
			}
		}
		
		public void closeProxy(){
			System.out.println("close From Client..." );
			server.getServerSocket().close();
		}

	};

	public class ServerListener implements Handler<Buffer> , proxyCloser
	{

		private NetSocket	server;
		private NetSocket	client = null;
		private NetClient	client_client;

		private Buffer		beforeBuffer;
		private String		con_key;

		public ServerListener( NetSocket server ){
			
			con_key = getConnectionKey();

			this.server = server;

			// create before buffer
			beforeBuffer = new Buffer();

			// CloseHandler
			server.endHandler( new AutoCloseListener( this ) );

			// Client
			client_client = vertx.createNetClient();
			
			client_client.connect( proxy.target_port, proxy.target_ip, 
							new ClientListener( this, con_key ) );

			// Event dispatch
			eb.send("proxy.connect", con_key );

		}

		public void handle(Buffer buffer) {
			if( null == client ){
				// 아직 생성 전이라면 before buffer에 쌓는다.
				beforeBuffer.appendBuffer(buffer);
				System.out.println("Client Connect before packet " + buffer.length() );
			}else{
				eb.send("proxy.recv.server", con_key );
				client.write(buffer);
			}
		}

		public NetSocket getServerSocket( ){
			return server;
		}
		public void setClientSocket( NetSocket client ){
			this.client = client;
			System.out.println("Client before packet Send " + beforeBuffer.length() );
			client.write( beforeBuffer );
			beforeBuffer = null;
		}

		public void closeProxy(){

			eb.send( "proxy.close", con_key );
			releaseConnectionKey( con_key );
			System.out.println("close From Server..." );
			if( null != client ){
				client.close();
			}
		}
		
	};

	public void start() {

		eb = vertx.eventBus();

		// create server
		server = vertx.createNetServer();
		server.setTCPNoDelay( true );
		server.setTCPKeepAlive( true );
		server.connectHandler(new Handler<NetSocket>() {

            @Override
            public void handle(NetSocket netSocket) {
                // 메시지 처리(그냥 메아리)
                netSocket.dataHandler( new ServerListener( netSocket ) );
            }

        }).listen(1234);


		// Mediator Run 
		JsonObject config = container.config();
		container.deployVerticle( "pmediator.java", config );

	}
}