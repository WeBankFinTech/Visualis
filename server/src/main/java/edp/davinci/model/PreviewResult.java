package edp.davinci.model;

import java.util.Date;

public class PreviewResult {

    private Long id;

    private String  execId;

    private String name;

    private String status;

    private String description;

    private byte[] result;

    private String createBy;

    private Date createTime;

    private boolean isArchive;

    public PreviewResult() {
    }

    public PreviewResult(String execId, String name, String status, String description, String createBy, Date createTime, boolean isArchive) {
        this.execId = execId;
        this.name = name;
        this.status = status;
        this.description = description;
        this.result = result;
        this.createBy = createBy;
        this.createTime = createTime;
        this.isArchive = isArchive;
    }

    public PreviewResult(String execId, String name, String status, String description, byte[] result, String createBy, Date createTime, boolean isArchive) {
        this.execId = execId;
        this.name = name;
        this.status = status;
        this.description = description;
        this.result = result;
        this.createBy = createBy;
        this.createTime = createTime;
        this.isArchive = isArchive;
    }

    public PreviewResult(Long id, String execId, String name, String status, String description, byte[] result, String createBy, Date createTime, boolean isArchive) {
        this.id = id;
        this.execId = execId;
        this.name = name;
        this.status = status;
        this.description = description;
        this.result = result;
        this.createBy = createBy;
        this.createTime = createTime;
        this.isArchive = isArchive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public String getExecId() {
        return execId;
    }

    public void setExecId(String execId) {
        this.execId = execId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getResult() {
        return result;
    }

    public void setResult(byte[] result) {
        this.result = result;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public boolean isArchive() {
        return isArchive;
    }

    public void setArchive(boolean archive) {
        isArchive = archive;
    }
}
