package edp.davinci.dto.viewDto;

import edp.davinci.model.Project;
import edp.davinci.model.View;
import lombok.Data;

@Data
public class ViewWithProject extends View {
    Project project;
}
