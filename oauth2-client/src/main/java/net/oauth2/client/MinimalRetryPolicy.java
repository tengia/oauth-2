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