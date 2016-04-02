/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package squaresumimplementation;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.*;
interface SquareSum {       

 
long getSquareSum(int[] values, int numberOfThreads);    
}

class WorkerThread implements Runnable {
     
   private int begin;
   private int end;
   private Phaser phsr;
   SquareSumImplementation implsum;
     
    public WorkerThread(int begin, int end, SquareSumImplementation implsum, Phaser phsr){
        this.begin = begin;
        this.end = end;
        this.implsum = implsum;
        this.phsr = phsr;
        
    }
 
    @Override
    public void run() {
        implsum.addValueToSum(implsum.countSquareSum(begin, end));
    }
 
    private void processCommand() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
 
    
}
class Sum implements Callable<Long> {  
    int []values;
    private Phaser phsr;
 
  Sum(int []values,Phaser phsr) 
  {
    
      this.values = values;
      this.phsr = phsr;
  } 
 
  public Long call() { 
      long sum= 0;
   System.out.println("Phase assingment");   
    phsr.arriveAndAwaitAdvance();
    System.out.println("Phase counting sum");  
 for(int i = 0; i < values.length; ++i)
         sum +=Math.pow(this.values[i], 2); 
 phsr.arriveAndAwaitAdvance();
 System.out.println("Phase return sum");  
    return sum; 
  } 
} 
public class SquareSumImplementation implements SquareSum  {
    
    private int values[];
    private AtomicLong sum;
    private Phaser phsr;
    
    
    SquareSumImplementation(int []values,Phaser phsr)
    {
      this.values = values;  
      sum = new AtomicLong(0);
      this.phsr= phsr;
    }
    
      SquareSumImplementation()
    {
      this.values = null;  
      sum = new AtomicLong(0);
    }

 public long countSquareSum(int begin, int end)
 {
     
     long suma = 0;
  System.out.println("Phase assingment sum!");   
   for(int i = begin; i < end; ++i)
   suma +=Math.pow(values[i], 2);
   phsr.arriveAndAwaitAdvance();
    processCommand(); 
   System.out.println("Phase counting sum in threads!");   
   phsr.arriveAndAwaitAdvance();
    processCommand(); 
    System.out.println("Phase return sum in thread and adding it to final sum!"); 
     return suma;
     
 }
 
  public void addValueToSum(long value)
 {
      this.sum.addAndGet(value);
  }
  
    private void processCommand() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
 

  
    @Override
    public  long getSquareSum(int[] values, int numberOfThreads)
  {
      Phaser phsr = new Phaser(1);  
    SquareSumImplementation sumImpl = new SquareSumImplementation(values,phsr);
      
      
        int begin = 0;
        final int arraylength = values.length;
        int step = (int)Math.floor(Math.sqrt(values.length));
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        int end = step;
       while(end < arraylength)
       {
           
           Runnable worker = new WorkerThread(begin, end, sumImpl,phsr);
            executor.execute(worker);
           begin = end;
           end +=step;
       }
       end = arraylength;
       phsr.arriveAndAwaitAdvance();
       Runnable worker = new WorkerThread(begin, end,sumImpl, phsr);
       executor.execute(worker);
       
     
     
       executor.shutdown();
        while (!executor.isTerminated()) {
        }
       phsr.arriveAndDeregister();
        System.out.println("Ending all phases and show result of sum");
        System.out.println("Finished all threads");
        return sumImpl.sum.get();
  }


    public static void main(String[] args) {
        int massiv[] = new  int[100489];
        for(int i = 0; i < 100489; ++i)
            massiv[i] = i;
            
       
        int mas[] = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
        SquareSumImplementation sumImpl = new SquareSumImplementation();
      //  int step = (int)Math.floor(Math.sqrt(mas.length));
        
        int step = (int)Math.floor(Math.sqrt(massiv.length));
        
        System.out.println("sum (Interface Runnable) = "+  sumImpl.getSquareSum(massiv, step));  
        
        
        
        //Вирішення цієї самої задачі за допомогою інтерфейсів Callable і Future
        ExecutorService executor = Executors.newFixedThreadPool(step);
        Phaser phsr = new Phaser(1);
        
        
        Future<Long> f;
        f = executor.submit(new Sum(massiv,phsr));
        try { 
            
       phsr.arriveAndDeregister(); 
        System.out.println("sum (Interface Future and Callable) = "+f.get()); 
    
    } catch (InterruptedException exc) { 
      System.out.println(exc); 
    } 
    catch (ExecutionException exc) { 
      System.out.println(exc); 
    } 
 
    executor.shutdown(); 
    System.out.println("Done"); 
    }
}
