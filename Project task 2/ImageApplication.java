package project2;

import java.util.concurrent.atomic.AtomicIntegerArray;

 
public class ImageApplication{
	
	public static int level = 256;
	public static int num_loops = 3;
	
	public static void main(String[] args) {
		String fileName1="C:/Users/Salman/Desktop/ECTE 331/Rain_Tree.jpg/";
		String fileName2="C:/Users/Salman/Desktop/ECTE 331/Wr.jpg/";  
		
		int [] numOfThreads = {1,2,6,10};
		
		colourImage input_img= new colourImage();
        imageReadWrite.readJpgImage(fileName1, input_img);
		
		// Single-threaded implementation
		System.out.println("Single-threaded implementation:");
		long totalTime = 0;
		for (int i = 0; i < num_loops; i++)
		{
			colourImage output = new colourImage();
			output.width = input_img.width;
			output.height = input_img.height;
			output.pixels = new short [output.height][output.width][3];
			
			long startTime = System.nanoTime();
			SingleHistogramEqualization(input_img, output);
			long endTime = System.nanoTime();
			
			totalTime += (endTime - startTime);
			
			if (i == 0) {
				imageReadWrite.writeJpgImage(output, fileName2);
			}
		}
		double avgTime = (totalTime / num_loops) / 1000000.0;
		System.out.println("Average time: " + avgTime + " ms");
		
		// Multi-threaded implementation - Shared Histogram
		System.out.println("\nMulti-threaded implementation - Shared Histogram:");
		for (int numThread : numOfThreads) {
			if (numThread == 1) continue;
			totalTime = 0;
			for (int i = 0; i < num_loops; i++)
			{
				colourImage output = new colourImage();
				output.width = input_img.width;
				output.height = input_img.height;
				output.pixels = new short [output.height][output.width][3];
				
				long startTime = System.nanoTime();
				MultiThreadHistogramEqualizationShared(input_img, output, numThread);
				long endTime = System.nanoTime();
				
				totalTime += (endTime - startTime);
			}
			avgTime = (totalTime / num_loops) / 1000000.0;
			System.out.println("Threads: " + numThread + ", Average time: " + avgTime + " ms");
		}
		
		// Multi-threaded implementation - Sub-histograms
		System.out.println("\nMulti-threaded implementation - Sub-histograms:");
		for (int numThread : numOfThreads) {
			if (numThread == 1) continue;
			totalTime = 0;
			for (int i = 0; i < num_loops; i++)
			{
				colourImage output = new colourImage();
				output.width = input_img.width;
				output.height = input_img.height;
				output.pixels = new short [output.height][output.width][3];
				
				long startTime = System.nanoTime();
				MultiThreadHistogramEqualizationSub(input_img, output, numThread);
				long endTime = System.nanoTime();
				
				totalTime += (endTime - startTime);
			}
			avgTime = (totalTime / num_loops) / 1000000.0;
			System.out.println("Threads: " + numThread + ", Average time: " + avgTime + " ms");
		}
            
		// demo reshaping a 4*4 matrix into 16 1-D array
		int width=4, height=4;
		short mat [][]=new short[height][width];
		  for(int i=0; i<height; i++)
			  for(int j=0;j<width;j++)
				  mat[i][j]=(short)(i*width+j);	
	     
	     short vect []=new short[height*width];
	     matManipulation.mat2Vect(mat, width, height, vect);
	   
	     for(int i=0; i<height; i++) {	    
			  for(int j=0;j<width;j++)
				  System.out.printf("%3d ", mat[i][j]);
			  System.out.println();
	     }
	     
	     for(int i=0; i<width*height; i++) 	    
				  System.out.printf("%d ", vect[i]);
	
	} // main	
	
	public static void SingleHistogramEqualization(colourImage input, colourImage output)
	{
		int size = input.height * input.width;
		
		for (int color = 0; color < 3; color++)
		{
			int [] histogram = new int[level];
			
			for (int i = 0; i < input.height; i++)
			{
				for (int j = 0; j < input.width; j++)
				{
					histogram[input.pixels[i][j][color]]++;
				}
			}
			
			int [] cumulativeHist = new int[level];
			cumulativeHist[0] = histogram[0];
			
			for (int i = 1; i < level; i++)
			{
				cumulativeHist[i] = cumulativeHist[i-1] + histogram[i];
			}
			
			for (int i = 0; i < level; i++)
			{
				cumulativeHist[i] = (cumulativeHist[i] * (level-1)) / size;
			}
			
			for (int i = 0; i < input.height; i++)
			{
				for (int j = 0; j < input.width; j++)
				{
					output.pixels[i][j][color] = (short)cumulativeHist[input.pixels[i][j][color]];
				}
			}
		}
	}
	
