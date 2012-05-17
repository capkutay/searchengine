import java.util.LinkedList;
import org.apache.log4j.*;

/* Work Queue Implementation
 *
 * Original Source:
 * http://www.ibm.com/developerworks/library/j-jtp0730.html
 *
 * Modifications:
 * -- Specified type for LinkedList
 * -- Added shutdown capability
 * -- Added log4j debug messages
 */

public class WorkQueue {
    private final int nThreads;
    private final PoolWorker[] threads;
    private final LinkedList<Runnable> queue;
    
    // Example of using a volatile variable
    private volatile boolean shutdown;
    
    // You can remove logging after you understand everything
	private static Logger log = Logger.getRootLogger();

    public WorkQueue(int nThreads) {
        this.nThreads = nThreads;
        queue = new LinkedList<Runnable>();
        threads = new PoolWorker[this.nThreads];

        for (int i=0; i<nThreads; i++) {
            threads[i] = new PoolWorker();
            threads[i].start();
        }
        
        shutdown = false;
        
        log.debug("WorkQueue initialized with " + nThreads + " threads.");
    }

    // Make sure you understand why we didn't make this method synchronized
    public void execute(Runnable r) {
        synchronized(queue) {
            queue.addLast(r);
            queue.notify();
        }
    }
    
    // This will let threads finish any work that is still in queue
    public void shutdown() {
        log.debug("Attempting shutdown of all threads.");
        shutdown = true;
        
        synchronized(queue) {
        		queue.notifyAll();
        }
    }

    private class PoolWorker extends Thread {
        public void run() {
            Runnable r = null;

            while (true) {
                synchronized(queue) {
                    while (queue.isEmpty() && !shutdown) {
                        try {
                            log.debug("Waiting for work...");
                            queue.wait();
                        }
                        catch (InterruptedException ex) {
                            log.debug("Interrupted while waiting for work.");
                        }
                    }
                    
                    // Check why we exited inner while
                    if(shutdown) {
                        log.debug("Shutdown detected.");
                        break;
                    }
                    else {
                        assert !queue.isEmpty();
                        r = queue.removeFirst();
                    }
                }
                
                // If we don't catch RuntimeException, 
                // the pool could leak threads
                try {
                    log.debug("Found work!");
                    r.run();
                }
                catch (RuntimeException ex) {
                    log.error("Encountered exception running work.", ex);
                }
            }
            
            log.debug("Thread terminating.");
        }
    }
}
