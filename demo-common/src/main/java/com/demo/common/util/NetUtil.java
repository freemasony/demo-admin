package com.demo.common.util;


import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;

/**
 * 与网络相关的工具集
 */
public class NetUtil
{

	/**
	 * 获取页面输入的String类型参数
	 *
	 * @param request  ServletRequest的实例
	 * @param name     参数名字
	 * @return String型的输入参数
	 */
	public static String getStringParameter(ServletRequest request, String name)
	{
		return getStringParameter(request, name, null);
	}


	/**
	 * 获取页面输入的String类型参数
	 *
	 * @param request  ServletRequest的实例
	 * @param name     参数名字
	 * @param defaults 设定的默认值
	 * @return String型的输入参数
	 */
	public static String getStringParameter(ServletRequest request, String name, String defaults)
	{
		return getStringParameter(request, name, defaults, true);
	}

	/**
	 * 获取页面输入的String类型参数
	 *
	 * @param request  ServletRequest的实例
	 * @param name     参数名字
	 * @param defaults 设定的默认值
	 * @param decode   是否需要解码&#xxx;这种编码
	 * @return String型的输入参数
	 */
	public static String getStringParameter(ServletRequest request, String name, String defaults, boolean decode)
	{
		String str = request.getParameter(name);
		if (decode)
		{
			//解码
			str = StringUtil.decodeNetUnicode(str);
		}
		return StringUtil.convertString(str, defaults);
	}

	/**
	 * 获取页面输入的int类型参数
	 *
	 * @param request  ServletRequest的实例
	 * @param name     参数名字
	 * @param defaults 设定的默认值
	 * @return int型的输入参数
	 */
	public static int getIntParameter(ServletRequest request, String name, int defaults)
	{
		return StringUtil.convertInt(request.getParameter(name), defaults);
	}

	/**
	 * 获取页面输入的int类型参数，若无该输入参数，则返回0
	 *
	 * @param request ServletRequest的实例
	 * @param name    参数名字
	 * @return int型的输入参数
	 */
	public static int getIntParameter(ServletRequest request, String name)
	{
		return getIntParameter(request, name, 0);
	}

	/**
	 * 获取页面输入的long类型参数
	 *
	 * @param request  ServletRequest的实例
	 * @param name     参数名字
	 * @param defaults 设定的默认值
	 * @return long型的输入参数
	 */
	public static long getLongParameter(ServletRequest request, String name, long defaults)
	{
		return StringUtil.convertLong(request.getParameter(name), defaults);
	}

	/**
	 * 获取页面输入的long类型参数，若无该输入参数，则返回0
	 *
	 * @param request ServletRequest的实例
	 * @param name    参数名字
	 * @return long型的输入参数
	 */
	public static long getLongParameter(ServletRequest request, String name)
	{
		return getLongParameter(request, name, 0);
	}

	/**
	 * 获取页面输入的double类型参数
	 *
	 * @param request  ServletRequest的实例
	 * @param name     参数名字
	 * @param defaults 设定的默认值
	 * @return double型的输入参数
	 */
	public static double getDoubleParameter(ServletRequest request, String name, double defaults)
	{
		return StringUtil.convertDouble(request.getParameter(name), defaults);
	}

	/**
	 * 获取页面输入的double类型参数，若无该参数，则返回0.0
	 *
	 * @param request ServletRequest的实例
	 * @param name    参数名字
	 * @return long型的输入参数
	 */
	public static double getDoubleParameter(ServletRequest request, String name)
	{
		return getDoubleParameter(request, name, 0.0);
	}

	/**
	 * 获取页面输入的short类型参数
	 *
	 * @param request  ServletRequest的实例
	 * @param name     参数名字
	 * @param defaults 设定的默认值
	 * @return short型的输入参数
	 */
	public static short getShortParameter(ServletRequest request, String name, short defaults)
	{
		return StringUtil.convertShort(request.getParameter(name), defaults);
	}

	/**
	 * 获取页面输入的short类型参数，若无该参数，则返回0
	 *
	 * @param request ServletRequest的实例
	 * @param name    参数名字
	 * @return short型的输入参数
	 */
	public static short getShortParameter(ServletRequest request, String name)
	{
		return getShortParameter(request, name, (short) 0);
	}

	/**
	 * 获取页面输入的float类型参数
	 *
	 * @param request  ServletRequest的实例
	 * @param name     参数名字
	 * @param defaults 设定的默认值
	 * @return float型的输入参数
	 */
	public static float getFloatParameter(ServletRequest request, String name, float defaults)
	{
		return StringUtil.convertFloat(request.getParameter(name), defaults);
	}

	/**
	 * 获取页面输入的float类型参数，若无该参数，则返回0.0
	 *
	 * @param request ServletRequest的实例
	 * @param name    参数名字
	 * @return long型的输入参数
	 */
	public static float getFloatParameter(ServletRequest request, String name)
	{
		return getFloatParameter(request, name, (float) 0.0);
	}

	/**
	 * 获取boolean 类型的参数
	 *
	 * @return boolean
	 */
	public static boolean getBooleanParameter(ServletRequest request, String name, boolean defaults)
	{
		return StringUtil.convertBoolean(request.getParameter(name), defaults);
	}


	/**
	 * 获取boolean 类型的参数,默认值为false
	 *
	 * @return boolean
	 */
	public static boolean getBooleanParameter(ServletRequest request, String name)
	{
		return getBooleanParameter(request, name, false);
	}

	/**
	 * 获取Timestamp 类型的参数
	 *
	 * @return boolean
	 */
	public static Timestamp getTimestampParameter(ServletRequest request, String name, Timestamp defaults)
	{
		return StringUtil.convertTimestamp(request.getParameter(name), defaults);
	}

	/**
	 * 获取Timestamp 类型的参数
	 *
	 * @return boolean
	 */
	public static Timestamp getTimestampParameter(ServletRequest request, String name)
	{
		return StringUtil.convertTimestamp(request.getParameter(name), null);
	}

	/**
	 * 获取客户端IP
	 * @param request
	 * @return
	 */
	public String getRemoteIp(HttpServletRequest request)
	{
		String ipAddress = request.getHeader("X-Forwarded-For-Pound");
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress))
		{
			ipAddress = request.getRemoteAddr();
		}
		ipAddress = request.getHeader("x-forwarded-for");
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress))
		{
			ipAddress = request.getHeader("Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress))
		{
			ipAddress = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress))
		{
			ipAddress = request.getRemoteAddr();
			if (ipAddress.equals("127.0.0.1"))
			{
				//根据网卡取本机配置的IP
				InetAddress inet = null;
				try
				{
					inet = InetAddress.getLocalHost();
				} catch (UnknownHostException e)
				{
					e.printStackTrace();
				}
				ipAddress = inet.getHostAddress();
			}

		}
		//对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
		if (ipAddress != null && ipAddress.length() > 15)
		{ //"***.***.***.***".length() = 15
			if (ipAddress.indexOf(",") > 0)
			{
				ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
			}
		}
		return ipAddress;
	}

}
