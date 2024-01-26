package com.l2j4team.commons.logging.formatter;

import java.util.logging.LogRecord;

import com.l2j4team.commons.lang.StringUtil;
import com.l2j4team.commons.logging.MasterFormatter;

public class ConsoleLogFormatter extends MasterFormatter
{
	@Override
	public String format(LogRecord record)
	{
		final StringBuilder sb = new StringBuilder(500);

		StringUtil.append(sb, record.getMessage(), CRLF);

		final Throwable throwable = record.getThrown();
		if (throwable != null)
		{
			for (StackTraceElement traceElement : throwable.getStackTrace())
				StringUtil.append(sb, SHIFT, traceElement, CRLF);
		}
		return sb.toString();
	}
}