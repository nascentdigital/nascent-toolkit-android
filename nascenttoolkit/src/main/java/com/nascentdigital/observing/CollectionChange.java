package com.nascentdigital.observing;


public class CollectionChange {

	//[region] cctor
	public CollectionChange(CollectionChangeType collectionChangeType, int offset, int length)
	{
		this.collectionChangeType = collectionChangeType;
		this.offset = offset;
		this.length = length;
	}
	//[endregion]

    //[region] instance fields
	public final CollectionChangeType collectionChangeType;
	public final int offset;
	public final int length;
    //[endregion]
}
