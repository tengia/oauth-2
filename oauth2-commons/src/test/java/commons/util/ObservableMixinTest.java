/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
package commons.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;

import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ObservableMixinTest {

	static interface Listener{
		void update();
	}
	
	@Mock
	Listener listener;
	
	@Test
	public void testListenerInvocation() {
		ObservableMixin<Listener> observable = new ObservableMixin<>();
		observable.attach(listener);
		observable.notify(listener->listener.update());
		verify(listener).update();
		verifyNoMoreInteractions(listener);
	}

}
