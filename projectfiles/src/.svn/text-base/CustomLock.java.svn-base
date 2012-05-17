
public class CustomLock {
	
	int readers;
	int writers;
	//allows threads to "acquire" the read lock if there are no writers
	public synchronized void acquireReadLock(){
		while(writers > 0){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		readers++;
	}
	
	public synchronized void releaseReadLock(){
		readers--;
		if (readers == 0) {
			notifyAll();			
		}
	}
	//only allows threads to "acquire" the write lock if there are no readers or writers
	public synchronized void acquireWriteLock(){
		while(writers > 0 || readers > 0){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		writers++;
	}
	
	//"returns" the write lock
	public synchronized void releaseWriteLock(){
		writers--;
		notifyAll();
	}
}