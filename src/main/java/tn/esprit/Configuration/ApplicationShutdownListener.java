package tn.esprit.Configuration;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;
import tn.esprit.utility.LocationStorageUtil;

@Component
public class ApplicationShutdownListener implements ApplicationListener<ContextClosedEvent> {

    private final LocationStorageUtil locationStorageUtil;

    public ApplicationShutdownListener(LocationStorageUtil locationStorageUtil) {
        this.locationStorageUtil = locationStorageUtil;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        locationStorageUtil.keepOnlyFirstEntry();
        System.out.println("Shutdown detected: Only first location entry kept.");
    }
}
