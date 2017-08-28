package com.speculation1000.specvault.market;

import java.math.BigDecimal;

import com.speculation1000.specvault.listview.Displayable;

public class Market implements Displayable {
		
	private String base;
	
	private String counter;
	
	private String exchange;
	
	private long date;
	
	private BigDecimal open;
	
	private BigDecimal close;
	
	private BigDecimal high;
	
	private BigDecimal low;
	
	private int volume;
	
	private BigDecimal trueRange;
	
	public String getBase(){
		return base;
	}
	
	public void setBase(String base){
		this.base = base;
	}
	
	public String getCounter(){
		return counter;
	}
	
	public void setCounter(String counter){
		this.counter = counter;
	}

	public String getExchange() {
		return exchange;
	}
	
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public long getDate() {
		return date;
	}
	
	public void setDate(long date){
		this.date = date;
	}

	public BigDecimal getHigh() {
		return high;
	}
	
	public void setHigh(BigDecimal high){
		this.high = high;
	}

	public BigDecimal getLow() {
		return low;
	}
	
	public void setLow(BigDecimal low){
		this.low = low;
	}

	public BigDecimal getOpen() {
		return open;
	}
	
	public void setOpen(BigDecimal open){
		this.open = open;
	}

	public BigDecimal getClose() {
		return close;
	}
	
	public void setClose(BigDecimal close){
		this.close = close;
	}

	public int getVolume() {
		return volume;
	}
	
	public void setVolume(int volume){
		this.volume = volume;
	}
	
	public BigDecimal getTrueRange(){
		return trueRange;
	}
	
	public void setTrueRange(BigDecimal trueRange){
		this.trueRange = trueRange;
	}
	
	@Override
	public String toString(){
		return base + counter + ":" + exchange +" "+ "@" + close;
	}
}