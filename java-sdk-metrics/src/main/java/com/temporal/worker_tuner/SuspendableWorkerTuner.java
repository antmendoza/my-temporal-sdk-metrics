package com.temporal.worker_tuner;

import com.google.common.annotations.VisibleForTesting;
import io.temporal.worker.tuning.*;

/**
 * SuspendableWorkerTuner is a composite worker tuner that can suspend and resume all types of slot suppliers.
 */
public class SuspendableWorkerTuner extends CompositeTuner {

    private final BaseLogger logger;
    private final SuspendableSlotSupplier<WorkflowSlotInfo> workflowTaskSlotSupplier;
    private final SuspendableSlotSupplier<ActivitySlotInfo> activityTaskSlotSupplier;
    private final SuspendableSlotSupplier<LocalActivitySlotInfo> localActivitySlotSupplier;
    private final SuspendableSlotSupplier<NexusSlotInfo> nexusSlotSupplier;


    @VisibleForTesting
    SuspendableWorkerTuner(
            BaseLogger logger,
            SuspendableSlotSupplier<WorkflowSlotInfo> workflowTaskSlotSupplier,
            SuspendableSlotSupplier<ActivitySlotInfo> activityTaskSlotSupplier,
            SuspendableSlotSupplier<LocalActivitySlotInfo> localActivitySlotSupplier,
            SuspendableSlotSupplier<NexusSlotInfo> nexusSlotSupplier) {
        super(workflowTaskSlotSupplier, activityTaskSlotSupplier, localActivitySlotSupplier, nexusSlotSupplier);
        this.logger = logger;
        this.workflowTaskSlotSupplier = workflowTaskSlotSupplier;
        this.activityTaskSlotSupplier = activityTaskSlotSupplier;
        this.localActivitySlotSupplier = localActivitySlotSupplier;
        this.nexusSlotSupplier = nexusSlotSupplier;
    }

    /**
     * @param callerName: identifier of the caller that is suspending
     */
    public void suspend(String callerName) {
        workflowTaskSlotSupplier.suspend();
        activityTaskSlotSupplier.suspend();
        localActivitySlotSupplier.suspend();
        nexusSlotSupplier.suspend();
        logger.info("SuspendableWorkerTuner: %s suspended by %s" + callerName);
    }

    /**
     * @param callerName: identifier of the caller that is resuming
     */
    public void resume(String callerName) {
        workflowTaskSlotSupplier.resume();
        activityTaskSlotSupplier.resume();
        localActivitySlotSupplier.resume();
        nexusSlotSupplier.resume();
        logger.info("SuspendableWorkerTuner: %s resumed by %s" + callerName);
    }

    /**
     * Sum of workload count in supplier.
     */
    public int getWorkloadCount() {
        return workflowTaskSlotSupplier.getWorkloadCount()
                + activityTaskSlotSupplier.getWorkloadCount()
                + localActivitySlotSupplier.getWorkloadCount()
                + nexusSlotSupplier.getWorkloadCount();
    }

    /**
     * Sum of used slots in supplier.
     */
    public int getUsedSlotsCount() {
        return workflowTaskSlotSupplier.getUsedSlots()
                + activityTaskSlotSupplier.getUsedSlots()
                + localActivitySlotSupplier.getUsedSlots()
                + nexusSlotSupplier.getUsedSlots();
    }

    /**
     * Sum of issued slots in supplier.
     */
    public int getIssuedSlotsCount() {
        return workflowTaskSlotSupplier.getIssuedSlots()
                + activityTaskSlotSupplier.getIssuedSlots()
                + localActivitySlotSupplier.getIssuedSlots()
                + nexusSlotSupplier.getIssuedSlots();
    }

    /**
     * @return true, when all slot suppliers are suspended, otherwise false.
     */
    public boolean isSuspended() {
        return workflowTaskSlotSupplier.isSuspended()
                && activityTaskSlotSupplier.isSuspended()
                && localActivitySlotSupplier.isSuspended()
                && nexusSlotSupplier.isSuspended();
    }

    @Override
    public SuspendableSlotSupplier<WorkflowSlotInfo> getWorkflowTaskSlotSupplier() {
        return workflowTaskSlotSupplier;
    }

    @Override
    public SuspendableSlotSupplier<ActivitySlotInfo> getActivityTaskSlotSupplier() {
        return activityTaskSlotSupplier;
    }

    @Override
    public SuspendableSlotSupplier<LocalActivitySlotInfo> getLocalActivitySlotSupplier() {
        return localActivitySlotSupplier;
    }

    @Override
    public SuspendableSlotSupplier<NexusSlotInfo> getNexusSlotSupplier() {
        return nexusSlotSupplier;
    }


    @Override
    public String toString() {
        return "{workflowTaskSlotSupplier=%s, activityTaskSlotSupplier=%s, localActivitySlotSupplier=%s, nexusSlotSupplier=%s}"
                .formatted(
                        workflowTaskSlotSupplier,
                        activityTaskSlotSupplier,
                        localActivitySlotSupplier,
                        nexusSlotSupplier);
    }
}
