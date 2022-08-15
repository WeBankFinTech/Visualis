package edp.davinci.service.screenshot;

public class HtmlContent {
    private int order;

    private Long cId;

    private String desc;

    private String htmpPage;

    private String url;

    public HtmlContent(int order, Long cId, String htmpPage, String url) {
        this.order = order;
        this.cId = cId;
        this.desc = desc;
        this.htmpPage = htmpPage;
        this.url = url;
    }

    public HtmlContent() {}

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Long getcId() {
        return cId;
    }

    public void setcId(Long cId) {
        this.cId = cId;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getHtmpPage() {
        return htmpPage;
    }

    public void setHtmpPage(String htmpPage) {
        this.htmpPage = htmpPage;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
