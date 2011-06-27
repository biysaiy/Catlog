package com.nolanlawson.logcat.data;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;
import android.util.Log;

import com.nolanlawson.logcat.util.LogLineAdapterUtil;
import com.nolanlawson.logcat.util.UtilLogger;


public class LogLine {

	public static final String LOGCAT_DATE_FORMAT = "MM-dd HH:mm:ss.SSS";
	
	private static Pattern logPattern = Pattern.compile("(\\w)/([^(]+)\\(\\s*(\\d+)\\): (.*)");
	
	private static UtilLogger log = new UtilLogger(LogLine.class);
	
	private int logLevel;
	private String tag;
	private String logOutput;
	private int processId = -1;
	private String timestamp;
	private boolean expanded = false;
	private boolean highlighted = false;
	
	public CharSequence getOriginalLine() {
		
		if (logLevel == -1) { // starter line like "begin of log etc. etc."
			return logOutput;
		}
		
		StringBuilder stringBuilder = new StringBuilder();
		
		if (timestamp != null) {
			stringBuilder.append(timestamp).append(' ');
		}
		
		stringBuilder.append(convertLogLevelToChar(logLevel))
			.append('/')
			.append(tag)
			.append('(')
			.append(processId)
			.append("): ")
			.append(logOutput);
		
		return stringBuilder;
	}

	public int getLogLevel() {
		return logLevel;
	}
	public void setLogLevel(int logLevel) {
		this.logLevel = logLevel;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getLogOutput() {
		return logOutput;
	}
	public void setLogOutput(String logOutput) {
		this.logOutput = logOutput;
	}
	
	
	public int getProcessId() {
		return processId;
	}
	public void setProcessId(int processId) {
		this.processId = processId;
	}
	
	
	
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public boolean isExpanded() {
		return expanded;
	}
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}
	
	public boolean isHighlighted() {
		return highlighted;
	}

	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
	}

	public static LogLine newLogLine(String originalLine, boolean expanded) {
		
		LogLine logLine = new LogLine();
		logLine.setExpanded(expanded);
		
		// first get the timestamp
		String timestamp = null;
		
		// if the first char is a digit, then this starts out with a timestamp
		// otherwise, it's a legacy log or the beginning of the log output or something
		if (!TextUtils.isEmpty(originalLine) && TextUtils.isDigitsOnly(Character.toString(originalLine.charAt(0)))) {
			timestamp = originalLine.substring(0,18);
			originalLine = originalLine.substring(19); // cut off timestamp
		}
		
		logLine.setTimestamp(timestamp);
		
		Matcher matcher = logPattern.matcher(originalLine);
		
		if (matcher.matches()) {
			char logLevelChar = matcher.group(1).charAt(0);
			
			logLine.setLogLevel(convertCharToLogLevel(logLevelChar));
			logLine.setTag(matcher.group(2));
			logLine.setProcessId(Integer.parseInt(matcher.group(3)));
			logLine.setLogOutput(matcher.group(4));
			
			
		} else {
			log.d("Line doesn't match pattern: " + originalLine);
			logLine.setLogOutput(originalLine);
			logLine.setLogLevel(-1);
		}
		
		return logLine;
		
	}
	public static int convertCharToLogLevel(char logLevelChar) {
		
		switch (logLevelChar) {
			case 'D':
				return Log.DEBUG;
			case 'E':
				return Log.ERROR;
			case 'I':
				return Log.INFO;
			case 'V':
				return Log.VERBOSE;
			case 'W':
				return Log.WARN;
			case 'F':
				return LogLineAdapterUtil.LOG_WTF; // 'F' actually stands for 'WTF', which is a real Android log level in 2.2
		}
		return -1;
	}
	
	public static char convertLogLevelToChar(int logLevel) {

		switch (logLevel) {
			case Log.DEBUG:
				return 'D';
			case Log.ERROR:
				return 'E';
			case Log.INFO:
				return 'I';
			case Log.VERBOSE:
				return 'V';
			case Log.WARN:
				return 'W';
			case LogLineAdapterUtil.LOG_WTF:
				return 'F';
		}
		return ' ';
	}
	
	private static Comparator<LogLine> sortByDate = new Comparator<LogLine>(){

		@Override
		public int compare(LogLine left, LogLine right) {
			if (TextUtils.isEmpty(left.getTimestamp()) && TextUtils.isEmpty(right.getTimestamp())) {
				return 0;
			} else if (TextUtils.isEmpty(left.getTimestamp())) {
				return -1;
			} else if (TextUtils.isEmpty(right.getTimestamp())) {
				return 1;
			} else {
				// string comparison works for dates when the format is always the same
				return left.getTimestamp().compareTo(right.getTimestamp());
			}
		}};

	public static Comparator<LogLine> sortByDate() {
		return sortByDate;
	}
}
