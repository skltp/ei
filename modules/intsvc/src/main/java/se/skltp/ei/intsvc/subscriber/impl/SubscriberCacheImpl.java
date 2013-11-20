package se.skltp.ei.intsvc.subscriber.impl;

import java.util.ArrayList;
import java.util.List;

import se.skltp.ei.intsvc.subscriber.api.Subscriber;
import se.skltp.ei.intsvc.subscriber.api.SubscriberCache;

public class SubscriberCacheImpl implements SubscriberCache {

	private List<Subscriber> subscribers;
	private boolean initialized;
	
	public SubscriberCacheImpl() {
		reset();
	}

	@Override
	public List<Subscriber> getSubscribers() {
		// TODO. Add lazy read of subscribers here?
		// TODO. Save to local file if successful read
		// TODO. Log Warn and load from local file if lazy read fails
		// TODO. Throw and log error if load from both TAK and local file fails?
		return subscribers;
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public void reset() {
		subscribers = new ArrayList<Subscriber>();
		initialized = false;
	}

	@Override
	public void initialize(List<Subscriber> subscribers) {
		this.subscribers = subscribers;
		initialized = true;
	}
}
