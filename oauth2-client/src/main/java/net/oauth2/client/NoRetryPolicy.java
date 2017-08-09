package net.oauth2.client;

public class NoRetryPolicy implements RetryPolicy{
	public long periodBetweenRetries(){
		return 0L;
	}
	public long maxRetries(){
		return 1;
	}
	public boolean onException(Throwable t){
		return false;
	}
}