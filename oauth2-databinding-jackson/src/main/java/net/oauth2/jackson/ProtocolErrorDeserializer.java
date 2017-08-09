package net.oauth2.jackson;

import java.io.IOException;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;

import net.oauth2.ProtocolError;

public class ProtocolErrorDeserializer extends StdDeserializer<ProtocolError> {

	private static final long serialVersionUID = 1L;
	
	public ProtocolErrorDeserializer() {
		super(TypeFactory.defaultInstance().constructSimpleType(ProtocolError.class, null));
	}
	
	protected ProtocolErrorDeserializer(JavaType valueType) {
		super(valueType);
	}

	@Override
	public ProtocolError deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		HashMap<String, Object> map = p.readValueAs(new TypeReference<HashMap<String, Object>>() {});
		if(map!=null){
			return new ProtocolError(map);
		}
		return null;
	}

	public static final SimpleModule REGISTER(SimpleModule module){
		JavaType type = TypeFactory.defaultInstance().constructSimpleType(ProtocolError.class, null);
		ProtocolErrorDeserializer deser = new ProtocolErrorDeserializer(type);		
		module.addDeserializer(ProtocolError.class, deser);
		return module;
	}
}
