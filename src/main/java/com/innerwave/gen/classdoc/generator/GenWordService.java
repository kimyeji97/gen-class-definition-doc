package com.innerwave.gen.classdoc.generator;

import com.innerwave.gen.classdoc.GenConstant;
import com.innerwave.gen.classdoc.domain.ClassInfo;
import com.innerwave.gen.classdoc.domain.OperationInfo;
import com.innerwave.gen.classdoc.domain.PropertyInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
//@Service
public class GenWordService
{
    private static GenWordService genWordService;

    public GenWordService()
    {
        genWordService = this;
    }

    private XWPFDocument document = null;
    private XWPFParagraph paragraph = null;
    private XWPFRun line = null;
    private XWPFTable table = null;
    private XWPFTableRow row = null;
    private XWPFTableCell cell = null;


    private int DF_ROW_CNT = 1;
    private int FONT_SIZE = 10;
    private String PERCENT_100 = "100%";
    private String PERCENT_80 = "80%";
    private String PERCENT_32 = "35%";
    private String PERCENT_30 = "30%";
    private String PERCENT_20 = "20%";
    private String PERCENT_18 = "15%";
    private String PERCENT_10 = "10%";
    private int LINE_SPACING = 30;
    private String TITLE_COLOR_RGB_STR = "e0e0e0";


    public void writeFile(List<ClassInfo> listClassInfo)
    {
        if(listClassInfo == null || listClassInfo.size() < 1)
        {
            log.info("listClassInfo is null.");
            return;
        }

        log.info("============= START Create Word =============");

        List<ClassInfo> listClassInfotemp = listClassInfo.stream()
                .sorted(Comparator.comparing(ClassInfo::getFilledCnt).reversed())
                .collect(Collectors.toList());

        try
        {
            // 문서 생성
            document = new XWPFDocument();

            for(ClassInfo classInfo : listClassInfotemp)
            {
                this.setClassTable(classInfo);
                this.setPageSplit();
            }

            // 파일 쓰기
            this.makeFile();
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }

        log.info("=============  END  Create Word =============");
    }

    /**
     * 해당 경로에 파일 생성
     *
     * @throws IOException
     */
    private void makeFile() throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String pjModule = GenConstant.threadLocal.get();
        File file = new File(Paths.get(GenConstant.DOCLET_OUTPUT_PATH, String.format(GenConstant.DOCLET_OUTPUT_WORD,pjModule.replaceAll("/","_"), sdf.format(new Date()))).toString());
//        File file = new File(Paths.get(GenConstant.DOCLET_OUTPUT_PATH, String.format(GenConstant.DOCLET_OUTPUT_WORD,GenConstant.PJ_MODULE.replaceAll("/","_"), sdf.format(new Date()))).toString());
        FileOutputStream os = null;

        try
        {
            os = new FileOutputStream(file);
            document.write(os);
        }
        catch (IOException e)
        {
            if(os != null)
            {
                os.close();
            }
            document.close();
        }

