package com.aiqaos.observability.dashboard;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class WidgetRegistry {
    private final List<String> widgets = new ArrayList<>();

    public void registerWidget(String widget) {
        widgets.add(widget);
    }

    public List<String> getWidgets() {
        return widgets;
    }
}