package net.oauth2.gson;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

//https://stackoverflow.com/a/11272452
public class WhitespaceDelimitedCollectionTypeAdapterFactory<C extends Collection<String>> implements TypeAdapterFactory {

	private final Class<?> customizedClass;

	public WhitespaceDelimitedCollectionTypeAdapterFactory(Class<?> customizedClassType) {
		this.customizedClass = customizedClassType;
	}

	@SuppressWarnings("unchecked") // we use a runtime check to guarantee that 'C' and 'T' are equal
	public final <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
		return type.getRawType() == customizedClass || customizedClass.isAssignableFrom(type.getRawType())
				? (TypeAdapter<T>) customizeTypeAdapter(gson, (TypeToken<C>) type) : null;
	}

	private TypeAdapter<C> customizeTypeAdapter(final Gson gson, TypeToken<C> type) {
		final TypeAdapter<C> delegate = gson.getDelegateAdapter(this, type);
		final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
		return new TypeAdapter<C>() {
			@Override
			public void write(JsonWriter out, C value) throws IOException {
				JsonElement tree = delegate.toJsonTree(value);
				if(beforeWrite(value, tree, out)){
					elementAdapter.write(out, tree);	
				}
			}

			@Override
			public C read(JsonReader in) throws IOException {
				
				JsonElement tree = elementAdapter.read(in);
				
				String enumerationStr = tree.getAsString();
				String[] enumerationArr = enumerationStr.split("\\s+");
				@SuppressWarnings("unchecked")
				C collection = (C) Collections.unmodifiableList(Arrays.asList(enumerationArr));
				afterRead(tree, collection); 
				return collection; 
			}
		};
	}

	private static final String SP = " ";
	/**
	 * Override this to muck with {@code toSerialize} before it is written to
	 * the outgoing JSON stream.
	 */
	protected boolean beforeWrite(C source, JsonElement toSerialize, JsonWriter out) {
		final StringBuilder scopesBuilder = new StringBuilder();
		source.stream().forEach((e) -> {
			scopesBuilder.append(e).append(SP);
		});
		String scopeString = scopesBuilder.toString();
		scopeString = scopeString.trim();
		try {
			out.value(scopeString);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}		
		return false;
	}

	/**
	 * Override this to muck with {@code deserialized} before it parsed into the
	 * application type.
	 * @param collection 
	 */
	protected void afterRead(JsonElement deserialized, C collection) {
	}
	
	public static GsonBuilder REGISTER(GsonBuilder builder){
		return builder.registerTypeAdapterFactory(new WhitespaceDelimitedCollectionTypeAdapterFactory<Collection<String>>(Collection.class));
	}

}