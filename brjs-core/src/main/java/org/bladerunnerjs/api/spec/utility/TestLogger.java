package org.bladerunnerjs.api.spec.utility;

import org.bladerunnerjs.api.logging.Logger;

public class TestLogger implements Logger
{

	private String name;
	private LogMessageStore logStore;

	public TestLogger(String name, LogMessageStore logStore)
	{
		this.name = name;
		this.logStore = logStore;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void error(String message, Object... params)
	{
		logStore.addError(name, message, params);
	}
	
	@Override
	public void warn(String message, Object... params)
	{
		logStore.addWarn(name, message, params);
	}

	@Override
	public void info(String message, Object... params)
	{
		logStore.addInfo(name, message, params);
	}

	@Override
	public void debug(String message, Object... params)
	{
		logStore.addDebug(name, message, params);
	}

	@Override
	public void println(String message, Object... params)
	{
		console(message, params);
	}

	@Override
	public void console(String message, Object... params)
	{
		logStore.addConsole(name, message, params);
	}
}
