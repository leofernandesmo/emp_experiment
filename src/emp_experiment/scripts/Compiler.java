package emp_experiment.scripts;

import java.io.IOException;
import java.util.List;

public class Compiler implements Runnable {

	private List<String> javaFiles;
	private String classDirectory;

	public Compiler(List<String> javaFiles, String classDirectory) {
		super();
		this.javaFiles = javaFiles;
		this.classDirectory = classDirectory;
	}

	public void compile(String javaFile) {
		Process pro;
		try {
			pro = Runtime.getRuntime().exec("javac " + javaFile + " -d " + this.classDirectory);
			pro.getErrorStream(); // Logar os erros
			pro.getInputStream();
			pro.waitFor();

			System.out.println("File " + javaFile + "=> Status:" + ((pro.exitValue() == 0) ? "Compiled" : "Failed"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		for (String javaFile : javaFiles) {
			compile(javaFile);
		}
	}

	// Implementar o metodo para compilar todos os arquivos Java de uma vez.
	// usando um arquivo e chamando: javac @arquivo
	// Depois pegar os arquivos que não compilam(pela saida de erro)
	// e deletar ou mover para outra pasta.
	public void compileJavaFiles(List<String> files) {
		// Compiling file
		// String sourceDir = output + sessionName + "/src/";
		// String classesDir = output + sessionName + "/classes/";
		// String cmd = "javac " + sourceDir + "*.java" + " -d " + classesDir +
		// " -Xmaxerrs -1 -Xmaxwarns -1 ";
		//
		// PrintWriter out = new PrintWriter(new File("saida"));
		// out.println(tempSaida);
		//
		// String cmd = "javac @saida -Xmaxerrs -1 -Xmaxwarns -1 ";
		// Process pro = Runtime.getRuntime().exec(cmd);
		// InputStream err = pro.getErrorStream(); // Logar os erros
		// InputStream inp = pro.getInputStream();
		// pro.waitFor();
		//
		// BufferedReader in = new BufferedReader(new InputStreamReader(err));
		// String line = null;
		// while ((line = in.readLine()) != null) {
		// System.out.println(line);
		// }
		//
		// System.out.println("File " + newFileName + "= Exit status:" +
		// pro.exitValue());

	}

}
