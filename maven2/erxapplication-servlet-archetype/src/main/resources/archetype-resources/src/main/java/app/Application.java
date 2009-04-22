// Generated by the Maven Archetype Plug-in
package ${package}.app;

import com.webobjects.appserver.WOSession;
import com.webobjects.foundation.NSLog;
#if($WonderSupport == "no")
import com.webobjects.appserver.WOApplication;
import com.webobjects.foundation._NSUtilities;
#set( $applicationClass = "WOApplication" )
#else
import er.extensions.appserver.ERXApplication;
import er.extensions.foundation.ERXPatcher;
#set( $applicationClass = "ERXApplication" )
#end
import ${package}.components.Main;

public class Application extends $applicationClass {
	public static void main(String[] argv) {
		${applicationClass}.main(argv, Application.class);
	}

	public Application() {
		super();
		NSLog.out.appendln("Welcome to " + name() + " !");
		/* ** put your initialization code in here ** */
#if( ($WonderSupport == "no") )
		// ensure that Main is correctly resolved at runtime
		_NSUtilities.setClassForName(Main.class, "Main");
#end
	}

	/**
	 * Determines the WOSession class to instantiate.
	 * @see com.webobjects.appserver.WOApplication#_sessionClass()
	 */
	@Override
	protected Class< ? extends WOSession > _sessionClass() {
		return Session.class;
	}

#if( !($WonderSupport == "no") )
	/**
	 * Install patches including ensuring that Main is correctly resolved at runtime.
	 * @see er.extensions.appserver.ERXApplication#installPatches()
	 */
	@Override
	public void installPatches() {
		super.installPatches();
		ERXPatcher.setClassForName(Main.class, "Main");
	}
#end
}