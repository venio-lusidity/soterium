package com.lusidity.test.misc;

import com.lusidity.framework.math.IntegerX;
import org.junit.Test;

public class IntergerXTest
{	
	@Test
	public void simple(){
		System.out.println(IntegerX.insertCommas(10L, true));
		System.out.println(IntegerX.insertCommas(1000L, true));
		System.out.println(IntegerX.insertCommas(10000L, true));
		System.out.println(IntegerX.insertCommas(100000L, true));
		System.out.println(IntegerX.insertCommas(1000000L, true));
		System.out.println(IntegerX.insertCommas(10000000L, true));

		System.out.println(IntegerX.insertCommas(10L, false));
		System.out.println(IntegerX.insertCommas(1000L, false));
		System.out.println(IntegerX.insertCommas(10000L, false));
		System.out.println(IntegerX.insertCommas(100000L, false));
		System.out.println(IntegerX.insertCommas(1000000L, false));
		System.out.println(IntegerX.insertCommas(10000000L, false));
	}
}
