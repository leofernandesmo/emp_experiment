package emp_experiment.scripts;

public class JDollyDriver {

	public String execute(String scope, String sessionPath){
		String[] scopes = scope.split(" ");
		String skip = "25";
		String output = sessionPath + "/jdolly";
		String[] argsJDolly = { "-scope", scopes[0], scopes[1], scopes[2], scopes[3], "-output", sessionPath, "-skip",
				skip };
		jdolly.main.Main.main(argsJDolly);
		return output;
	}
}
