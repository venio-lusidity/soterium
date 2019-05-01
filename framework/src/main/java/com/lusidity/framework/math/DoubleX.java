/*
 * Copyright 2018 lusidity inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.lusidity.framework.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class DoubleX extends BigDecimal
{
	// Constructors
	public DoubleX(char[] in, int offset, int len)
	{
		super(in, offset, len);
	}

	public DoubleX(char[] in, int offset, int len, MathContext mc)
	{
		super(in, offset, len, mc);
	}

	public DoubleX(char[] in)
	{
		super(in);
	}

	public DoubleX(char[] in, MathContext mc)
	{
		super(in, mc);
	}

	public DoubleX(String val)
	{
		super(val);
	}

	public DoubleX(String val, MathContext mc)
	{
		super(val, mc);
	}

	public DoubleX(double val)
	{
		super(val);
	}

	public DoubleX(double val, MathContext mc)
	{
		super(val, mc);
	}

	public DoubleX(BigInteger val)
	{
		super(val);
	}

	public DoubleX(BigInteger val, MathContext mc)
	{
		super(val, mc);
	}

	public DoubleX(BigInteger unscaledVal, int scale)
	{
		super(unscaledVal, scale);
	}

	public DoubleX(BigInteger unscaledVal, int scale, MathContext mc)
	{
		super(unscaledVal, scale, mc);
	}

	public DoubleX(int val)
	{
		super(val);
	}

	public DoubleX(int val, MathContext mc)
	{
		super(val, mc);
	}

	public DoubleX(long val)
	{
		super(val);
	}

	public DoubleX(long val, MathContext mc)
	{
		super(val, mc);
	}

	public DoubleX from(Double value){
		return new DoubleX(value);
	}
}
