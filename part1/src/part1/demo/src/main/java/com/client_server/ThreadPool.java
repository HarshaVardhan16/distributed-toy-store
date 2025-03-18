package com.client_server;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;

public class ThreadPool {
    private final List<WorkerThread> threadList = new LinkedList<>();
    private final Queue<Runnable> taskList = new LinkedList<>();
    private boolean isRunning = true;
    private int noOfThreads;

    public ThreadPool(int noOfThreads) { //Initializes the threadpool with noOfThreads.
        this.noOfThreads = noOfThreads;
        String threadName = "Thread";
        for(int i = 0; i< noOfThreads;i++){
            WorkerThread thread = new WorkerThread(this);
            thread.setName(threadName + Integer.toString(i));
            threadList.add(thread); //Adding a thread to the threadpool.
            thread.start();
        }
    }
    
    public synchronized void addTask(Runnable runnable) throws Exception{ //Adds Task to the taskList queue and notifies all threads.
        if(isRunning){
            this.taskList.add(runnable);
            this.notifyAll();
        }
        else{
            throw new Exception("Thread Pool is not running anymore!");
        }
    }

    public synchronized Runnable getTask() throws InterruptedException{ // Gets first Task in the queue, otherwise makes the threads wait.
        while(this.taskList.isEmpty()){
            this.wait();
        }
        return this.taskList.remove();
    }

    public synchronized void stop() { //Stop the threadpool
        this.isRunning = false;
        for (WorkerThread thread : threadList) {
            thread.Stop();
        }
    }

}

class WorkerThread extends Thread{

    private ThreadPool threadPool;
    private boolean isRunning = true;

    public WorkerThread(ThreadPool threadPool){
        this.threadPool = threadPool;
    }
    
    @Override
    public void run(){
        System.out.println(this.getName() + " has Started");
        try {
            while(isRunning()){ //Checks if the thread is still running 
                Runnable task = threadPool.getTask();
                //System.out.println("Task executed by :" + this.getName());
                task.run();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public synchronized void Stop() {
        isRunning = false;
        this.interrupt(); // Break pool thread out of dequeue() call.
    }

    public synchronized boolean isRunning() { //Returns true if a thread is still running
        return isRunning; 
    }    
}
