package com.temporal.worker_tuner;

import io.temporal.worker.tuning.*;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class AcceptTaskLimitedTimeSlotSupplier<SI extends SlotInfo> implements SlotSupplier<SI> {


    private boolean canReserveSlot;

    public AcceptTaskLimitedTimeSlotSupplier() {
        //create a timer that completes after 1 second and set canReserveSlot to true
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1_000); //10 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            canReserveSlot = true;
        });


    }


    @Override
    public SlotSupplierFuture reserveSlot(SlotReserveContext<SI> ctx) throws Exception {

        if (canReserveSlot) {
            return new SlotSupplierFuture() {
                @Nullable
                @Override
                public SlotPermit abortReservation() {
                    return null;
                }
            };
        }



        System.out.println(new Date().toGMTString() + " ---> AcceptTaskLimitedTimeSlotSupplier: waiting ...");

        Thread.sleep(300_000);

        //this code should never be reached
        return null;

    }

    @Override
    public Optional<SlotPermit> tryReserveSlot(SlotReserveContext<SI> ctx) {

        if (canReserveSlot) {
            return Optional.of(new SlotPermit());
        }

        return Optional.empty();

    }

    @Override
    public void markSlotUsed(SlotMarkUsedContext<SI> ctx) {

    }

    @Override
    public void releaseSlot(SlotReleaseContext<SI> ctx) {

    }


}
