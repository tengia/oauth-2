/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
package net.oauth2.client;

public class MinimalRetryPolicy implements RetryPolicy{
	public long periodBetweenRetries(){
		return 60*1000;
	}
	public long maxRetries(){
		return 3;
	}
	public boolean onException(Throwable t){
		return true;
	}
}