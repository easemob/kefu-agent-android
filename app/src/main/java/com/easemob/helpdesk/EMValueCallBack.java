package com.easemob.helpdesk;

public abstract class  EMValueCallBack<T> {
	public abstract void onSuccess(T value);

	public abstract void onError(final int error, final String errorMsg);

	public void onProgress(int progress){
		
	}
}
