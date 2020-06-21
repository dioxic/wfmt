package uk.dioxic.wfmt.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.PersistenceConstructor;

import java.util.Objects;

@Data
@Builder
public class ActivitySummary {
    private final String activityId;
    private final Integer regionId;
    private final Activity.ActivityState state;
    // other summary fields

    public ActivitySummary(Activity activity) {
        activityId = activity.getActivityId();
        regionId = activity.getRegionId();
        state = activity.getState();
    }

    @PersistenceConstructor
    public ActivitySummary(String activityId, Integer regionId, Activity.ActivityState state) {
        this.activityId = activityId;
        this.regionId = regionId;
        this.state = state;
    }

    public static boolean summaryFieldsEqual(@NonNull Activity activity1, @NonNull Activity activity2) {
        return Objects.equals(activity1.getRegionId(), activity2.getRegionId()) &&
                Objects.equals(activity1.getState(), activity2.getState());
    }
}