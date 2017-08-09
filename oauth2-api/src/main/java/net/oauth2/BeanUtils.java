package net.oauth2;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class BeanUtils {

	public static <T> Map<String, Object> asMap(T grantRequest, Map<String, String> propertyMap) throws IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Map<String, Object> values = new HashMap<String, Object>();
		BeanInfo info = Introspector.getBeanInfo(grantRequest.getClass(), Object.class);
		for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
			// This will access public properties through getters
			String name = null;
			Method getter = pd.getReadMethod();
			// first try an explicit property map if any is provided.
			if (propertyMap != null && !propertyMap.isEmpty()) {
				name = propertyMap.get(pd.getName());
			}
			// try annotations if map didn't help
			if (name == null) {
				if (getter != null) {
					// first inspect the getter for this property for annotation
					OAuthPayloadProperty propertyAnnotation = getter.getAnnotation(OAuthPayloadProperty.class);
					if (propertyAnnotation != null) {
						name = propertyAnnotation.value();
					}
				}
			}
			// next, inspect the corresponding field for annotation.
			// This algorithm assumes that the field name equals the
			// bean property descriptor's name
			if (name == null) {
				String fieldName = pd.getName();
				try {
					Field field = grantRequest.getClass().getDeclaredField(fieldName);
					field.setAccessible(true);
					OAuthPayloadProperty annotation = field.getDeclaredAnnotation(OAuthPayloadProperty.class);
					if (annotation != null) {
						name = annotation.value();
					}
				} catch (NoSuchFieldException nsfe) {
					// silently ignore
				}
			}
			// finally, try using the name of the property field assuming it
			// follows a correct naming convention for payload
			// de-/serialization to work properly
			if (name == null) {
				name = pd.getName();
			}
			if (name != null) {
				Object value = null;
				if(getter != null) {
					value = getter.invoke(grantRequest);
				} else {
					try {
						Field field = grantRequest.getClass().getDeclaredField(name);
						field.setAccessible(true);
						value = field.get(grantRequest);
					} catch (NoSuchFieldException | IllegalAccessException e) {
						// silently ignore
					}
				}
				if (value != null)
					values.put(name, value);
			}
		}
		return values;
	}


}
