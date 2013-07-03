package com.nascentdigital.communication;

public enum ServiceOperationPriority {
	 VERY_LOW(-8), LOW(-4), NORMAL(0), HIGH(4), VERY_HIGH(8);
	 
	 private int priority;
	 
	 private ServiceOperationPriority(int priority) {
		 this.priority = priority;
	 }
	 
	 public int getIntValue() {
	   return priority;
	 }
}
