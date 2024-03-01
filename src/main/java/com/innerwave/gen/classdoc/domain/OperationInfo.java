package com.innerwave.gen.classdoc.domain;

import lombok.Data;

import java.util.List;

/**
 * 오퍼레이션 정보
 *
 * @author yjkim
 */
@Data
public class OperationInfo
{
    private String classPackageName;
    // 이름
    private String name;
    // 접근자
    private String accessor;
    // 파라미터
    private List<String> parameterList;
    // 리턴타입
    private String returnType;
    // 설명
    private String description;
}
