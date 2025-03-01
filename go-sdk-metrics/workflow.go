package sample_5888

import (
	"context"
	"time"

	"go.temporal.io/sdk/activity"
	"go.temporal.io/sdk/workflow"
)

func Workflow(ctx workflow.Context) error {
	ao := workflow.ActivityOptions{
		StartToCloseTimeout: 10 * time.Second,
	}
	ctx = workflow.WithActivityOptions(ctx, ao)
	lao := workflow.LocalActivityOptions{
		StartToCloseTimeout: 10 * time.Second,
	}
	ctx = workflow.WithLocalActivityOptions(ctx, lao)

	logger := workflow.GetLogger(ctx)
	logger.Info("Metrics workflow started.")

	scheduledTimeNanos := workflow.Now(ctx).UnixNano()
	_ = workflow.Sleep(ctx, 5*time.Second)

	_ = workflow.ExecuteLocalActivity(ctx, Activity, scheduledTimeNanos).Get(ctx, nil)

	_ = workflow.ExecuteActivity(ctx, Activity, scheduledTimeNanos).Get(ctx, nil)
	_ = workflow.ExecuteActivity(ctx, Activity, scheduledTimeNanos).Get(ctx, nil)

	logger.Info("Metrics workflow completed.")
	return nil
}

func Activity(ctx context.Context, scheduledTimeNanos int64) error {
	logger := activity.GetLogger(ctx)

	var err error
	metricsHandler := activity.GetMetricsHandler(ctx)
	metricsHandler = recordActivityStart(metricsHandler, "metrics.Activity", scheduledTimeNanos)
	startTime := time.Now()
	defer func() { recordActivityEnd(metricsHandler, startTime, err) }()

	time.Sleep(time.Second)
	logger.Info("Metrics reported.")
	return err
}
