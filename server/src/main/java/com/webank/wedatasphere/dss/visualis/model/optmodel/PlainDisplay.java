package com.webank.wedatasphere.dss.visualis.model.optmodel;

import edp.davinci.model.Display;
import edp.davinci.model.DisplaySlide;
import edp.davinci.model.MemDisplaySlideWidget;
import lombok.Data;

import java.util.List;

@Data
public class PlainDisplay {
    Display display;
    DisplaySlide displaySlide;
    List<MemDisplaySlideWidget> memDisplaySlideWidgets;

}
