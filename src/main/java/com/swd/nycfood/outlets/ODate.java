package com.swd.nycfood.outlets;

import java.util.Date;

public class ODate {
	public ODate() {
		this.set$date(new Date().getTime());
	}

	private long $date;

	public long get$date() {
		return $date;
	}

	public void set$date(long $date) {
		this.$date = $date;
	}

	@Override
	public String toString() {
		return "ODate [$date=" + $date + "]";
	}
}
