/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
package commons.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

/**
 * Concurrent observable mixin implementation, capable of multiple threads concurrently notifying the subject
 * @param <T>
 */
public class ConcurentObservableMixin<T> implements Observable<T> {

	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    protected final Lock readLock = readWriteLock.readLock();
    protected final Lock writeLock = readWriteLock.writeLock();
	
	@SuppressWarnings("unchecked")
	private Collection<T> listeners = (Collection<T>) Collections.synchronizedCollection(new ArrayList<>());

	/* (non-Javadoc)
	 * @see net.oauth2.client.Observable#registerListener(T)
	 */
	@Override
	public T attach(T listener) {
		// Add the listener to the list of registered listeners
		// Lock the list of listeners for writing
        this.writeLock.lock();
        try {
            // Add the listener to the list of registered listeners
            this.listeners.add(listener);
        }
        finally {
            // Unlock the writer lock
            this.writeLock.unlock();
        }
        return listener;
	}

	/* (non-Javadoc)
	 * @see net.oauth2.client.Observable#unregisterListener(T)
	 */
	@Override
	public void detach(T listener) {
		// Remove the listener from the list of the registered listeners
		// Lock the list of listeners for writing
        this.writeLock.lock();
        try {
            // Remove the listener from the list of the registered listeners
            this.listeners.remove(listener);
        }
        finally {
            // Unlock the writer lock
            this.writeLock.unlock();
        }
	}

	/* (non-Javadoc)
	 * @see net.oauth2.client.Observable#notifyListeners(java.util.function.Consumer)
	 */
	@Override
	public void notify(Consumer<? super T> algorithm) {
		// Execute some function on each of the listeners
		// Lock the list of listeners for reading
        this.readLock.lock();
        try {
        	this.listeners.forEach(algorithm);
        }
        finally {
            // Unlock the reader lock
            this.readLock.unlock();
        }
	}

}
