package framework.model;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {
    private Map<String, Object> model;
    private String url;

    public ModelAndView() {
        this.model = new HashMap<>();
    }

    public ModelAndView(String url) {
        this();
        this.url = url;
    }

    public ModelAndView(String url, Map<String, Object> model) {
        this.url = url;
        this.model = model;
    }

    public Map<String, Object> getModel() {
        return model;
    }

    public void setModel(Map<String, Object> model) {
        this.model = model;
    }

    public void addAttribute(String name, Object value) {
        this.model.put(name, value);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
