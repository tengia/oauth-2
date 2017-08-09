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
