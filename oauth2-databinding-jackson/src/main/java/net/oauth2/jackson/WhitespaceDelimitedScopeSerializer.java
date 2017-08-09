package net.oauth2.jackson;

import java.io.IOException;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class WhitespaceDelimitedScopeSerializer extends StdSerializer<Collection<String>> {

	private static final long serialVersionUID = -1463700179768817262L;

	private static final String SP = " ";
	
	private static final ToStringSerializer stringSerializer = ToStringSerializer.instance;

	protected WhitespaceDelimitedScopeSerializer(JavaType type) {
		super(type);
	}
	
	@Override
	public void serialize(Collection<String> model, JsonGenerator jsonGen, final SerializerProvider serializer)
			throws IOException, JsonProcessingException {
		if (model == null) {
			return;
		}
		final StringBuilder scopesBuilder = new StringBuilder();
		model.stream().forEach((e) -> {
			scopesBuilder.append(e).append(SP);
		});
		String scopeString = scopesBuilder.toString();
		scopeString = scopeString.trim();
		stringSerializer.serialize(scopeString, jsonGen, serializer);
	}
	
	public static final SimpleModule REGISTER(SimpleModule module){
		WhitespaceDelimitedScopeSerializer ser = new WhitespaceDelimitedScopeSerializer(TypeFactory.defaultInstance().constructCollectionType(Collection.class, String.class));
		module.addSerializer(ser.handledType(), ser);
		return module;
	}

}
