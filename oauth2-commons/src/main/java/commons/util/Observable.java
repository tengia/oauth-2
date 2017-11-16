/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
package commons.util;

import java.util.function.Consumer;

public interface Observable<T> {

	T attach(T observer);

	void detach(T observer);

	void notify(Consumer<? super T> algorithm);
	
}