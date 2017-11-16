/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
package commons.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Encoder/decoder for the x-www-form-urlencoded data format. 
 *
 */
public class WwwFormUrlEncodedCodec {

	@SuppressWarnings("rawtypes")
	private Map<String, Serializer> globalSerializerMappings = new HashMap<>();
	@SuppressWarnings("rawtypes")
	private Map<String, Deserializer> globalDeseralizerMappings = new HashMap<>();

	public static interface Serializer<T> {
		String serialize(T value); 
	}

	public static interface Deserializer<T> {
		T deserialize(String input);
	}

	public <T> WwwFormUrlEncodedCodec with(String mappedName, Serializer<T> serializer) {
		this.globalSerializerMappings.put(mappedName, serializer);
		return this;
	}

	@SuppressWarnings("rawtypes")
	protected Map<String,Serializer> resolveSerializerMappings(Map<String, Serializer> serializersMappings) {
		//resolve mappings to use. We start with globals if any, then override them with specifically provided as argument in this method if any
		Map<String,Serializer> mappings = this.globalSerializerMappings;
		if(serializersMappings != null){
			mappings = Stream.of(serializersMappings)
				        .map(Map::entrySet)
				        .flatMap(Set::stream)
				        .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
				        						  (v1, v2) -> v2,
				                				  () -> new HashMap<>(this.globalSerializerMappings))); 
		}
		return mappings;
	}

	@SuppressWarnings("unchecked")
	protected Entry<String, Object> encodeEntry(final Entry<String, Object> entry, @SuppressWarnings("rawtypes") final Map<String,Serializer> mappings) {
		String encodedValue = null;
		//encode values with mapped serializers if any
		encodedValue = mappings.entrySet().stream()
						.filter(serializerMapping -> entry.getKey().equals(serializerMapping.getKey()))
						.map(Map.Entry::getValue)
						.findFirst()
						.flatMap((serializer)->{
							return Optional.ofNullable((String)serializer.serialize(entry.getValue()));
						})
	                    .orElseGet(()-> null);
		if(encodedValue == null) {
			//no special serializers were used. We will use toString() if the value is not a string already 
			if(!String.class.isAssignableFrom(entry.getValue().getClass()))
	    		encodedValue = entry.getValue().toString();
			else
				encodedValue = (String) entry.getValue();//TODO: refactor to avoid unnecessary change of the entry
		}
		entry.setValue(encodedValue);
		return entry;
	}

	/**
	 * Transform a stream of key-value pairs into a www.form-encoded string.
	 * @param stream
	 * @return
	 */
	public String encodeStream(final Stream<Entry<String, Object>> stream, @SuppressWarnings("rawtypes") Map<String, Serializer> serializersMappings) {
		@SuppressWarnings("rawtypes")
		final Map<String,Serializer> mappings = resolveSerializerMappings(serializersMappings); 
		String encoded = stream.filter(parameterEntry -> parameterEntry.getValue() != null)
		.map(parameterEntry -> {
			return encodeEntry(parameterEntry, mappings);
		})
		.map((entry) -> {
			return entry.getKey() + "=" + entry.getValue();
		})
		.collect(Collectors.joining("&"));		
		return encoded;
	}

	public WwwFormUrlEncodedCodec() {
		super();
	}

	public <T> WwwFormUrlEncodedCodec with(String mappedName, Deserializer<T> deserializer) {
		globalDeseralizerMappings.put(mappedName, deserializer);
		return this;
	}
	
	public final class Tuple implements Map.Entry<String, String> {
		String k, v;
		Tuple(String key, String value){
			this.k = key;
			this.v = value;
		}
		@Override
		public String getKey() {
			return this.k;
		}
		@Override
		public String getValue() {
			return this.v;
		}
		@Override
		public String setValue(String value) {
			String old = this.v;
			this.v = value;
			return old;
		}		
	}

	public Stream<Tuple> decodeStream(final String encodedString) {
		if(encodedString == null)
			throw new IllegalArgumentException();
		String decodedString = null;
		try {
			decodedString = URLDecoder.decode(encodedString, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) { throw new RuntimeException(e);}
		return Stream.of(decodedString.split("&"))
		.map((entry)->{ 
			String[] kv = entry.split("=");
			return new Tuple(kv[0], kv[1]);
		});
	}

	@SuppressWarnings("rawtypes")
	protected Map<String,Deserializer> resolveDeserializerMappings(Map<String, Deserializer> deserializersMappings) {
		//resolve mappings to use. We start with globals if any, then override them with specifically provided as argument in this method if any
		Map<String,Deserializer> mappings = this.globalDeseralizerMappings;
		if(deserializersMappings != null){
			mappings = Stream.of(deserializersMappings)
			        .map(Map::entrySet)
			        .flatMap(Set::stream)
			        .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
			        						  (v1, v2) -> v2,
			                				  () -> new HashMap<>(this.globalDeseralizerMappings)));
		}
		return mappings;
	}

	protected Object decodeEntry(Tuple tuple, @SuppressWarnings("rawtypes") final Map<String, Deserializer> deserializersMappings) {
		Object result = deserializersMappings.entrySet().stream()
							.filter(entry -> tuple.getKey().equals(entry.getKey()))
							.map(Map.Entry::getValue)
		                    .findFirst()
		                    .flatMap((deser)->{
		                    	return Optional.ofNullable(deser.deserialize(tuple.getValue()));
		                    })
		                    .orElseGet(()->null);
		if(result == null)
			result = tuple.getValue();
		return result;
	}

	public Map<String, ?> from(final String encodedString, @SuppressWarnings("rawtypes") final Map<String, Deserializer> deserializersMappings) {
		@SuppressWarnings("rawtypes")
		final Map<String, Deserializer> mappings = resolveDeserializerMappings(deserializersMappings);
		Map<String, ?> parameters = this.decodeStream(encodedString)
		.collect(Collectors.toMap(Tuple::getKey, (tuple)-> {
			return decodeEntry(tuple, mappings);
		}));
		return parameters;
	}

}