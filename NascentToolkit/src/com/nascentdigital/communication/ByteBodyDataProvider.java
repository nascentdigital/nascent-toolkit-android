package com.nascentdigital.communication;

public class ByteBodyDataProvider implements BodyDataProvider {

	private byte[] bodyData;
	
	public ByteBodyDataProvider (byte [] data)
	{
		this.bodyData = data;
	}
	
	@Override
	public byte[] getBodyData() {
		return this.bodyData;
	}

}
