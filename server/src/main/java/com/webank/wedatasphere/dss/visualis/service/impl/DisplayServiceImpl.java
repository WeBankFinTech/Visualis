package com.webank.wedatasphere.dss.visualis.service.impl;

import com.google.common.collect.Lists;
import com.webank.wedatasphere.dss.visualis.service.DssDisplayService;
import com.webank.wedatasphere.dss.visualis.service.Utils;
import com.webank.wedatasphere.dss.visualis.model.optmodel.ExportedProject;
import com.webank.wedatasphere.dss.visualis.model.optmodel.IdCatalog;
import com.webank.wedatasphere.dss.visualis.model.optmodel.PlainDisplay;
import com.webank.wedatasphere.dss.visualis.utils.StringConstant;
import edp.davinci.dao.DisplayMapper;
import edp.davinci.dao.DisplaySlideMapper;
import edp.davinci.dao.MemDisplaySlideWidgetMapper;
import edp.davinci.model.Display;
import edp.davinci.model.DisplaySlide;
import edp.davinci.model.MemDisplaySlideWidget;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;


@Slf4j
@Service("dssDisplayService")
public class DisplayServiceImpl implements DssDisplayService {

    @Autowired
    DisplayMapper displayMapper;

    @Autowired
    DisplaySlideMapper displaySlideMapper;

    @Autowired
    private MemDisplaySlideWidgetMapper memDisplaySlideWidgetMapper;

    @Override
    public void exportDisplays(Long projectId, Map<String, Set<Long>> moduleIdsMap, boolean partial, ExportedProject exportedProject) throws Exception {
        List<PlainDisplay> exportedDisplays = Lists.newArrayList();
        List<Display> displays = Lists.newArrayList();
        if (partial) {
            Set<Long> idsSet = moduleIdsMap.get(StringConstant.DISPLAY_IDS);
            if (idsSet.size() > 0) {
                idsSet.stream().map(displayMapper::getById).forEach(displays::add);
            }
        } else {
            displays = displayMapper.getByProject(projectId);
        }
        for (Display display : displays) {
            PlainDisplay plainDisplay = new PlainDisplay();
            plainDisplay.setDisplay(display);
            plainDisplay.setDisplaySlide(displaySlideMapper.selectByDisplayId(display.getId()).get(0));
            List<MemDisplaySlideWidget> memDisplaySlideWidgets = memDisplaySlideWidgetMapper.getMemDisplaySlideWidgetListBySlideId(plainDisplay.getDisplaySlide().getId());
            memDisplaySlideWidgets.forEach(m -> moduleIdsMap.get(StringConstant.WIDGET_IDS).add(m.getWidgetId()));
            plainDisplay.setMemDisplaySlideWidgets(memDisplaySlideWidgets);
            exportedDisplays.add(plainDisplay);
        }
        log.info("exporting project, export displays: {}", exportedProject);
        exportedProject.setDisplays(exportedDisplays);
    }

    @Override
    public void importDisplay(Long projectId, String versionSuffix, ExportedProject exportedProject, IdCatalog idCatalog) throws Exception {
        List<PlainDisplay> displays = exportedProject.getDisplays();
        if (displays == null) {
            return;
        }
        for (PlainDisplay plainDisplay : displays) {
            Display display = plainDisplay.getDisplay();
            Long oldDisplayId = display.getId();
            display.setProjectId(projectId);
            display.setName(Utils.updateName(display.getName(), versionSuffix));
            Long existingId = displayMapper.getByNameWithProjectId(display.getName(), projectId);
            if (existingId != null) {
                display.setId(existingId);
                displaySlideMapper.deleteByDisplayId(display.getId());
                memDisplaySlideWidgetMapper.deleteByDisplayId(display.getId());
                idCatalog.getDisplay().put(oldDisplayId, display.getId());
            } else {
                displayMapper.insert(display);
                idCatalog.getDisplay().put(oldDisplayId, display.getId());
            }
            DisplaySlide displaySlide = plainDisplay.getDisplaySlide();
            Long oldSlideId = displaySlide.getId();
            displaySlide.setDisplayId(display.getId());
            displaySlideMapper.insert(displaySlide);
            idCatalog.getDisplaySlide().put(oldSlideId, displaySlide.getId());
            for (MemDisplaySlideWidget memDisplaySlideWidget : plainDisplay.getMemDisplaySlideWidgets()) {
                Long oldMemId = memDisplaySlideWidget.getId();
                memDisplaySlideWidget.setDisplaySlideId(displaySlide.getId());
                memDisplaySlideWidget.setWidgetId(idCatalog.getWidget().get(memDisplaySlideWidget.getWidgetId()));
                memDisplaySlideWidgetMapper.insert(memDisplaySlideWidget);
                idCatalog.getMemDisplaySlideWidget().put(oldMemId, memDisplaySlideWidget.getId());
            }
        }
    }

    @Override
    public void copyDisplay(Map<String, Set<Long>> moduleIdsMap, ExportedProject exportedProject) throws Exception {
        Set<Long> displayIds = moduleIdsMap.get(StringConstant.DISPLAY_IDS);
        if (!displayIds.isEmpty()) {
            PlainDisplay display = exportedProject.getDisplays().get(0);
            exportedProject.setDisplays(Lists.newArrayList(display));
        }
    }
}
