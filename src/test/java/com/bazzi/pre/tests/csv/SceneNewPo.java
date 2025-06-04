package com.bazzi.pre.tests.csv;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SceneNewPo {
    @CsvBindByPosition(position = 0)
    private Integer provinceId;

    @CsvBindByPosition(position = 1)
    private String province;

    @CsvBindByPosition(position = 2)
    private String cityId;

    @CsvBindByPosition(position = 3)
    private String city;

    @CsvBindByPosition(position = 4)
    private String sceneType;

    @CsvBindByPosition(position = 5)
    private String firstScene;

    @CsvBindByPosition(position = 6)
    private String secondScene;

    @CsvBindByPosition(position = 7)
    private String sceneName;

    @CsvBindByPosition(position = 8)
    private String sceneId;

    @CsvBindByPosition(position = 9)
    private String gpsRange;

//    @CsvBindByPosition(position = 10)
    private String areaType;
}
