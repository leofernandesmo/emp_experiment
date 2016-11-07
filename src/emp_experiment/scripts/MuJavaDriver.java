package emp_experiment.scripts;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MuJavaDriver {
	
	private static Logger logger = Logger.getLogger(Utils.LOGGER_NAME);

	public void execute(String output, String jdollyOutput, String sessionName) throws Exception {
		makeMujavaConfigFile(output);
		
//		organizeAndCompile(output, jdollyOutput);

		String[] argsMujava = { sessionName };
		generateMutants(argsMujava);

	}

	private void makeMujavaConfigFile(String output) throws IOException {
		FileWriter fw = new FileWriter("mujava.config");
		fw.write("MuJava_HOME="+output);
		fw.close();
		
		fw = new FileWriter("mujavaCLI.config");
		fw.write("MuJava_HOME="+output);
		fw.close();
	}

	private void organizeAndCompile(String output, String jdollyOutput) throws IOException, InterruptedException {
		// Create MuJava necessary infrastructure
		File mujavaSrcDir = new File(output +  "/src/");
		File mujavaClassesDir = new File(output +  "/classes/");
		File mujavaResultDir = new File(output + "/result/");
		File mujavaTestsetDir = new File(output + "/testset/");
		mujavaSrcDir.mkdirs();
		mujavaClassesDir.mkdirs();
		mujavaResultDir.mkdirs();
		mujavaTestsetDir.mkdirs();

		// Change class name and file name to avoid compiler confusion
		int count = 0;
		List<File> javaFiles = Utils.listFilesAndFilesSubDirectories(jdollyOutput, ".java");
		List<String> filesToCompile = new ArrayList<String>();
		List<Thread> listOfThreads = new ArrayList<Thread>();
		for (File javaFile : javaFiles) {
			Utils.replaceClassName(javaFile, Integer.toString(count));
			String newFileName = mujavaSrcDir.getAbsolutePath() + "/"
					+ javaFile.getName().replace("ClassId_0", "ClassId_" + count);

			Path source = javaFile.toPath();
			Path target = Paths.get(newFileName);
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

			count++;
			// A cada X arquivos inicio uma thread para compilar os arquivos
			// x == 10).
//			if (count % 10 == 0) {
//				Thread t = new Thread(new Compiler(filesToCompile, mujavaClassesDir.getAbsolutePath()));
//				listOfThreads.add(t);
//				t.start();
//				filesToCompile = new ArrayList<String>();
//			} else {
//				filesToCompile.add(newFileName);
//			}

			// Compiling file
			 Process pro = Runtime.getRuntime().exec("javac " + newFileName + " -d " +
					 mujavaClassesDir.getAbsolutePath());
			 pro.getErrorStream(); // Logar os erros
			 pro.getInputStream();
			 pro.waitFor();
			 System.out.println("File " + newFileName + "= Exit status:" +
			 pro.exitValue());

		}
		// Compila os arquivos remanescentes
		if (filesToCompile != null && filesToCompile.size() > 0) {
			Thread t = new Thread(new Compiler(filesToCompile, mujavaClassesDir.getAbsolutePath()));
			listOfThreads.add(t);
			t.start();
		}

		for (Thread thread : listOfThreads) {
			thread.join();
		}
	}

	private void generateMutants(String[] argsMujava) throws Exception {
		// Generate mutants with MuJava
//		mujava.cli.genmutes.main(argsMujava);
		
		mujava.gui.GenMutantsMain.main(argsMujava);
		System.out.println("Finalizou GUI!!!");
		
		
	}

}
