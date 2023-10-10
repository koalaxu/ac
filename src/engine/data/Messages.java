package ac.engine.data;

import java.util.LinkedList;
import java.util.Queue;
import ac.data.base.Date;
import ac.data.base.Pair;

public class Messages extends Data {

	protected Messages(DataAccessor data, Queue<Pair<Date, String>> message_queue) {
		super(data);
		this.message_queue = message_queue;
	}

	public void AddMessage(String text) {
		Pair<Date, String> message = new Pair<Date, String>(accessor.GetDate().CreateDate(0), text);
		message_queue.add(message);
		message_cache.add(message);
		while (message_cache.size() > kMaxCachedMessage) {
			message_cache.poll();
		}
	}
	
	public Pair<Date, String> PullMessage() {
		return message_queue.poll();
	}
	
	public LinkedList<Pair<Date, String>> GetCachedMessages() {
		return message_cache;
	}
	
	private Queue<Pair<Date, String>> message_queue;
	private LinkedList<Pair<Date, String>> message_cache = new LinkedList<Pair<Date, String>>();
	private static final int kMaxCachedMessage = 100;
}
