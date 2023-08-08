/*
 * <<
 *  Davinci
 *  ==
 *  Copyright (C) 2016 - 2019 EDP
 *  ==
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  >>
 *
 */

package edp.davinci.service.screenshot;

import com.alibaba.druid.util.StringUtils;
import com.webank.wedatasphere.dss.visualis.configuration.CommonConfig;
import edp.core.utils.ServerUtils;
import edp.davinci.dao.UserMapper;
import edp.davinci.model.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.linkis.adapt.LinkisUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

import static edp.davinci.service.screenshot.BrowserEnum.valueOf;

@Slf4j
@Component
public class ScreenshotUtil {

    @Value("${screenshot.default_browser:PHANTOMJS}")
    private String DEFAULT_BROWSER;

    @Value("${screenshot.chromedriver_path:}")
    private String CHROME_DRIVER_PATH;

    @Value("${screenshot.phantomjs_path:}")
    private String PHANTOMJS_PATH;

    @Value("${screenshot.timeout_second:600}")
    private int timeOutSecond;

    @Autowired
    ServerUtils serverUtils;

    @Autowired
    private UserMapper userMapper;


    private static final int DEFAULT_SCREENSHOT_WIDTH = 1920;
    private static final int DEFAULT_SCREENSHOT_HEIGHT = 1080;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(200);


    public void screenshot(long userId, long jobId, List<ImageContent> imageContents) {
        User user = userMapper.getById(userId);
        log.info("start screenshot for job: {}, and set screenshot time out second is: {}", jobId, timeOutSecond);
        try {
            CountDownLatch countDownLatch = new CountDownLatch(imageContents.size());
            List<Future> futures = new ArrayList<>(imageContents.size());
            imageContents.forEach(content -> futures.add(executorService.submit(() -> {
                log.info("thread for screenshot start, type: {}, id: {}", content.getDesc(), content.getCId());
                try {
                    File image = doScreenshot(content.getUrl(), user.username);
                    if (null != image) {
                        log.info("Finished doing screenshot, file path: {}", image.getAbsolutePath());
                        content.setContent(image);
                    } else {
                        log.info("Screenshot failed. Set the picture content to null.");
                        content.setContent(null);
                    }
                } catch (Exception e) {
                    log.error("error ScreenshotUtil.screenshot, ", e);
                } finally {
                    countDownLatch.countDown();
                    log.info("thread for screenshot finish, type: {}, id: {}", content.getDesc(), content.getCId());
                }
            })));

            try {
                for (Future future : futures) {
                    future.get();
                }
                countDownLatch.await();
            } catch (ExecutionException e) {
                log.error("screenshot error: ", e);
            }

            imageContents.sort(Comparator.comparing(ImageContent::getOrder));

        } catch (InterruptedException e) {
            log.error("screenshot thread gets interrupted, ", e);
        } finally {
            log.info("finish screenshot for job: {}", jobId);
        }
    }


    private File doScreenshot(String url, String username) throws Exception {
        url = getUrlWithEnv(url);
        WebDriver driver = generateWebDriver();
        driver.get(serverUtils.getServerUrl());
        Cookie ticketCookie = new Cookie(CommonConfig.TICKET_ID_STRING().getValue(), LinkisUtils.getUserTicketKV(username)._2,
                serverUtils.getAccessAddress(), "/", new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 30L));

