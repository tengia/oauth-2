package net.oauth2.client.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.oauth2.ParametersMap;
import net.oauth2.client.http.FormEncodeDataBinding.CollectionDeserializer;
import net.oauth2.client.http.FormEncodeDataBinding.CollectionSerializer;

//TODO: under construction
public class FormEncodeDataBindingTest {

	private static class TestBag implements ParametersMap {
		Map<String, Object> map;
		public TestBag(Map<String, Object> map) {
			this.map = map;
		}
		@Override
		public Map<String, Object> map() throws Exception {
			return this.map;
		}
	}
	
	@Test
	public void testEncodeStream() {
		FormEncodeDataBinding binding = new FormEncodeDataBinding()
		.with("scope", new CollectionDeserializer<String>().unmodifiable(true))
		.with("scope", new CollectionSerializer<Collection<String>>());
		
		Map<String, Object> params = new HashMap<>();
		params.put("a", "test");
		params.put("b", "test1");
		Collection<String> c = new ArrayList<>();
		c.add("a");c.add("b");c.add("c");
		params.put("scope", c);
		TestBag b = new TestBag(params);
		
		String encoded = binding.encode(b, null);
		System.out.println(encoded);
	
	}

	@Test
	public void testEncode() {
		FormEncodeDataBinding binding = new FormEncodeDataBinding()
				.with("scope", new CollectionDeserializer<String>().unmodifiable(true))
				.with("scope", new CollectionSerializer<Collection<String>>());
				
				Map<String, Object> params = new HashMap<>();
				params.put("a", "test");
				params.put("b", "test1");
				Collection<String> c = new ArrayList<>();
				c.add("a");c.add("b");c.add("c");
				params.put("scope", c);
				TestBag b = new TestBag(params);
				
				String encoded = binding.encode(b, null);
				System.out.println(encoded);
	}

	@Test
	public void testDecodeStream() throws Exception {
		FormEncodeDataBinding binding = new FormEncodeDataBinding()
				.with("scope", new CollectionDeserializer<String>().unmodifiable(true))
				.with("scope", new CollectionSerializer<Collection<String>>());
		String encoded = "a=1&b=2&c=a b c";
		ParametersMap decoded = binding.from(encoded, TestBag.class, null);
		System.out.println(decoded.map());
	}

	@Test
	public void testFromStringMapOfStringDeserializer() throws Exception {
		FormEncodeDataBinding binding = new FormEncodeDataBinding()
				.with("scope", new CollectionDeserializer<String>().unmodifiable(true))
				.with("scope", new CollectionSerializer<Collection<String>>());
		String encoded = "a=1&b=2&c=a b c";
		ParametersMap decoded = binding.from(encoded, TestBag.class, null);
		System.out.println(decoded.map());
	}

	@Test
	public void testFromStringClassOfTMapOfStringDeserializer() throws Exception {
		FormEncodeDataBinding binding = new FormEncodeDataBinding()
				.with("scope", new CollectionDeserializer<String>().unmodifiable(true))
				.with("scope", new CollectionSerializer<Collection<String>>());
		String encoded = "a=1&b=2&c=a b c";
		ParametersMap decoded = binding.from(encoded, TestBag.class, null);
		System.out.println(decoded.map());
	}

}
