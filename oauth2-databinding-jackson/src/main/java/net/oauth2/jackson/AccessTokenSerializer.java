package net.oauth2.jackson;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

import net.oauth2.AccessToken;

public class AccessTokenSerializer extends StdSerializer<AccessToken> {

	protected AccessTokenSerializer(JavaType type) {
		super(type);
	}

	private static final long serialVersionUID = 1L;

	@Override
	public void serialize(AccessToken token, JsonGenerator gen, SerializerProvider provider) throws IOException {
		if (token == null) {
			return;
		}
		Map<String, Object> map = null;
		try {
			map = token.map();
		} catch (Exception e) {
			provider.reportMappingProblem(e, e.getMessage(), "");
		}
		JavaType mapType = provider.getTypeFactory().constructMapType(Map.class, String.class, Object.class); 
		TypeSerializer vts = provider.findTypeSerializer(provider.getTypeFactory().constructSimpleType(String.class, null));
		JsonSerializer<Object> keySer = provider.findKeySerializer(String.class, null);
		JsonSerializer<Object> valueSer = provider.findValueSerializer(Object.class); 
		final MapSerializer mapSerializer = MapSerializer.construct((Set<String>)null, mapType, false, vts, valueSer, keySer, null);
		mapSerializer.serialize(map , gen, provider);
	}

	public static final SimpleModule REGISTER(SimpleModule module){
		AccessTokenSerializer ser = new AccessTokenSerializer(TypeFactory.defaultInstance().constructSimpleType(AccessToken.class, null));
		module.addSerializer(ser.handledType(), ser);
		return module;
	}

}