        Cookie innerCookie = new Cookie("dataworkcloud_inner_request", "true", serverUtils.getAccessAddress(),
                "/", new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 30L));

        driver.manage().addCookie(ticketCookie);
        driver.manage().addCookie(innerCookie);

        driver.get(url);

        log.info("for user {} getting... {}", username, url);
        try {

            log.info("Start the screenshot and set the timeout value is {}", timeOutSecond);
            WebDriverWait wait = new WebDriverWait(driver, timeOutSecond);

            ExpectedCondition<WebElement> ConditionOfSign = ExpectedConditions.presenceOfElementLocated(By.id("headlessBrowserRenderSign"));
            ExpectedCondition<WebElement> ConditionOfWidth = ExpectedConditions.presenceOfElementLocated(By.id("width"));
            ExpectedCondition<WebElement> ConditionOfHeight = ExpectedConditions.presenceOfElementLocated(By.id("height"));
            // WidgetExecuteFailedTag
            ExpectedCondition<WebElement> ConditionOfWidgetExecuteFailedTag =
                    ExpectedConditions.presenceOfElementLocated(By.id("WidgetExecuteFailedTag"));

            wait.until(ExpectedConditions.or(ConditionOfSign, ConditionOfWidgetExecuteFailedTag, ConditionOfWidth, ConditionOfHeight));

            WebElement widgetExecuteFailedTag = null;

            widgetExecuteFailedTag = waitUntilElementInvisible(By.id("WidgetExecuteFailedTag"), driver);

            if (null == widgetExecuteFailedTag) {

                String widthVal = driver.findElement(By.id("width")).getAttribute("value");
                String heightVal = driver.findElement(By.id("height")).getAttribute("value");

                int width = DEFAULT_SCREENSHOT_WIDTH;
                int height = DEFAULT_SCREENSHOT_HEIGHT;

                if (!StringUtils.isEmpty(widthVal)) {
                    log.info("Browser resolution width is {}", widthVal);
                    width = Integer.parseInt(widthVal);
                } else {
                    log.info("The browser resolution width is the default: {}", width);
                }

                if (!StringUtils.isEmpty(heightVal)) {
                    log.info("Browser resolution height is {}", heightVal);
                    height = Integer.parseInt(heightVal);
                } else {
                    log.info("The browser resolution height is the default: {}", height);
                }
                driver.manage().window().setSize(new Dimension(width, height));
                Thread.sleep(2000);
                // 是否能通过driver去获取这个share html页面 url
                return ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            } else {
                log.error("When the screenshot is taken, the widget execution fails and the widget WidgetExecuteFailedTag tag is captured!");
                return null;
            }
        } catch (InterruptedException e) {
            log.error("do screenshot thread gets interrupted, ", e);
        } finally {
            log.info("for user {} finished getting {}, webdriver will quit soon", username, url);
            driver.quit();
        }
        return null;
    }

    private WebDriver generateWebDriver() throws ExecutionException {
        WebDriver driver;
        BrowserEnum browserEnum = valueOf(DEFAULT_BROWSER);
        switch (browserEnum) {
            case CHROME:
                driver = generateChromeDriver();
                break;
            case PHANTOMJS:
                driver = generatePhantomJsDriver();
                break;
            default:
                throw new IllegalArgumentException("Unknown Web browser :" + DEFAULT_BROWSER);
        }

        driver.manage().timeouts().implicitlyWait(3, TimeUnit.MINUTES);
        driver.manage().window().maximize();
        driver.manage().window().setSize(new Dimension(DEFAULT_SCREENSHOT_WIDTH, DEFAULT_SCREENSHOT_HEIGHT));

        return driver;
    }


    private WebDriver generateChromeDriver() throws ExecutionException {
        File file = new File(CHROME_DRIVER_PATH);
        if (!file.canExecute()) {
            if (!file.setExecutable(true)) {
                throw new ExecutionException(new Exception(CHROME_DRIVER_PATH + "is not executable!"));
            }
        }

        log.info("Generating Chrome driver ({})...", CHROME_DRIVER_PATH);
        System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, CHROME_DRIVER_PATH);
        ChromeOptions options = new ChromeOptions();

        options.addArguments("headless");
        options.addArguments("no-sandbox");
        options.addArguments("disable-gpu");
        options.addArguments("disable-features=NetworkService");
        options.addArguments("ignore-certificate-errors");
        options.addArguments("silent");
        options.addArguments("--disable-application-cache");

        options.addArguments("disable-dev-shm-usage");
        options.addArguments("remote-debugging-port=9012");

        return new ChromeDriver(options);
    }

    private WebDriver generatePhantomJsDriver() throws ExecutionException {
        File file = new File(PHANTOMJS_PATH);
        if (!file.canExecute()) {
            if (!file.setExecutable(true)) {
                throw new ExecutionException(new Exception(PHANTOMJS_PATH + "is not executable!"));
            }
        }
        log.info("Generating PhantomJs driver ({})...", PHANTOMJS_PATH);
        System.setProperty(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, PHANTOMJS_PATH);
        PhantomJSDriver phantomJSDriver = null;
        try {
            phantomJSDriver = new PhantomJSDriver();
        } catch (final Exception e) {
            //初始化失败，需要进行下重试一次,如果两次都失败了，基本就证明本时段不可用
            log.warn("failed to new PhantomJSDriver, we will do it once again", e);
            phantomJSDriver = new PhantomJSDriver();
        }
        return phantomJSDriver;
    }

    private String getUrlWithEnv(String url) {
        String env = CommonConfig.ACCESS_ENV().getValue();
        if (org.apache.commons.lang.StringUtils.isBlank(env)) {
            return url;
        }
        url = url.replace("?", "?env=" + env + "&");
        return url;
    }

    /**
     * 缺陷记录：
     * 之前使用widgetExecuteFailedTag = driver.findElement(By.id("WidgetExecuteFailedTag"));来获取失败元素，
     * 通过判断widgetExecuteFailedTag是否为null，来判断是否执行成功，这样会导致一个问题。当执行成功是WidgetExecuteFailedTag对象并没有产生
     * 导致findElement会去长时间搜索该对象，截图操作效率降低很多，目前通过如下方法来设置超时
     * */
    private WebElement waitUntilElementInvisible(By element, WebDriver driver) {
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        WebElement expectElement = null;
        try {
            expectElement = driver.findElement(element);
        } catch (NoSuchElementException e) {
            log.info("When the screenshot page is opened, the widget execution failure tag is not found. So Screenshot successful!");
        }
        driver.manage().timeouts().implicitlyWait(timeOutSecond, TimeUnit.SECONDS);
        return expectElement;
    }
}
