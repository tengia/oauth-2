/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */

package net.oauth2;

import java.util.Map;

/**
 * Models an oauth payload object model as a key-value map. 
 *
 */
public interface ParametersMap {

	Map<String, Object> map() throws Exception;
 
}
