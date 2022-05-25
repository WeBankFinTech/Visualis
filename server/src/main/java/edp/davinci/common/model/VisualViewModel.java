package edp.davinci.common.model;

/**
 * Entity class representing indicators(指标) and dimensions(维度) in virtualView
 * */
public class VisualViewModel {

    String sqlType;

    String visualType;

    String modelType;

    public String getSqlType() {
        return sqlType;
    }

    public void setSqlType(String sqlType) {
        this.sqlType = sqlType;
    }

    public String getVisualType() {
        return visualType;
    }

    public void setVisualType(String visualType) {
        this.visualType = visualType;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }
}
