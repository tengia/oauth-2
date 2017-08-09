package commons.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ObservableMixin<T> implements Observable<T> {
	
	private List<T> listeners = new ArrayList<>();

	/* (non-Javadoc)
	 * @see net.oauth2.client.Observable#registerListener(T)
	 */
	@Override
	public T attach(T listener) {
		// Add the listener to the list of registered listeners
		this.listeners.add(listener);
		return listener;
	}

	/* (non-Javadoc)
	 * @see net.oauth2.client.Observable#unregisterListener(T)
	 */
	@Override
	public void detach(T listener) {
		// Remove the listener from the list of the registered listeners
		this.listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see net.oauth2.client.Observable#notifyListeners(java.util.function.Consumer)
	 */
	@Override
	public void notify(Consumer<? super T> algorithm) {
		// Execute some function on each of the listeners
		this.listeners.forEach(algorithm);
	}
}
