/*
 * This file is distributed as part of the MariaDB Manager.  It is free
 * software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * version 2.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Copyright 2012-2014 SkySQL Corporation Ab
 */

package com.skysql.manager;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A factory for creating Executor objects. Used for main polling (UI refresh) and short polling (running command)
 */
public class ExecutorFactory {

	private static final int NUM_THREADS = 24;
	private static ScheduledExecutorService fScheduler;
	private static final boolean MAY_INTERRUPT_IF_RUNNING = true;
	private static int runningTimers = 0;

	/**
	 * Adds the timer.
	 *
	 * @param timerTask the timer task
	 * @param fDelayBetweenRuns the delay between runs
	 * @return the scheduled future
	 */
	public static ScheduledFuture<?> addTimer(Runnable timerTask, long fDelayBetweenRuns) {

		if (runningTimers == 0) {
			fScheduler = Executors.newScheduledThreadPool(NUM_THREADS);
			ManagerUI.log("timer startup");
		}

		ScheduledFuture<?> newTimerHandle = fScheduler.scheduleWithFixedDelay(timerTask, fDelayBetweenRuns, fDelayBetweenRuns, TimeUnit.SECONDS);
		if (newTimerHandle != null) {
			runningTimers++;
			ManagerUI.log("timer added - " + runningTimers);
		} else {
			ManagerUI.log("cannot get new timer handle");
		}

		return newTimerHandle;

	}

	/**
	 * Removes the timer.
	 *
	 * @param timerHandle the timer handle
	 */
	public static void removeTimer(ScheduledFuture<?> timerHandle) {
		if (timerHandle != null) {
			timerHandle.cancel(MAY_INTERRUPT_IF_RUNNING);
			ManagerUI.log("timer cancel: " + runningTimers);
			if (runningTimers > 0) {
				runningTimers--;
			}
		}

		if (runningTimers == 0 && fScheduler != null) {
			List<Runnable> pendingTasks = fScheduler.shutdownNow();
			fScheduler = null;
			ManagerUI.log("timer shutdown - pending: " + pendingTasks.size());
		}
	}

	/**
	 * Adds the one timer.
	 *
	 * @param timerTask the timer task
	 * @param fDelay the delay
	 * @return the scheduled future
	 */
	public static ScheduledFuture<?> addOneTimer(Runnable timerTask, long fDelay) {

		if (runningTimers == 0) {
			fScheduler = Executors.newScheduledThreadPool(NUM_THREADS);
			ManagerUI.log("timer startup");
		}

		ScheduledFuture<?> newTimerHandle = fScheduler.schedule(timerTask, fDelay, TimeUnit.SECONDS);
		if (newTimerHandle != null) {
			runningTimers++;
			ManagerUI.log("timer added - " + runningTimers);
		} else {
			ManagerUI.log("cannot get new timer handle");
		}

		return newTimerHandle;

	}

}
