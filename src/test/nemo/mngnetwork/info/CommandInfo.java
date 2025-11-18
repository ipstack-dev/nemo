package test.nemo.mngnetwork.info;


public class CommandInfo {

	public String command; // the command line

	public String[] cmd; // alternatively, the program name and arguments
	
	public String[] getArgs() {
		return command!=null? command.split(" ") : cmd;
	}
}
