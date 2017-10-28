package test;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class T_Future {
	public static void main(String[] args) throws Throwable {
		FutureTask ft = new FutureTask(new Tcall());
		long start = System.currentTimeMillis();
		Thread t = new Thread(ft);
		t.start();
		try {
			ft.get(1500, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ft.cancel(true);
		//*************************
		t.interrupt();
		t.stop();
		//*************************
		System.out.println(System.currentTimeMillis() - start);
	}
	
	public static class Tcall implements Callable<Object> {
		@Override
		public Object call() throws Exception {
//			synchronized(this) {
//				this.wait();
//			}
			boolean flag = true;
			int i = 0;
			while(flag) {
				i++;
			}
			return null;
		}
	}
}
