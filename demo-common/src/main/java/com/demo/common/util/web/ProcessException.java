
package com.demo.common.util.web;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author sun
 * @version 1.0, 2013-7-5 下午4:51:30
 * @since 1.0
 */
public class ProcessException extends RuntimeException {
	private static final long serialVersionUID = -6719316009910747311L;

	/**
	 * @param message
	 * @param cause
	 */
	public ProcessException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public ProcessException(String message, Object... format) {
		super(getMsg(message, format));
	}

	private static String getMsg(String format, Object ... obj) {
		if (StringUtils.isBlank(format)) {
			return format;
		}

		if (obj == null) {
			return format;
		}

		if (obj.length == 1) {
			return StringUtils.replace(format, "{}", obj[0] == null ? "null" : obj[0].toString());
		}

		int i = 0;
		while(true) {
			int indexof = StringUtils.indexOf(format, "{}");
			if (indexof == -1)
				break;
			String left = StringUtils.substring(format, 0, indexof + 1);
			String right = StringUtils.substring(format, indexof + 1);
			format = left + i++ + right;
		}

		String[] search = new String[obj.length];
		String[] replace = new String[obj.length];

		for (int j = 0; j < obj.length; j ++) {
			search[j] = "{" + j + "}";
			replace[j] = obj[j] == null ? "null" : obj[j].toString();
		}

		return StringUtils.replaceEach(format, search, replace);
	}

	/**
	 * @param cause
	 */
	public ProcessException(Throwable cause) {
		super(cause);
	}
	
}
