package com.innerwave.gen.classdoc.domain;

import com.innerwave.gen.classdoc.GenConstant;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 클래스 정보
 *
 * @author yjkim
 */
@Data
public class ClassInfo
{
    // 클래스 명
    private String name;
    // 패키지
    private String packageName;
    // 클래스 설명
    private String description;
    // 스테레오 타입
    private String steraioType;
    // 상위 클래스
    private String superName;

    private int filledCnt = 0;
    private double percentFilled = 0d;
    private boolean isFilled = true;

    // 속성 리스트
    List<PropertyInfo> propertyInfoList = new ArrayList<>();
    // 오퍼레이션 리스트
    List<OperationInfo> operationInfoList = new ArrayList<>();

    public void calPercentFilled()
    {
        Long lcnt = propertyInfoList.stream().filter(item -> StringUtils.isNotBlank(item.getDescription())).count()
                + operationInfoList.stream().filter(item -> StringUtils.isNotBlank(item.getDescription())).count();
        int fillCnt = lcnt.intValue();
        this.filledCnt = fillCnt;

        if(GenConstant.CAL_FILLED) {
            int totalCnt = propertyInfoList.size() + operationInfoList.size();

            if(totalCnt == 0 || fillCnt == 0)
            {
                this.isFilled = false;
                return;
            }
            this.percentFilled = (fillCnt * 100) / totalCnt;
            this.isFilled = percentFilled >= GenConstant.CAL_FILLED_THRESHOLD;
        }

    }
}
