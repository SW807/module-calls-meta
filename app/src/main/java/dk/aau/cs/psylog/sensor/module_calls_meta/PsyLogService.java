package dk.aau.cs.psylog.sensor.module_calls_meta;

import dk.aau.cs.psylog.module_lib.ScheduledService;

public class PsyLogService extends ScheduledService {

    public PsyLogService(String name) {
        super(name);
    }

    @Override
    public void setScheduledTask() {
        scheduledTask = new CallHistoryListener(this);
    }
}
