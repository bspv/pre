package com.bazzi.pre.tests.csv;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ScenePo {
    @CsvBindByName(column = "province_id")
    private String provinceId;

    @CsvBindByName(column = "province")
    private String province;

    @CsvBindByName(column = "city_id")
    private String cityId;

    @CsvBindByName(column = "city")
    private String city;

    @CsvBindByName(column = "scene_type")
    private String sceneType;

    @CsvBindByName(column = "firstscene")
    private String firstScene;

    @CsvBindByName(column = "secondscene")
    private String secondScene;

    @CsvBindByName(column = "scenename")
    private String sceneName;

    @CsvBindByName(column = "sceneid")
    private String sceneId;

    @CsvBindByName(column = "gpsrange")
    private String gpsRange;
}
