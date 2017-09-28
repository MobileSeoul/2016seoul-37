package com.example.jjw.mydemo.lib.inf;

import com.example.jjw.mydemo.logger.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by JJW on 2016-10-17.
 */
public final class RefreshEventHandler {

    private static final int MAX_THREAD_POOL = 5;

    //private static final Log LOGGER = LoggerFactory.getLogger(EventHandler.class);

    private static List<RefreshEventListener> listeners = new CopyOnWriteArrayList<RefreshEventListener>();

    private static synchronized List<RefreshEventListener> getListeners() {
        return listeners;
    }

    public static synchronized void addListener(RefreshEventListener eventListener) {
        if (getListeners().indexOf(eventListener) == -1) {
            listeners.add(eventListener);
        }
    }

    public static synchronized void removeListener(RefreshEventListener eventListener) {
        if (getListeners().indexOf(eventListener) != -1) {
            listeners.remove(eventListener);
        }
    }

    public static synchronized void callEvent(final Class<?> caller, String event) {
        callEvent(caller, event, true);
    }

    public static synchronized void callEvent(final Class<?> caller, String event, boolean doAsynch) {
        if (doAsynch) {
            callEventByAsynch(caller, event);
        } else {
            callEventBySynch(caller, event);
        }
    }

    private static synchronized void callEventByAsynch(final Class<?> caller, final String event) {
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREAD_POOL);

        //LOGGER.info("[Event occur : <{}> by <{}>]", new Object[] { event, caller.getName() });
        System.out.println("[Event occur : <{}> " + event + " by <{ " + caller.getName() + " }>]");
        for (final RefreshEventListener listener : listeners) {
            executorService.execute(new Runnable() {
                public void run() {
                    if (listener.getClass().getName().equals(caller.getName())) {
                        //LOGGER.info("[Event skip : <{}> by self <{}>]", new Object[] { event, caller.getName() });
                        System.out.println("[Event skip : <{" + event + " }> by <{ " + caller.getName() + " }>]");
                    } else {
                       // LOGGER.info("[Event catch : <{}> by <{}>]", new Object[] { event, listener.getClass().getName() });
                        System.out.println("[Event catch : <{" + event + " }> by <{ " + caller.getName() + " }>]");
                        listener.onRefreshEvent(event);
                    }
                }
            });
        }

        executorService.shutdown();
    }

    private static synchronized void callEventBySynch(final Class<?> caller, final String event) {
       // LOGGER.info("[Event occur : <{}> by <{}>]", new Object[] { event, caller.getName() });
        System.out.println("[Event occur : <{" + event + " }> by <{ " + caller.getName() + " }>]");
        for (final RefreshEventListener listener : listeners) {
            if (listener.getClass().getName().equals(caller.getName())) {
               // LOGGER.info("[Event skip : <{}> by self <{}>]", new Object[] { event, caller.getName() });
                System.out.println("[Event skip : <{" + event + " }> by <{ " + caller.getName() + " }>]");
            } else {
                //LOGGER.info("[Event catch : <{}> by <{}>]", new Object[] { event, listener.getClass().getName() });
                System.out.println("[Event catch : <{" + event + " }> by <{ " + caller.getName() + " }>]");

                listener.onRefreshEvent(event);
            }
        }
    }
}