	public static void MultiThreadHistogramEqualizationShared(colourImage input, colourImage output, int numOfThreads)
	{
		int size = input.height * input.width;
		Thread[] threads = new Thread[numOfThreads];
		
		for (int color = 0; color < 3; color++)
		{
			AtomicIntegerArray sharedHistogram = new AtomicIntegerArray(level);
			
			for (int t = 0; t < numOfThreads; t++)
			{
				final int threadId = t;
				final int currentColor = color;
				threads[t] = new Thread(() -> {
					int startRow = threadId * input.height / numOfThreads;
					int endRow = (threadId + 1) * input.height / numOfThreads;
					
					for (int i = startRow; i < endRow; i++)
					{
						for (int j = 0; j < input.width; j++)
						{
							sharedHistogram.incrementAndGet(input.pixels[i][j][currentColor]);
						}
					}
				});
				threads[t].start();
			}
			
			for (int t = 0; t < numOfThreads; t++)
			{
				try {
					threads[t].join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			int[] cumulativeHist = new int[level];
			cumulativeHist[0] = sharedHistogram.get(0);
			
			for (int i = 1; i < level; i++)
			{
				cumulativeHist[i] = cumulativeHist[i-1] + sharedHistogram.get(i);
			}
			
			for (int i = 0; i < level; i++)
			{
				cumulativeHist[i] = (cumulativeHist[i] * (level-1)) / size;
			}
			
			for (int t = 0; t < numOfThreads; t++)
			{
				final int threadId = t;
				final int currentColor = color;
				final int[] finalCumulativeHist = cumulativeHist;
				threads[t] = new Thread(() -> {
					int startRow = threadId * input.height / numOfThreads;
					int endRow = (threadId + 1) * input.height / numOfThreads;
					
					for (int i = startRow; i < endRow; i++)
					{
						for (int j = 0; j < input.width; j++)
						{
							output.pixels[i][j][currentColor] = (short)finalCumulativeHist[input.pixels[i][j][currentColor]];
						}
					}
				});
				threads[t].start();
			}
			
			for (int t = 0; t < numOfThreads; t++)
			{
				try {
					threads[t].join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void MultiThreadHistogramEqualizationSub(colourImage input, colourImage output, int numOfThreads)
	{
		int size = input.height * input.width;
		Thread[] threads = new Thread[numOfThreads];
		
		for (int color = 0; color < 3; color++)
		{
			int[][] subHistograms = new int[numOfThreads][level];
			
			for (int t = 0; t < numOfThreads; t++)
			{
				final int threadId = t;
				final int currentColor = color;
				threads[t] = new Thread(() -> {
					int startRow = threadId * input.height / numOfThreads;
					int endRow = (threadId + 1) * input.height / numOfThreads;
					
					for (int i = startRow; i < endRow; i++)
					{
						for (int j = 0; j < input.width; j++)
						{
							subHistograms[threadId][input.pixels[i][j][currentColor]]++;
						}
					}
				});
				threads[t].start();
			}
			
			for (int t = 0; t < numOfThreads; t++)
			{
				try {
					threads[t].join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			int[] histogram = new int[level];
			for (int i = 0; i < level; i++)
			{
				for (int t = 0; t < numOfThreads; t++)
				{
					histogram[i] += subHistograms[t][i];
				}
			}
			
			int[] cumulativeHist = new int[level];
			cumulativeHist[0] = histogram[0];
			
			for (int i = 1; i < level; i++)
			{
				cumulativeHist[i] = cumulativeHist[i-1] + histogram[i];
			}
			
			for (int i = 0; i < level; i++)
			{
				cumulativeHist[i] = (cumulativeHist[i] * (level-1)) / size;
			}
			
			for (int t = 0; t < numOfThreads; t++)
			{
				final int threadId = t;
				final int currentColor = color;
				final int[] finalCumulativeHist = cumulativeHist;
				threads[t] = new Thread(() -> {
					int startRow = threadId * input.height / numOfThreads;
					int endRow = (threadId + 1) * input.height / numOfThreads;
					
					for (int i = startRow; i < endRow; i++)
					{
						for (int j = 0; j < input.width; j++)
						{
							output.pixels[i][j][currentColor] = (short)finalCumulativeHist[input.pixels[i][j][currentColor]];
						}
					}
				});
				threads[t].start();
			}
			
			for (int t = 0; t < numOfThreads; t++)
			{
				try {
					threads[t].join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
		            
}
