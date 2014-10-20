package org.bladerunnerjs.spec.command;

import static org.bladerunnerjs.appserver.BRJSApplicationServer.Messages.*;
import static org.bladerunnerjs.plugin.plugins.commands.standard.ServeCommand.Messages.*;

import org.bladerunnerjs.appserver.ApplicationServer;
import org.bladerunnerjs.plugin.plugins.commands.standard.ServeCommand;
import org.bladerunnerjs.testing.specutility.engine.SpecTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IntegrationServeCommandTest extends SpecTest
{
	private ApplicationServer appServer;
	
	@Before
	public void initTestObjects() throws Exception
	{
		given(brjs).hasCommandPlugins(new ServeCommand())
			.and(brjs).hasBeenCreated()
			.and(brjs).containsFolder("apps")
			.and(brjs).containsFolder("sdk/system-applications");
		appServer = brjs.applicationServer(appServerPort);
		brjs.bladerunnerConf().setJettyPort(appServerPort);
		brjs.appJars().create();
	}
	
	@After
	public void tearDown() throws Exception
	{
		logging.disableStoringLogs();
		logging.emptyLogStore();
		appServer = brjs.applicationServer(appServerPort);
		appServer.stop();
	}
	
	@Test
	public void serveCommandStartsAppServer() throws Exception
	{
		given(logging).enabled();
		when(brjs).runThreadedCommand("serve");
		then(logging).infoMessageReceived(SERVER_STARTING_LOG_MSG, "BladeRunnerJS")
			.and(logging).infoMessageReceived(SERVER_STARTED_LOG_MESSAGE, appServerPort)
			.and(logging).containsFormattedConsoleMessage(SERVER_STARTUP_MESSAGE + appServerPort +"/")
			.and(logging).containsFormattedConsoleMessage(SERVER_STOP_INSTRUCTION_MESSAGE + "\n")
			.and(appServer).requestIs302Redirected("/","/dashboard");
	}
	
	@Test
	public void commandIsAutomaticallyLoaded() throws Exception
	{
		given(brjs).hasBeenAuthenticallyCreated();
			/* and */ brjs.bladerunnerConf().setJettyPort(appServerPort);
		when(brjs).runThreadedCommand("serve");
		then(exceptions).verifyNoOutstandingExceptions();
	}
	
	@Test
	public void canOverridePortValueWithArgument() throws Exception
	{
		appServerPort = 7777;
		appServer = brjs.applicationServer(appServerPort);
		
		given(logging).enabled();
		when(brjs).runThreadedCommand("serve", "-p", "7777");
		then(logging).infoMessageReceived(SERVER_STARTING_LOG_MSG, "BladeRunnerJS")
			.and(logging).infoMessageReceived(SERVER_STARTED_LOG_MESSAGE, "7777")
			.and(logging).containsFormattedConsoleMessage(SERVER_STARTUP_MESSAGE + "7777/")
			.and(logging).containsFormattedConsoleMessage(SERVER_STOP_INSTRUCTION_MESSAGE + "\n")
			.and(appServer).requestIs302Redirected("/","/dashboard");
	}
	
}
