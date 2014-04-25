import org.vertx.java.core.Handler;
import org.vertx.java.core.net.NetSocket;
import org.vertx.java.platform.Verticle;
import org.vertx.java.core.buffer.Buffer;

import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;

import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;

import org.vertx.java.core.json.JsonObject;

import org.vertx.java.core.buffer.Buffer;

public class testmsg extends Verticle {

	
	public void start() {
		testobj obj = new testobj();
		obj.test1 = "test111";
		obj.test2 = 10;
		obj.test3 = "aaaa";

		Buffer buff = new Buffer();;

		buff.appendString("TEST");
		buff.appendInt( 3000 );
		buff.appendString("TEST2");


		EventBus eb = vertx.eventBus();
		eb.publish("test.obj", buff);
	}
}