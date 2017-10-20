package com.demo.common.converter;

import org.springframework.core.convert.converter.Converter;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created with IntelliJ IDEA. User: Cougar Date: 15-1-13 Time: 下午5:31 To change this template use File | Settings | File Templates.
 */
public class StringToTimestampConverter implements Converter<String, Timestamp>
{

	private static final DateFormat DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static final DateFormat TIMEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Override
	public Timestamp convert(String source)
	{
		Timestamp ts = null;
		try
		{
			ts = Timestamp.valueOf(source);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return ts;
	}
}
