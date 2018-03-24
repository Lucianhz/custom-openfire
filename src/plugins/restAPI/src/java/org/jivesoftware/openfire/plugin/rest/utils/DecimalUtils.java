package org.jivesoftware.openfire.plugin.rest.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class DecimalUtils {
	public static BigDecimal format(String num){
		DecimalFormat df = new DecimalFormat("#.00");
		if(num==null||"".equals(num)){
			return new BigDecimal("0.00");
		}
		Double n = Double.valueOf(num);
		String numNew = df.format(n);
		return new BigDecimal(numNew);
	}
}
