package com.innerwave.gen.classdoc;

import com.innerwave.gen.classdoc.domain.ClassInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenConstant
{
    public static ThreadLocal<String> threadLocal = ThreadLocal.withInitial(String::new);
    public static final List<ClassInfo> classInfoList = new ArrayList<>();

    public static final String UTF8 = "UTF-8";
    public static final String SUB_PACKAGE = ".";
    public static final String ENTER = "\\\\n";

    /**
     * 프로젝트 관련 경로
     */
    public static final String PJ_ROOT_PATH = "/Users/yjkim/innerwave_source/git/caims";
    public static final List<String> PJ_MODULES = Arrays.asList(
            "caims-assurance/caims-fault"
//            ,"caims-collector"
//            ,"caims-batch"
//            ,"caims-data-processor/caims-convertor"
//            ,"caims-data-processor/flink-deployer"
//            ,"caims-service"
//            ,"caims-base"
//            ,"caims-ds"
    );
    //javadoc 추출 프로젝트 클래스파일
    public static final String PJ_CLASSPATH_FILE_PATH = "project/classpath.txt";
    // javadoc doclet에서 읽을 소스 경로
    public static final String PJ_SOURCE_PATH = PJ_ROOT_PATH + "/%s/src/main/java";

    /**
     * javadoc doclet 경로
     */
    public static final String DOCLET_OUTPUT_PATH = "output/";
    // javadoc 워드 문서
    public static final String DOCLET_OUTPUT_WORD = "%s_%s.docx";
    // javadoc log 파일명
    public static final String DOCLET_LOG_FILE = "javadoc.log";




    /**
     * 제거할 문구 리스트
     */
    public static final List<String> REPLACE_LIST = Arrays.asList("TODO","TODO YJ","jajakk","{@link","}","###","***");
    /**
     * 파일 한개로 생성 여부.
     */
    public static final boolean MAKE_ONE_FILE = true;
    /**
     * 설명 적힌 비율 계산 여부
     */
    public static final boolean CAL_FILLED = true;
    /**
     * 설명 적힌 비율 임계치 (임계치 이하인 클래스는 생성하지 않음)
     */
    public static final double CAL_FILLED_THRESHOLD = 20d ; // percent
}
