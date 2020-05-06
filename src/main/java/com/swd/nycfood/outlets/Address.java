package com.swd.nycfood.outlets;

import lombok.Data;
import lombok.NonNull;

@Data
public class Address {
	@NonNull private String street;
	@NonNull private String zipcode;
	@NonNull private String building;
	@NonNull private Float[] coord;
}