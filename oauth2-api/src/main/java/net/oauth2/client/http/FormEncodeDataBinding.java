package net.oauth2.client.http;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import commons.http.WwwFormUrlEncodedCodec;
import net.oauth2.ParametersMap;

public class FormEncodeDataBinding extends WwwFormUrlEncodedCodec {
	
	public FormEncodeDataBinding() {}

	//Encoding

	@Override
	public <T> FormEncodeDataBinding with(String mappedName, Serializer<T> serializer) {
		return (FormEncodeDataBinding) super.with(mappedName, serializer);
	}
	
	public static class CollectionSerializer<T> implements Serializer<Collection<T>> {
		@Override
		public String serialize(Collection<T> value) {
			return formatToDelimitedString(value);
		}
		private final static String SP = " ";
		public static <T> String formatToDelimitedString(Collection<T> scopes){
			final StringBuilder sb = new StringBuilder();
			scopes.stream().forEach((el)-> {
				sb.append(el.toString()).append(SP);	
			});
			return sb.toString().trim();
		}
	}
	
	public <T extends ParametersMap> String encode(final T bag, @SuppressWarnings("rawtypes") Map<String, Serializer> serializersMappings) {
		Map<String, Object> grantRequestFormFrields = null;
		try {
			grantRequestFormFrields = bag.map();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return this.encodeStream(grantRequestFormFrields.entrySet().stream(), serializersMappings);
	}
	
	// Decoding
	
	@Override
	public <T> FormEncodeDataBinding with(String mappedName, Deserializer<T> deserializer) {
		return (FormEncodeDataBinding) super.with(mappedName, deserializer);
	}
	
	public static class CollectionDeserializer<T> implements Deserializer<Collection<T>> {
		private String delimiterPattern;
		private boolean returnUnmodifiable;
		public CollectionDeserializer() {}
		@SuppressWarnings("unchecked")
		@Override
		public Collection<T> deserialize(String input) {
			return (Collection<T>) parseDelimitedString(input, this.delimiterPattern, this.returnUnmodifiable);
		}
		public static Collection<String> parseDelimitedString(String delimitedString, String delimiterPattern, boolean returnUnmodifiable){
			if(delimiterPattern == null)
				delimiterPattern = "\\s+";
			String[] enumerationArr = delimitedString.split(delimiterPattern);
			Collection<String> list = Stream.of(enumerationArr).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
			if(returnUnmodifiable)
				list = Collections.unmodifiableCollection(list);
			return list;
		}
		public CollectionDeserializer<T> delimiter(String delimiterPattern) {
			this.delimiterPattern = delimiterPattern;
			return this;
		}
		public CollectionDeserializer<T> unmodifiable(boolean returnUnmodifiable) {
			this.returnUnmodifiable = returnUnmodifiable;
			return this;
		}
	}
	
	/**
	 * Requires a constructor with a single Map<String, ?> parameter.
	 * 
	 * @param encodedString
	 * @param targetClass
	 * @param deserializersMapping
	 * @return
	 * @throws IOException
	 */
	public <T> T from(final String encodedString, final Class<T> targetClass, @SuppressWarnings("rawtypes") final Map<String, Deserializer> deserializersMapping) throws IOException{
		Map<String, ?>  parameters = from(encodedString, deserializersMapping);
		//TODO: the requirement for constructor with Map argument is implicit and cannot be enforced by interface unfortunately 
		Constructor<T> constructor;
		try {
			constructor = targetClass.getConstructor(Map.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IOException(e);
		}
		T result = null;
		try {
			constructor.setAccessible(true);
			result = constructor.newInstance(parameters);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	

}
