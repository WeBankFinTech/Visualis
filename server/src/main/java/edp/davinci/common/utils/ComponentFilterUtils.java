package edp.davinci.common.utils;

import edp.davinci.dto.viewDto.ViewBaseInfo;
import edp.davinci.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComponentFilterUtils {

    Logger log = LoggerFactory.getLogger(ComponentFilterUtils.class);

    Pattern suffixVersionPattern = Pattern.compile("[v]\\d_[v][0-9]{6}");
    Pattern withoutSuffixVersionPattern = Pattern.compile("^.+(?<![v]\\d_[v][0-9]{6})$");
    Pattern namePattern = Pattern.compile("([a-zA-Z]+_\\d+).*");

    public List<Source> doFilterSources(List<Source> sources) {
        return null;
    }

    public List<ViewBaseInfo> doFilterViews(List<ViewBaseInfo> views) {
        String defaultVersion = "v1_v000000";
        HashMap<String, Tuple<String, ViewBaseInfo>> viewMap = new HashMap<>();
        for (ViewBaseInfo view : views) {
            String shortName = getShortName(view.getName());
            String version = getSuffixVersion(view.getName());
            if (null == version) {
                version = defaultVersion;
            }
            if (viewMap.containsKey(shortName)) {
                Tuple<String, ViewBaseInfo> currentItem = viewMap.get(shortName);
                String currentVersion = currentItem.first;
                if (currentVersion.compareTo(version) <= 0) {
                    currentItem.setFirst(version);
                    currentItem.setSecond(view);
                }
            } else {
                Tuple<String, ViewBaseInfo> item = new Tuple<>(version, view);
                viewMap.put(shortName, item);
            }
        }
        List<ViewBaseInfo> viewList = new ArrayList<>();
        for (Map.Entry<String, Tuple<String, ViewBaseInfo>> item : viewMap.entrySet()) {
            ViewBaseInfo selectView = item.getValue().getSecond();
            selectView.setName(item.getKey());
            viewList.add(selectView);
        }
        return viewList;
    }

    public List<Widget> doFilterWidgets(List<Widget> widgets) {
        String defaultVersion = "v1_v000000";
        HashMap<String, Tuple<String, Widget>> widgetMap = new HashMap<>();
        for (Widget widget : widgets) {
            String shortName = getShortName(widget.getName());
            String version = getSuffixVersion(widget.getName());
            if (null == version) {
                version = defaultVersion;
            }
            if (widgetMap.containsKey(shortName)) {
                Tuple<String, Widget> currentItem = widgetMap.get(shortName);
                String currentVersion = currentItem.first;
                if (currentVersion.compareTo(version) <= 0) {
                    currentItem.setFirst(version);
                    currentItem.setSecond(widget);
                }
            } else {
                Tuple<String, Widget> item = new Tuple<>(version, widget);
                widgetMap.put(shortName, item);
            }
        }
        List<Widget> widgetList = new ArrayList<>();
        for (Map.Entry<String, Tuple<String, Widget>> item : widgetMap.entrySet()) {
            Widget selectWidget = item.getValue().getSecond();
            selectWidget.setName(item.getKey());
            widgetList.add(selectWidget);
        }
        return widgetList;
    }

    public List<Display> doFilterDisplays(List<Display> displays) {
        String defaultVersion = "v1_v000000";
        HashMap<String, Tuple<String, Display>> displayMap = new HashMap<>();
        for (Display display : displays) {
            String shortName = getShortName(display.getName());
            String version = getSuffixVersion(display.getName());
            if (null == version) {
                version = defaultVersion;
            }
            if (displayMap.containsKey(shortName)) {
                Tuple<String, Display> currentItem = displayMap.get(shortName);
                String currentVersion = currentItem.first;
                if (currentVersion.compareTo(version) <= 0) {
                    currentItem.setFirst(version);
                    currentItem.setSecond(display);
                }
            } else {
                Tuple<String, Display> item = new Tuple<>(version, display);
                displayMap.put(shortName, item);
            }
        }
        List<Display> displayList = new ArrayList<>();
        for (Map.Entry<String, Tuple<String, Display>> item : displayMap.entrySet()) {
            Display selectDisplay = item.getValue().getSecond();
            selectDisplay.setName(item.getKey());
            displayList.add(selectDisplay);
        }
        return displayList;
    }

    public List<DashboardPortal> doFilterDashboardPortal(List<DashboardPortal> dashboardPortals) {
        String defaultVersion = "v1_v000000";
        HashMap<String, Tuple<String, DashboardPortal>> dashboardPortalMap = new HashMap<>();
        for (DashboardPortal dashboardPortal : dashboardPortals) {
            String shortName = getShortName(dashboardPortal.getName());
            String version = getSuffixVersion(dashboardPortal.getName());
            if (null == version) {
                version = defaultVersion;
            }
            if (dashboardPortalMap.containsKey(shortName)) {
                Tuple<String, DashboardPortal> currentItem = dashboardPortalMap.get(shortName);
                String currentVersion = currentItem.first;
                if (currentVersion.compareTo(version) <= 0) {
                    currentItem.setFirst(version);
                    currentItem.setSecond(dashboardPortal);
                }
            } else {
                Tuple<String, DashboardPortal> item = new Tuple<>(version, dashboardPortal);
                dashboardPortalMap.put(shortName, item);
            }
        }
        List<DashboardPortal> dashboardPortalList = new ArrayList<>();
        for (Map.Entry<String, Tuple<String, DashboardPortal>> item : dashboardPortalMap.entrySet()) {
            DashboardPortal selectDashboardPortal = item.getValue().getSecond();
            selectDashboardPortal.setName(item.getKey());
            dashboardPortalList.add(selectDashboardPortal);
        }
        return dashboardPortalList;
    }

    private String getShortName(String longName) {
        String shortName;
        String version = getSuffixVersion(longName);
        if (null == version) {
            shortName = longName;
        } else {
            shortName = longName.substring(0, longName.length() - version.length() - 1);
        }
        log.info("Get component name: " + shortName);
        return shortName;
    }

    private String getSuffixVersion(String longName) {
        String version;
        Matcher matcherVersionPattern = suffixVersionPattern.matcher(longName);
        if (matcherVersionPattern.find()) {
            version = matcherVersionPattern.group();
            log.info("Get component version: " + version);
        } else {
            version = null;
            log.info("The component does not match the version.");
        }
        return version;
    }

    class Tuple<A, B> {
        public A first;
        public B second;

        public Tuple(A a, B b) {
            first = a;
            second = b;
        }

        public A getFirst() {
            return first;
        }

        public void setFirst(A first) {
            this.first = first;
        }

        public B getSecond() {
            return second;
        }

        public void setSecond(B second) {
            this.second = second;
        }

        public String toString() {
            return "(" + first + ", " + second + ")";
        }
    }
}
