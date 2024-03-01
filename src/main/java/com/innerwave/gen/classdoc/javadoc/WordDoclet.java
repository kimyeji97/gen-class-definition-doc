package com.innerwave.gen.classdoc.javadoc;


import com.innerwave.gen.classdoc.GenConstant;
import com.innerwave.gen.classdoc.domain.ClassInfo;
import com.innerwave.gen.classdoc.domain.OperationInfo;
import com.innerwave.gen.classdoc.domain.PropertyInfo;
import com.innerwave.gen.classdoc.generator.GenWordService;
import com.sun.javadoc.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class WordDoclet extends Doclet
{
    public static boolean start(RootDoc root)
    {
        System.out.println("WordDoclet start!!!");

        List<ClassInfo> listClassInfo = new ArrayList<>();
        List<ClassDoc> classDocs = Arrays.asList(root.classes());
        for(ClassDoc cd : classDocs)
        {
            ClassInfo classInfo = WordDoclet.getClassInfo(cd);
            if(classInfo != null && classInfo.isFilled())
            {
                listClassInfo.add(classInfo);
            }
        }

        printConsole(listClassInfo);

        System.out.println("\ndoclet end : class file "+ listClassInfo.size() + "건.\n");


        if(GenConstant.MAKE_ONE_FILE)
        {
            GenConstant.classInfoList.addAll(listClassInfo);
        }
        else
        {
            GenWordService genWordService = new GenWordService();
            genWordService.writeFile(listClassInfo);
        }

        return true;
    }

    public static String replaceComment(String comment)
    {
        String result = comment;
        for(String replace : GenConstant.REPLACE_LIST)
        {
            result = result.replace(replace, "");
        }
        return result;
    }

    public static void main(String[] args) {

    }
    public static ClassInfo getClassInfo(ClassDoc classDoc)
    {
        ClassInfo classInfo = new ClassInfo();

        /*
         * 클래스 정보
         */
        // 클래스 명
        classInfo.setName(classDoc.simpleTypeName());
        // 설명
        classInfo.setDescription(WordDoclet.replaceComment(classDoc.commentText()));
        // 패키지
        classInfo.setPackageName(classDoc.containingPackage().name());
        // 상위 클래스 명
        if(classDoc.superclass() != null)
        {
            ClassDoc superClassDoc = classDoc.superclass().equals("Object") ? null : classDoc.superclass();
            if(superClassDoc != null && superClassDoc.simpleTypeName().equalsIgnoreCase("object") == false)
            {
                classInfo.setSuperName(superClassDoc.simpleTypeName());
            }
        }

        if(StringUtils.isBlank(classInfo.getDescription()))
        {
            // 클래스명
            StringBuffer sb = new StringBuffer();
            sb.append(classInfo.getName()).append(" 클래스");
            // 인터페이스 정보
            if(classDoc.interfaces().length > 0) {
                sb.append(" ( 인터페이스 : ");
                for(int idx =0; idx < classDoc.interfaces().length; idx ++) {
                    sb.append(classDoc.interfaces()[idx]).append(",");
                }
                classInfo.setDescription( sb.toString().substring(0, sb.length() -1) + ")");
            } else {
                classInfo.setDescription(sb.toString());
            }
        }

        /*
         * 속성 정보 (필드)
         */
        classInfo.setPropertyInfoList(WordDoclet.getListPropertyInfo(classDoc.qualifiedName(), classDoc.fields()));


        /*
         * 오퍼레이션 정보 (메소드)
         */
        classInfo.setOperationInfoList(WordDoclet.getListOperationInfo(classDoc.qualifiedName(),classDoc.methods()));

        if((classInfo.getPropertyInfoList() == null || classInfo.getPropertyInfoList().size() < 1)
                && (classInfo.getOperationInfoList() == null || classInfo.getOperationInfoList().size() < 1) )
        {
            return null;
        }


        classInfo.calPercentFilled();
        return classInfo;
    }

    public static List<PropertyInfo> getListPropertyInfo(String classPackage, FieldDoc[] fieldDocs)
    {
        // 속성 리스트
        if(fieldDocs == null)
        {
            return new ArrayList<>();
        }

        List<PropertyInfo> listPropertyInfo = new ArrayList<>();
        for(FieldDoc fieldDoc : Arrays.asList(fieldDocs))
        {
            PropertyInfo propertyInfo = new PropertyInfo();
            propertyInfo.setClassPackageName(classPackage);
            // 이름
            propertyInfo.setName(fieldDoc.name());
            // 접근자
            propertyInfo.setAccessor(fieldDoc.modifiers());
            // 속성 타입
            propertyInfo.setType(fieldDoc.type().simpleTypeName());
            // 기본값
            propertyInfo.setDefaultValue(fieldDoc.constantValueExpression() != null ? fieldDoc.constantValueExpression().replaceAll("\"","") : null);
            // 설명
            propertyInfo.setDescription(WordDoclet.replaceComment(fieldDoc.commentText()));

            if(StringUtils.isBlank(propertyInfo.getDescription()) && StringUtils.isNotBlank(propertyInfo.getDefaultValue()))
            {
                propertyInfo.setDescription(String.format("선언시 초기화 : %s",propertyInfo.getDefaultValue()));
            }

            listPropertyInfo.add(propertyInfo);
        }

        return listPropertyInfo;
    }

    public static List<OperationInfo> getListOperationInfo(String classPackage, MethodDoc[] methodDocs)
    {
        if(methodDocs == null)
        {
            return new ArrayList<>();
        }

        List<OperationInfo> listOperationInfo = new ArrayList<>();
        for(MethodDoc methodDoc : Arrays.asList(methodDocs))
        {
            OperationInfo operationInfo = new OperationInfo();
            operationInfo.setClassPackageName(classPackage);
            // 이름
            operationInfo.setName(methodDoc.name());
            // 접근자
            operationInfo.setAccessor(methodDoc.modifiers());
            // 파라미터
            operationInfo.setParameterList(Arrays.asList(methodDoc.flatSignature()));
            // 리턴타입
            operationInfo.setReturnType(methodDoc.returnType().simpleTypeName());
            // 설명
            operationInfo.setDescription(WordDoclet.replaceComment(methodDoc.commentText()));

            listOperationInfo.add(operationInfo);
        }
        return listOperationInfo;
    }

    public static void printConsole(List<ClassInfo> listClassInfo)
    {
        StringBuffer sb = new StringBuffer();
        for(ClassInfo info : listClassInfo)
        {
            if(info == null)
            {
                continue;
            }
            sb.append("===================================================").append("\n");
            sb.append("[" + info.getName() + "] ")
                    .append("패키지 : " + info.getPackageName()).append(" / ")
                    .append("상위 : " + info.getSuperName()).append(" / ")
                    .append("설명 : " + info.getDescription()).append("\n");
            sb.append("1. 속성 목록").append("\n");
            for(PropertyInfo pinfo : info.getPropertyInfoList())
            {
                sb.append("    <"+pinfo.getName()+"> ")
                        .append("접근자 : "+pinfo.getAccessor()).append(" / ")
                        .append("속성 타입 : "+pinfo.getType()).append(" / ")
                        .append("기본값 : "+pinfo.getDefaultValue()).append(" / ")
                        .append("설명 : "+pinfo.getDescription()).append("\n");
            }
            sb.append("2. 오퍼레이션 목록").append("\n");
            for(OperationInfo oinfo : info.getOperationInfoList())
            {
                sb.append("    <"+oinfo.getName()+"> ")
                        .append("접근자 : "+oinfo.getAccessor()).append(" / ")
                        .append("파라미터 : "+oinfo.getParameterList()).append(" / ")
                        .append("리턴타입 : "+oinfo.getReturnType()).append(" / ")
                        .append("설명 : "+oinfo.getDescription()).append("\n");
            }
        }
        System.out.println(sb.toString());

    }
}
