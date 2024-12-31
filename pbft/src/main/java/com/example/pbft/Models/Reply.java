package com.example.pbft.Models;

import java.util.Objects;

public class Reply {
	private String type;
	private int V;
    private long timestamp;
    private int i;
    private int n;
    
	public int getN() {
		return n;
	}
	public void setN(int n) {
		this.n = n;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getV() {
		return V;
	}
	public void setV(int v) {
		V = v;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public int getI() {
		return i;
	}
	public void setI(int i) {
		this.i = i;
	}
	
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reply reply = (Reply) o;
        return V == reply.V &&
               timestamp == reply.timestamp &&
               i == reply.i &&
               n == reply.n &&
               Objects.equals(type, reply.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, V, timestamp, i, n);
    }

   
}
