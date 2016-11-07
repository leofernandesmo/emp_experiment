package emp_experiment.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

public class SafeRefactorDriver {

	private static final String SAFEREFACTOR_MAIN = " saferefactor.ui.Main";
	public static final String SAFEREFACTOR_MUTANTS_DIR = "/saferefactor/mutants/";
	public static final String SAFEREFACTOR_ORIGINAL_DIR = "/saferefactor/original/";

	private static Logger logger = Logger.getLogger(Utils.LOGGER_NAME);

	private Map<String, List<String>> mutants;

	public SafeRefactorDriver() {
		super();
		mutants = new HashMap<String, List<String>>();

	}

	public void execute(String mujavaSession, String saferefactorDir, List<String> mutantOperators)
			throws IOException, InterruptedException {

		deleteSRTempFolders();
		executeByFile(mujavaSession, saferefactorDir, mutantOperators);
		// executeByOperator(mujavaSession, saferefactorDir, mutantOperators);
	}

	// Necessário deletar a pasta de diretorios temporarios do SafeRefactor
	// Deleta exemplo: /tmp/SafeRefactor99/
	private void deleteSRTempFolders() throws IOException {
		List<File> sfDirs = Utils.listDirectories("/tmp");
		for (File folder : sfDirs) {
			if (folder.exists() && folder.getName().contains("SafeRefactor")) {
				FileUtils.deleteDirectory(folder);
			}
		}

	}

	/**
	 * Organização dos arquivos por operador de mutação. Ex. Mutant:
	 * ./saferefactor/mutants/SDL_1/ClassId_1.java Original:
	 * ./saferefactor/original/ClassId_1.java
	 * 
	 * @param mujavaSession
	 * @param saferefactorDir
	 * @param mutantOperators
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void executeByOperator(String mujavaSession, String saferefactorDir, List<String> mutantOperators)
			throws IOException, InterruptedException {
		String safeRefactorCommand = makeComand(saferefactorDir);

		for (String operator : mutantOperators) {
			System.out.println("Executing SafeRefactor for Operator: " + operator);
			safeRefactorCommand += " " + mujavaSession + SAFEREFACTOR_ORIGINAL_DIR;
			safeRefactorCommand += " " + mujavaSession + SAFEREFACTOR_MUTANTS_DIR + operator + "/";

			runCommand(safeRefactorCommand, operator, "");
		}

	}

	/**
	 * Organização dos arquivos por Arquivo Gerado. Ex. Mutant:
	 * ./saferefactor/ClassId_1/mutants/SDL_1/ClassId_1.java Original:
	 * ./saferefactor/ClassId_1/original/ClassId_1.java
	 * 
	 * @param mujavaSession
	 * @param saferefactorDir
	 * @param mutantOperators
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void executeByFile(String mujavaSession, String saferefactorDir, List<String> mutantOperators)
			throws IOException, InterruptedException {
		List<File> dirs = Utils.listDirectories(mujavaSession + "/saferefactor/");

		logger.info("Executing SafeRefactor!!!");
		for (File classId : dirs) {

			// if(classId.getName().contains("8221")){

			String pathOriginal = classId.getAbsolutePath() + "/original/";
			String pathMutants = classId.getAbsolutePath() + "/mutants/";

			List<File> dirMutans = Utils.listDirectories(pathMutants);

			for (File mutantOp : dirMutans) {

				String safeRefactorCommand = makeComand(saferefactorDir);
				safeRefactorCommand += " " + pathOriginal;
				safeRefactorCommand += " " + mutantOp.getAbsolutePath() + "/";

				runCommand(safeRefactorCommand, mutantOp.getName(), classId.getName());

			}

			// }

		}
	}

	private String makeComand(String saferefactorDir) {
		String safeRefactorLibDir = saferefactorDir + "lib/*";
		String safeRefactorBinDir = saferefactorDir + "bin/";
		String safeRefactorCommand = "java -cp " + safeRefactorLibDir + ":" + safeRefactorBinDir + SAFEREFACTOR_MAIN;
		return safeRefactorCommand;
	}

	private void runCommand(String safeRefactorCommand, String mutantOp, String fileName)
			throws IOException, InterruptedException {
		Process pro = Runtime.getRuntime().exec(safeRefactorCommand);
		InputStream err = pro.getErrorStream(); // Logar os erros
		InputStream in = pro.getInputStream();
		pro.waitFor();
		// ERROR InputStream
		processInputStream(err, true, mutantOp, fileName);
		// InputStream
		processInputStream(in, false, mutantOp, fileName);

	}

	private void processInputStream(InputStream in, boolean isError, String mutantOp, String fileName)
			throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder out = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			out.append(line + "\n");
			if (line.contains("behavioral")) {
				// logger.info(mutantOp + ":" + line);
				System.out.println(mutantOp + ":" + line + " in " + fileName);
				// Checagem do Log gerado pelo SafeRefator.
				// Necessario para validar a análise.
				int numTests = checkSRTempFolder();
				if (numTests > 10) {
					addMutants(mutantOp, line + " " + fileName);
				} else {
					System.out.println("Descartado: " + mutantOp + " para o arquivo " + fileName);
					Utils.logSRFailedAnalysis(
							"failed:" + fileName + ":operator:" + mutantOp + ":num_tests:" + numTests);
				}

			}
			// System.out.println(line);
		}
		if (isError) {
			// System.err.println(out.toString());
		} else {
			// System.out.println(out.toString());
		}
		reader.close();
	}

	public Map<String, List<String>> getMutants() {
		return mutants;
	}

	public void addMutants(String mutantOp, String result) throws IOException {
		if (this.mutants.containsKey(mutantOp)) {
			List<String> list = this.mutants.get(mutantOp);
			list.add(result);
			// if (list.size() % 2 == 0) {
			printListResult();
			// }
		} else {
			List<String> list = new ArrayList<String>();
			list.add(result);
			mutants.put(mutantOp, list);
		}
	}

	private void printListResult() throws IOException {
		for (String mutantOp : getMutants().keySet()) {
			List<String> lines = new ArrayList<String>();
			for (String line : getMutants().get(mutantOp)) {
				lines.add(line);
			}
			Utils.logMutantEquivalentResult(mutantOp, lines);
		}
		// Clear the mutant Set
		mutants = new HashMap<String, List<String>>();
	}

	private int checkSRTempFolder() throws IOException {
		int result = 0;
		List<File> sfDirs = Utils.listDirectories("/tmp");
		for (File folder : sfDirs) {
			if (folder.isDirectory() && folder.getName().contains("SafeRefactor")) {
				List<File> files = Utils.listFilesAndFilesSubDirectories(folder.getAbsolutePath(), ".txt");
				for (File logFile : files) {
					if (logFile.getName().contains("log_saferefactor_testrunner")) {
						BufferedReader br = new BufferedReader(new FileReader(logFile));
						String sCurrentLine = "";
						while ((sCurrentLine = br.readLine()) != null) {
							sCurrentLine = sCurrentLine.toLowerCase();
							if (sCurrentLine.contains("tests run:")) {
								int start = sCurrentLine.indexOf(":") + 1;
								int end = sCurrentLine.indexOf(",");
								result += Integer.parseInt(sCurrentLine.substring(start, end).trim());
							}
						}
						br.close();
					}
				}
			}
		}
		deleteSRTempFolders();
		return result;
	}

}
