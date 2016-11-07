package emp_experiment.scripts;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Utils {
	public static final String LOGGER_NAME = "emp_experiment";
	public static String session = "";
	private static Logger logger = Logger.getLogger(Utils.LOGGER_NAME);

	public static void logSetup(String sessionPath) throws SecurityException, IOException {
		// construct a general file handler
		session = sessionPath;
		String generalLogFile = sessionPath + "/" + LOGGER_NAME + ".log";
		Handler fh1 = new FileHandler(generalLogFile);
		fh1.setFormatter(new SimpleFormatter());
		fh1.setLevel(Level.SEVERE);

		Logger logger = Logger.getLogger(LOGGER_NAME);
		logger.addHandler(fh1);
	}

	public static void logMutantEquivalentResult(String mutantOp, List<String> lines) throws IOException {
		File f = new File(session + "/" + LOGGER_NAME + "_" + mutantOp + ".log");
		Files.write(f.toPath(), lines, UTF_8, APPEND, CREATE);
	}
	
	public static void logSRFailedAnalysis(String line) throws IOException {
		List<String> lines = new ArrayList<String>();
		lines.add(line);
		File f = new File(session + "/" + LOGGER_NAME + "_sr_invalid_tests.log");
		Files.write(f.toPath(), lines, UTF_8, APPEND, CREATE);
	}

	/**
	 * List all files from a directory and its subdirectories (recursive)
	 * 
	 * @param directoryName
	 *            to be listed
	 */
	public static List<File> listFilesAndFilesSubDirectories(String directoryName, String... extensions) {
		List<File> result = new ArrayList<File>();
		File directory = new File(directoryName);
		// get all the files from a directory
		File[] fList = directory.listFiles();
		for (File file : fList) {
			for (String ext : extensions) {
				if (file.isFile() && file.getName().endsWith(ext)) {
					result.add(file);
				} else if (file.isDirectory()) {
					List<File> temp = listFilesAndFilesSubDirectories(file.getAbsolutePath(), extensions);
					if (temp != null && temp.size() > 0) {
						result.addAll(temp);
					}
				}
			}
		}
		return result;
	}

	/**
	 * List all files from a directory and its subdirectories (recursive)
	 * 
	 * @param directoryName
	 *            to be listed
	 */
	public static List<File> listFilesAndFilesSubDirectories(String directoryName) {
		List<File> result = new ArrayList<File>();
		File directory = new File(directoryName);
		// get all the files from a directory
		File[] fList = directory.listFiles();
		for (File file : fList) {

			if (file.isFile()) {
				result.add(file);
			} else if (file.isDirectory()) {
				List<File> temp = listFilesAndFilesSubDirectories(file.getAbsolutePath());
				if (temp != null && temp.size() > 0) {
					result.addAll(temp);
				}
			}

		}
		return result;
	}

	/**
	 * List all directories from a directory (not recursive)
	 * 
	 * @param directoryName
	 *            to be listed
	 */
	public static List<File> listDirectories(String directoryName) {
		List<File> result = new ArrayList<File>();
		File directory = new File(directoryName);
		// get all the files from a directory
		File[] fList = directory.listFiles();
		if (fList == null)
			fList = new File[0];
		for (File file : fList) {
			if (file.isDirectory()) {
				result.add(file);
			}
		}
		return result;
	}

	public static void replaceClassName(File javaFile, String number) throws IOException {
		// input the file content to the String "input"
		BufferedReader file = new BufferedReader(new FileReader(javaFile));
		String line;
		String input = "";

		while ((line = file.readLine()) != null) {
			input += line + '\n';
		}
		file.close();
		// TODO It's a test. Try a more generic replacement
		input = input.replace("ClassId_0", "ClassId_" + number);
		// TODO Teste removendo Package para ver se compila
		input = input.replace("package Package_0;", "");
		// write the new String with the replaced line OVER the same file
		FileOutputStream fileOut = new FileOutputStream(javaFile);
		fileOut.write(input.getBytes());
		fileOut.close();
	}

	public List<String> organizeFilesByProgram(String mujavaSession) throws IOException {
		String mujavaResultDir = mujavaSession + "/result/";
		String saferefactorDir = mujavaSession + "/saferefactor/";
		Set<String> mutantOperators = new HashSet<String>();

		new File(saferefactorDir).mkdirs();

		List<File> javaFiles = Utils.listFilesAndFilesSubDirectories(mujavaResultDir, ".java");
		for (File file : javaFiles) {
			String newDirName = saferefactorDir + file.getName().replace(".java", "");
			if (file.getAbsolutePath().contains("original")) {
				newDirName += "/original/";
				File newPath = new File(newDirName + file.getName());
				newPath.mkdirs();
				Files.copy(file.toPath(), newPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} else if (file.getAbsolutePath().contains("traditional_mutants")) {
				newDirName += "/mutants/";
				String aux = file.getAbsolutePath();
				int index = aux.lastIndexOf("traditional_mutants");
				aux = aux.substring(index);
				index = aux.indexOf("/") + 1;
				aux = aux.substring(index);
				index = aux.indexOf("/") + 1;
				aux = aux.substring(index);

				File newPath = new File(newDirName + aux);
				newPath.mkdirs();
				Files.copy(file.toPath(), newPath.toPath(), StandardCopyOption.REPLACE_EXISTING);

				index = aux.indexOf("/");
				String mutantOp = aux.substring(0, index);
				mutantOperators.add(mutantOp);

			} else if (file.getAbsolutePath().contains("class_mutants")) {
				newDirName += "/mutants/";
				String aux = file.getAbsolutePath();
				int index = aux.lastIndexOf("class_mutants");
				aux = aux.substring(index);
				index = aux.indexOf("/") + 1;
				aux = aux.substring(index);

				File newPath = new File(newDirName + aux);
				newPath.mkdirs();
				Files.copy(file.toPath(), newPath.toPath(), StandardCopyOption.REPLACE_EXISTING);

				index = aux.indexOf("/");
				String mutantOp = aux.substring(0, index);
				mutantOperators.add(mutantOp);
			}

			logger.info("File copied to: " + file.getAbsolutePath());
		}

		List<String> mutants = new ArrayList<String>();
		for (String mop : mutantOperators) {
			mutants.add(mop);
		}
		return mutants;

	}

	public List<String> organizeFilesByOperator(String mujavaSession) throws IOException {
		String mujavaResultDir = mujavaSession + "/result/";
		String originalDir = mujavaSession + SafeRefactorDriver.SAFEREFACTOR_ORIGINAL_DIR;
		String mutantsDir = mujavaSession + SafeRefactorDriver.SAFEREFACTOR_MUTANTS_DIR;

		new File(originalDir).mkdirs();
		new File(mutantsDir).mkdirs();

		List<File> javaFiles = Utils.listFilesAndFilesSubDirectories(mujavaResultDir, ".java");
		for (File file : javaFiles) {
			if (file.getAbsolutePath().contains("original")) {
				File newPath = new File(originalDir + file.getName());
				Files.copy(file.toPath(), newPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} else if (file.getAbsolutePath().contains("traditional_mutants")) {
				String aux = file.getAbsolutePath();
				int index = aux.lastIndexOf("traditional_mutants");
				aux = aux.substring(index);
				index = aux.indexOf("/") + 1;
				aux = aux.substring(index);
				index = aux.indexOf("/") + 1;
				aux = aux.substring(index);

				File newPath = new File(mutantsDir + aux);
				newPath.mkdirs();
				Files.copy(file.toPath(), newPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} else if (file.getAbsolutePath().contains("class_mutants")) {
				String aux = file.getAbsolutePath();
				int index = aux.lastIndexOf("class_mutants");
				aux = aux.substring(index);
				index = aux.indexOf("/") + 1;
				aux = aux.substring(index);

				File newPath = new File(mutantsDir + aux);
				newPath.mkdirs();
				Files.copy(file.toPath(), newPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}

			logger.info("File copied to: " + file.getAbsolutePath());
		}

		List<File> mutantsDirs = Utils.listDirectories(mutantsDir);
		List<String> mutantOperators = new ArrayList<String>();
		for (File dir : mutantsDirs) {
			mutantOperators.add(dir.getName());
		}
		return mutantOperators;
	}

}
