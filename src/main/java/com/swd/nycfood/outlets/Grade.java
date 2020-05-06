package com.swd.nycfood.outlets;

import lombok.Data;
import lombok.NonNull;

@Data
public class Grade {
	@NonNull private Integer score;
	@NonNull private String grade;
	private ODate date = new ODate();
}