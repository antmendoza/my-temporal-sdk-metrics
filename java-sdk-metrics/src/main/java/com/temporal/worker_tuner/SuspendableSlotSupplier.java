package com.temporal.worker_tuner;

import com.google.common.annotations.VisibleForTesting;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.tuning.SlotInfo;
import io.temporal.worker.tuning.SlotMarkUsedContext;
import io.temporal.worker.tuning.SlotPermit;
import io.temporal.worker.tuning.SlotReleaseContext;
import io.temporal.worker.tuning.SlotReleaseReason;
import io.temporal.worker.tuning.SlotReserveContext;
import io.temporal.worker.tuning.SlotSupplier;
import io.temporal.worker.tuning.SlotSupplierFuture;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SuspendableSlotSupplier<SI extends SlotInfo> implements SlotSupplier<SI> {
    private final SlotSupplier<SI> delegate;
    /**
     * Number of issued slots, once we suspend, we won't be issuing any more slots.
     */
    private final AtomicInteger issuedSlotCount = new AtomicInteger(0);

    /**
     * Issued slots doesn't necessary will be used, as if polling doesn't return anything to work on.
     * So number of used slots is different from issued slots.
     */
    private final Map<SlotPermit, SI> usedSlots = new ConcurrentHashMap<>();
    private final BaseLogger logger;

    private volatile boolean isSuspended = false;

    public SuspendableSlotSupplier(BaseLogger logger, SlotSupplier<SI> delegate) {
        this.logger = logger;
        this.delegate = delegate;
    }

    @Override
    public SlotSupplierFuture reserveSlot(SlotReserveContext<SI> ctx) throws Exception {

        var slotFuture = delegate.reserveSlot(ctx);

        var suspendableSlotFuture = slotFuture.thenApply(permit -> {
            sleepUntilResumed();
            issuedSlotCount.getAndIncrement();
            return permit;
        });
        SlotSupplierFuture slotSupplierFuture = SlotSupplierFuture.fromCompletableFuture(suspendableSlotFuture, slotFuture::abortReservation);
        return slotSupplierFuture;
    }

    @Override
    public Optional<SlotPermit> tryReserveSlot(SlotReserveContext<SI> ctx) {
        if (isSuspended) {
            return Optional.empty();
        }
        var slotPermit = delegate.tryReserveSlot(ctx);
        if (slotPermit.isPresent()) {
            if (isSuspended) {
                // immediately release the slot as we already suspended
                releaseSlot(new UnusedSlotReleaseContext(slotPermit.get()));
                return Optional.empty();
            } else {
                issuedSlotCount.getAndIncrement();
                return slotPermit;
            }
        }

        return slotPermit;
    }

    @Override
    public void markSlotUsed(SlotMarkUsedContext<SI> ctx) {
        usedSlots.put(ctx.getSlotPermit(), ctx.getSlotInfo());
        delegate.markSlotUsed(ctx);
    }

    @Override
    public void releaseSlot(SlotReleaseContext<SI> ctx) {
        // We have to update class state before calling delegate.releaseSlot(ctx)
        // as delegate.releaseSlot(ctx) can call back into this class for reserveSlot method
        issuedSlotCount.getAndDecrement();
        usedSlots.remove(ctx.getSlotPermit());

        delegate.releaseSlot(ctx);
    }

    @Override
    public Optional<Integer> getMaximumSlots() {
        return delegate.getMaximumSlots();
    }

    public boolean isSuspended() {
        return isSuspended;
    }

    public void suspend() {
        System.out.println("suspend SuspendableSlotSupplier.isSuspended=" + isSuspended);
        isSuspended = true;
    }

    public void resume() {
        System.out.println("resume SuspendableSlotSupplier.isSuspended=" + isSuspended);
        isSuspended = false;
    }

    /**
     * We want to keep use used slot count which represents actual running workload when worker is not suspended.
     * Once worker is suspended, we want to keep track of issued slot count which represents the number of slots that are issued
     * and can be used, so we should wait for them to finish.
     * <br/>
     *
     * {@link #reserveSlot} and {@link #tryReserveSlot} doesn't return a slot if the worker is suspended,
     * so we don't need to worry about issuing more slots.
     *
     * @return {@link #issuedSlotCount}, when {@link #isSuspended} = true,
     * otherwise {@link #usedSlots::size()}
     */
    int getWorkloadCount() {
        if (isSuspended) {
            return issuedSlotCount.get();
        } else {
            return usedSlots.size();
        }
    }

    int getIssuedSlots() {
        return issuedSlotCount.get();
    }

    int getUsedSlots() {
        return usedSlots.size();
    }

    @VisibleForTesting
    void sleepUntilResumed() {
        var logEveryMillis = Duration.ofMinutes(1).toMillis();
        var nextLogAfter = logEveryMillis;
        var startedAtMillis = System.currentTimeMillis();
        while (isSuspended) {
            ThreadingUtils.sleepThrowOnInterrupt(1_000);
            var currentTimeMillis = System.currentTimeMillis();
            var sleptMillis = currentTimeMillis - startedAtMillis;
            if (sleptMillis > nextLogAfter) {
                logger.info("Slot supplier is suspended since %s millis, waiting to resume..."+ sleptMillis);
                nextLogAfter += logEveryMillis;
            }
        }
    }

    @Override
    public String toString() {
        return "{isSuspended=%s, issuedSlotCount=%s, usedSlotCount=%s}"
                .formatted(isSuspended, issuedSlotCount, usedSlots.size());
    }

    class UnusedSlotReleaseContext implements SlotReleaseContext<SI> {
        private final SlotPermit slotPermit;

        UnusedSlotReleaseContext(SlotPermit slotPermit) {
            this.slotPermit = slotPermit;
        }

        @Override
        public SlotReleaseReason getSlotReleaseReason() {
            return SlotReleaseReason.neverUsed();
        }

        @Override
        public SlotPermit getSlotPermit() {
            return slotPermit;
        }

        @Override
        public SI getSlotInfo() {
            return null;
        }
    }
}
