package com.swd.nycfood.outlets;

import java.util.ArrayList;

import lombok.Data;
import lombok.NonNull;

@Data
public class Outlet {
	private ArrayList<Grade> grades = new ArrayList<>();
	@NonNull private String name;
	@NonNull private Address address;
	@NonNull private String borough;
	@NonNull private String cuisine;
	@NonNull private String restaurant_id;
}