package com.innerwave.gen.classdoc.domain;

import lombok.Data;

/**
 * 속성 정보
 *
 * @author yjkim
 */
@Data
public class PropertyInfo
{
    private String classPackageName;
    // 이름
    private String name;
    // 접근자
    private String accessor;
    // 속성 타입
    private String type;
    // 기본값
    private String defaultValue;
    // 설명
    private String description;
}
