package net.oauth2.jackson;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;

import net.oauth2.AccessToken;

public class AccessTokenDeserializer extends StdDeserializer<AccessToken> {

	private static final long serialVersionUID = 1L;

	public AccessTokenDeserializer(Class<AccessToken> tokenClass) {
		super(tokenClass);
	}

	public AccessTokenDeserializer(JavaType valueType) {
		super(valueType);
	}

	public AccessTokenDeserializer(StdDeserializer<AccessToken> src) {
		super(src);
	}

	@Override
	public AccessToken deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		HashMap<String, Object> map = p.readValueAs(new TypeReference<HashMap<String, Object>>() {});
		if(map!=null){
			if("".equals(map.get("scope"))){
				map.put("scope", null);
			}
			return new AccessToken(map);
		}
		return null;
	}

	public static final SimpleModule REGISTER(SimpleModule module){
		JavaType type = TypeFactory.defaultInstance().constructCollectionType(Collection.class, String.class);
		AccessTokenDeserializer deser = new AccessTokenDeserializer(type);		
		module.addDeserializer(AccessToken.class, deser);
		return module;
	}
	
}
