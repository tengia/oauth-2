/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */

package net.oauth2.client.http;

import java.io.IOException;

import net.oauth2.AccessToken;
import net.oauth2.ProtocolError;

/**
 * An abstraction for the data binding operations performed by
 * TokenServiceHttpClient. Implementations could use an existing data mapping
 * framework such as Gson or Jackson to handle e.g. json string-to-java
 * mappings. Since OAuth2.0 implemntations are loosely following the standard
 * more custom solutions might be involved just as well. For example, Github
 * returns a form url encoded string instead of JSON. This abstraction provides
 * for both 1) reuse of the mapping technology already in use in an organization
 * and 2)supporting non-standard payloads
 * 
 * @param <O>
 */
public interface DataBindingProvider<O> {

	O raw();

	void with(O mapper);

	<T extends AccessToken> T parseToken(String payload, Class<T> tokenType) throws IOException;

	<T extends ProtocolError> T parseError(String payload, Class<T> errorType) throws IOException;

}