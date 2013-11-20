package se.skltp.ei.intsvc.subscriber.api;

import java.util.List;

public interface SubscriberCache {

	List<Subscriber> getSubscribers();
	boolean isInitialized();
	void reset();
	void initialize(List<Subscriber> subscribers);

}