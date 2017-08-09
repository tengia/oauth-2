package commons.util;

import java.util.function.Consumer;

public interface Observable<T> {

	T attach(T observer);

	void detach(T observer);

	void notify(Consumer<? super T> algorithm);
	
}