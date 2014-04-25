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

public class pmediator extends Verticle {

	public void start() {

		EventBus eb = vertx.eventBus();

		// Event Receved
		eb.registerHandler("proxy.connect", new Handler< Message<String> >(){
			public void handle( Message<String> message ){
				System.out.println("connect " + message.body() );
			}
		});

		eb.registerHandler("proxy.recv.client", new Handler< Message<String> >(){
			public void handle( Message<String> message ){
				System.out.println("recv " + message.body() );
			}
		});

		eb.registerHandler("proxy.recv.server", new Handler< Message<String> >(){
			public void handle( Message<String> message ){
				System.out.println("recv " + message.body() );
			}
		});

		eb.registerHandler("proxy.close", new Handler< Message<String> >(){
			public void handle( Message<String> message ){
				System.out.println("close " + message.body() );
			}
		});
	}

}