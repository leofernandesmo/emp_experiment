package emp_experiment.scripts;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
	
	private static Logger logger = Logger.getLogger(Utils.LOGGER_NAME);
	
	public static void main(String[] args) {

		if (args.length > 0) {
			Main m = new Main();
			InputStream inputStream = null;

			try {
				// get config file
				String file = args[0];
				inputStream = m.getClass().getClassLoader().getResourceAsStream(file);
				Properties prop = new Properties();
				prop.load(inputStream);

				// get property values
				String jdollyScope = prop.getProperty("jdolly_scope");
				String output = prop.getProperty("output");
				String saferefactorDir = prop.getProperty("saferefactor_dir");

				if (jdollyScope == null || output == null || saferefactorDir == null)
					throw new Exception("Config file is invalid.");
				
				

				// Count elapsed time
				long tStart = System.currentTimeMillis();

				// Exec script
//				String sessionName = Long.toString(System.currentTimeMillis());
				String sessionName = "exemplo02";
				String sessionPath = m.setupSession(sessionName, output);
				
//				String jdollyOutput = m.execJDolly(jdollyScope, sessionPath);
				String jdollyOutput = sessionPath + "/jdolly";
				
//				m.execMuJava(sessionPath, jdollyOutput, sessionName);
				
				List<String> mutantOperators = m.organizeFilesByProgram(sessionPath);
				m.execSafeRefactor(sessionPath, saferefactorDir, mutantOperators);
				
//				m.deleteTemporary(jdollyOutput, sessionPath);

				// print Elapsed time
				long tEnd = System.currentTimeMillis();
				long tDelta = tEnd - tStart;
				double elapsedSeconds = tDelta / 1000.0;
				
				logger.info("Finished in "+elapsedSeconds+" seconds ");
				logger.info("Resulted in: "+sessionPath+" ");

			} catch (Exception exc) {
				exc.printStackTrace();
				logger.severe(exc.getMessage());
			}

		} else {
			System.err.println("Config file not found. You must pass a config file as parameter.");
			System.err.println("Eg.: java emp_experiment.scripts.Main /path/to/config/configfile.config");
			System.err.println("Check a config file example in the root directory of the project.jar");
		}

	}


	private void deleteTemporary(String jdollyOutput, String mujavaSession) throws IOException {
		List<File> javafiles = Utils.listFilesAndFilesSubDirectories(jdollyOutput, ".java");
		for (File file : javafiles) {
			Files.delete(file.toPath());
		}
		Files.delete(Paths.get(jdollyOutput));
	}

	private void execSafeRefactor(String mujavaSession, String saferefactorDir, List<String> mutantOperators)
			throws IOException, InterruptedException {
		SafeRefactorDriver srd = new SafeRefactorDriver();
		srd.execute(mujavaSession, saferefactorDir, mutantOperators);
	}
	


	private List<String> organizeFilesByOperator(String mujavaSession) throws IOException {
		Utils u = new Utils();
		return u.organizeFilesByOperator(mujavaSession);
	}

	
	
	
	private List<String> organizeFilesByProgram(String mujavaSession) throws IOException {
		Utils u = new Utils();
		return u.organizeFilesByProgram(mujavaSession);

	}

	// Mujava mutant generator
	private void execMuJava(String output, String jdollyOutput, String sessionName) throws Exception {
		MuJavaDriver mjd = new MuJavaDriver();
		mjd.execute(output, jdollyOutput, sessionName);
	}

	// JDolly program generator
	private String execJDolly(String scope, String sessionPath) {
		JDollyDriver jdd = new JDollyDriver();
		return jdd.execute(scope, sessionPath);

	}
	
	private String setupSession(String sessionName, String output) throws SecurityException, IOException{
		String sessionPath = output + sessionName;
		new File(sessionPath).mkdirs();
		Utils.logSetup(sessionPath); //setup Logger
		 
		return sessionPath;
	}

}