        log.info("Output File : {}",file.getAbsolutePath());
    }

    /**
     * 페이지 나누기
     */
    private void setPageSplit()
    {
        paragraph = document.createParagraph();
        line = paragraph.createRun();
        line.addBreak(BreakType.PAGE);
    }

    /**
     * 테이블 생성
     */
    private void createTable()
    {
        table = document.createTable();
        table.setWidth(PERCENT_100);
        table.setTableAlignment(TableRowAlign.CENTER);
    }

    /**
     * 셀에 데이터 입력
     *
     * @param isTitle 제목 여부
     * @param cellIdx 셀 인덱스
     * @param text 입력 값
     * @param mergeCnt 병합 셀 개수 (수평)
     */
    private void setCell(boolean isTitle, int cellIdx, String text, int mergeCnt)
    {
        this.setCell(isTitle, cellIdx, text, mergeCnt, null);
    }

    /**
     * 셀에 데이터 입력
     *
     * @param isTitle 제목 여부
     * @param cellIdx 셀 인덱스
     * @param text 입력 값
     * @param mergeCnt 병합 셀 개수 (수평)
     * @param widthPercentage 셀 너비 퍼센트
     */
    private void setCell(boolean isTitle, int cellIdx, String text, int mergeCnt, String widthPercentage)
    {
        cell = row.getCell(cellIdx);
        if(cell == null)
        {
            cell = row.addNewTableCell();
        }

        String[] texts = text == null ? new String[] {""} : text.split(GenConstant.ENTER);
        for(int i = 0; i < texts.length; i++)
        {
            paragraph = i == 0 ? cell.getParagraphs().get(0) : cell.addParagraph();
            paragraph.setSpacingBeforeLines(LINE_SPACING);
            paragraph.setSpacingAfterLines(LINE_SPACING);
            line = paragraph.createRun();
            line.setText(texts[i]);
            line.setFontSize(FONT_SIZE);
        }

        // 제목 > 배경색 + 굵게
        if(isTitle == true)
        {
            cell.getCTTc().addNewTcPr().addNewShd().setFill(TITLE_COLOR_RGB_STR);
            paragraph.setAlignment(ParagraphAlignment.CENTER);
            line.setBold(true);
        }

        // 셀 병합
        if(mergeCnt > 1)
        {
            CTTcPr cttcpr = cell.getCTTc().addNewTcPr();
            cttcpr.addNewGridSpan();
            cttcpr.getGridSpan().setVal(BigInteger.valueOf((long) mergeCnt));
        }

        if(widthPercentage != null)
        {
            cell.setWidth(widthPercentage);
        }

        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
    }


    /**
     * merge하면서 생긴 불필요한 셀 제거
     *
     * @param cellCnt 남길 셀 개수
     */
    private void removeCell(int cellCnt)
    {
        int includeCnt = row.getTableCells().size();
        for(int idx = includeCnt-1 ; idx >= cellCnt; idx --)
        {
            row.removeCell(idx);
        }
    }

    /**
     * 클래스 정보 입력하기
     *
     * @param classInfo 클래스 정보
     */
    private void setClassTable(ClassInfo classInfo)
    {
        this.createTable();

        // 1행
        row = table.getRow(0);
        this.setCell(true, 0, "클래스 명", 1, PERCENT_20);
        this.setCell(false, 1, classInfo.getName(), 3, PERCENT_30);
        this.setCell(true, 2, "패키지", 1, PERCENT_18);
        this.setCell(false, 3, classInfo.getPackageName(), 1, PERCENT_32);

        // 2행
        row = table.createRow();
        this.setCell(true, 0, "클래스 설명", 1, PERCENT_20);
        this.setCell(false, 1, classInfo.getDescription(), 5, PERCENT_80);
        removeCell(2);

        // 3행
        row = table.createRow();
        this.setCell(true, 0, "스테레오타입", 1, PERCENT_20);
        this.setCell(false, 1, classInfo.getSteraioType(), 3, PERCENT_30);
        this.setCell(true, 2, "상위 클래스", 1, PERCENT_18);
        this.setCell(false, 3, classInfo.getSuperName(), 1, PERCENT_32);
        removeCell(4);

        // 속성
        this.setPropertyRows(classInfo.getPropertyInfoList());

        // 오퍼레이션
        this.setOperationRows(classInfo.getOperationInfoList());

    }

    /**
     * 속성 정보 입력하기
     *
     * @param propertyInfoList 속성 정보
     */
    private void setPropertyRows(List<PropertyInfo> propertyInfoList)
    {
        // 속성
        row = table.createRow();
        this.setCell(true, 0, "속성", 6);
        removeCell(1);

        // 이름 | 접근자 | 속성 타입 | 기본값 | 설명
        row = table.createRow();
        this.setCell(true, 0, "이름", 1, PERCENT_20);
        this.setCell(true, 1, "접근자", 1, PERCENT_10);
        this.setCell(true, 2, "속성타입", 1, PERCENT_10);
        this.setCell(true, 3, "기본값", 1, PERCENT_10);
        this.setCell(true, 4, "설명", 2, PERCENT_20);

        // 속성 데이터
        int rowCnt = DF_ROW_CNT;
        if(ObjectUtils.isEmpty(propertyInfoList) == false && propertyInfoList.size() > rowCnt)
        {
            rowCnt = propertyInfoList.size();
        }

        for(int idx = 0; idx < rowCnt; idx ++)
        {
            PropertyInfo propertyInfo = idx < propertyInfoList.size() ? propertyInfoList.get(idx) : new PropertyInfo();

            row = table.createRow();
            this.setCell(false, 0, propertyInfo.getName(), 1, PERCENT_20);
            this.setCell(false, 1, propertyInfo.getAccessor(), 1, PERCENT_10);
            this.setCell(false, 2, propertyInfo.getType(), 1, PERCENT_10);
            this.setCell(false, 3, propertyInfo.getDefaultValue(), 1, PERCENT_10);
            this.setCell(false, 4, propertyInfo.getDescription(), 2, PERCENT_20);
        }
    }

    /**
     * 오퍼레이션 정보 입력 하기
     *
     * @param operationInfoList 오퍼레이션 정보
     */
    private void setOperationRows(List<OperationInfo> operationInfoList)
    {
        // 오퍼레이션
        row = table.createRow();
        this.setCell(true, 0, "오퍼레이션", 6);
        removeCell(1);

        // 이름 | 접근자 | 파라미터 | 리턴타입 | 설명
        row = table.createRow();
        this.setCell(true, 0, "이름", 1, PERCENT_20);
        this.setCell(true, 1, "접근자", 1, PERCENT_10);
        this.setCell(true, 2, "파라미터", 2, PERCENT_20);
        this.setCell(true, 3, "리턴타입", 1, PERCENT_18);
        this.setCell(true, 4, "설명", 1, PERCENT_32);

        // 오퍼레이션 데이터
        int rowCnt = DF_ROW_CNT;
        if(ObjectUtils.isEmpty(operationInfoList) == false && operationInfoList.size() > rowCnt)
        {
            rowCnt = operationInfoList.size();
        }

        for(int idx = 0; idx < rowCnt; idx ++)
        {
            OperationInfo operationInfo = idx < operationInfoList.size() ? operationInfoList.get(idx) : new OperationInfo();

            row = table.createRow();
            this.setCell(false, 0, operationInfo.getName(), 1, PERCENT_20);
            this.setCell(false, 1, operationInfo.getAccessor(), 1, PERCENT_10);
            StringBuffer sb = new StringBuffer();
            if(operationInfo.getParameterList() != null)
            {
                for(String param : operationInfo.getParameterList())
                {
                    if(sb.length() > 0)
                    {
                        sb.append(", ");
                    }
                    sb.append(param);
                }
            }
            this.setCell(false, 2, sb.toString().replace("(","").replace(")",""), 2, PERCENT_20);
            this.setCell(false, 3, operationInfo.getReturnType(), 1, PERCENT_18);
            this.setCell(false, 4, operationInfo.getDescription(), 1, PERCENT_32);
        }
    }
}
