package com.innerwave.gen.classdoc;

import com.innerwave.gen.classdoc.generator.GenWordService;
import com.innerwave.gen.classdoc.javadoc.WordDoclet;
import com.sun.tools.javadoc.Main;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class GenClassDocLauncher implements ApplicationRunner
{
	public static void main(String[] args)
	{
		SpringApplication.run(GenClassDocLauncher.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws IOException
	{
		/**
		 * classpath 미리 준비되어 있어야한다.
		 */
		StringBuffer sbClassPath = new StringBuffer();

		File classFile = new File(GenConstant.PJ_CLASSPATH_FILE_PATH);
		if(classFile.exists() && classFile.isFile())
		{
			List<String> lines = FileUtils.readLines(classFile, StandardCharsets.UTF_8);
			for(String line : lines)
			{
				sbClassPath.append(line).append(":");
			}
		}

		if(sbClassPath.length() < 1)
		{

			log.error("########################################");
			log.error("First, write the '{}' file.",classFile.getAbsolutePath());
			log.error("\n>>>>>>>>>>>>> gradle task <<<<<<<<<<<<<\n" +
					"task printClasspath(){\n" +
					"        doLast {\n" +
					"            println \"\\n======= classpath ========\"\n" +
					"            sourceSets.main.compileClasspath.files.each {\n" +
					"                print it.path+\":\"\n" +
					"            }\n" +
					"            sourceSets.main.runtimeClasspath.files.each {\n" +
					"                print it.path+\":\"\n" +
					"            }\n" +
					"        }\n" +
					"}");
			log.error("########################################");
			return;
		}

		/**
		 * javadoc 실행
		 */
		File logFile = new File(Paths.get(GenConstant.DOCLET_OUTPUT_PATH,GenConstant.DOCLET_LOG_FILE).toString());
		PrintWriter pw = new PrintWriter(logFile);
		for(String pjModule : GenConstant.PJ_MODULES)
		{
			String pjSourcePath = String.format(GenConstant.PJ_SOURCE_PATH,pjModule);
			GenConstant.threadLocal.set(pjModule);
			Main.execute("WordDoclet",
					pw,
					pw,
					pw,
					WordDoclet.class.getName()
					,new String[] {
					"-private" ,"-quiet"
					, "-encoding" , GenConstant.UTF8
					, "-classpath", sbClassPath.toString()
					, "-sourcepath", pjSourcePath
					, "-subpackages", GenConstant.SUB_PACKAGE
			});
		}

		if(GenConstant.MAKE_ONE_FILE)
		{
			GenConstant.threadLocal.set("all("+GenConstant.PJ_MODULES.size()+")");
			GenWordService genWordService = new GenWordService();
			genWordService.writeFile(GenConstant.classInfoList);
		}
	}

}
