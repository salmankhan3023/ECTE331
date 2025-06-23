package project2;

import java.util.concurrent.atomic.AtomicIntegerArray;

public class ImageApplication{
    
    public static int level = 256;
    public static int num_loops = 3;
    
    public static void main(String[] args) {
        // Fix file paths - remove trailing slashes
        String fileName1="C:/Users/Salman/Desktop/ECTE 331/Rain_Tree.jpg";
        String fileName2="C:/Users/Salman/Desktop/ECTE 331/Output.jpg";  
        
        int [] numOfThreads = {1,2,6,10};
        
        colourImage input_img= new colourImage();
        imageReadWrite.readJpgImage(fileName1, input_img);
        
        System.out.println("Image dimensions: " + input_img.width + "x" + input_img.height);
        
        // Single-threaded implementation
        System.out.println("\nSingle-threaded implementation:");
        long totalTime = 0;
        
        for (int i = 0; i < num_loops; i++) {
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
                System.out.println("Single-threaded output saved to: " + fileName2);
            }
        }
        double avgTime = (totalTime / num_loops) / 1000000.0;
        System.out.println("Average time: " + avgTime + " ms");
        
        // Multi-threaded implementation - Shared Histogram (Row-based)
        System.out.println("\nMulti-threaded implementation - Shared Histogram (Row-based):");
        for (int numThread : numOfThreads) {
            totalTime = 0;
            
            for (int i = 0; i < num_loops; i++) {
                colourImage output = new colourImage();
                output.width = input_img.width;
                output.height = input_img.height;
                output.pixels = new short [output.height][output.width][3];
                
                long startTime = System.nanoTime();
                if (numThread == 1) {
                    SingleHistogramEqualization(input_img, output);
                } else {
                    MultiThreadHistogramEqualizationSharedRows(input_img, output, numThread);
                }
                long endTime = System.nanoTime();
                
                totalTime += (endTime - startTime);
                
                // Save first iteration output
                if (i == 0) {
                    String outputFileName = "C:/Users/Salman/Desktop/ECTE 331/SharedRows_" + numThread + "threads.jpg";
                    imageReadWrite.writeJpgImage(output, outputFileName);
                    System.out.println("Saved: SharedRows_" + numThread + "threads.jpg");
                }
            }
            avgTime = (totalTime / num_loops) / 1000000.0;
            System.out.println("Threads: " + numThread + ", Average time: " + avgTime + " ms");
        }
        
        // Multi-threaded implementation - Shared Histogram (Column-based)
        System.out.println("\nMulti-threaded implementation - Shared Histogram (Column-based):");
        for (int numThread : numOfThreads) {
            totalTime = 0;
            for (int i = 0; i < num_loops; i++) {
                colourImage output = new colourImage();
                output.width = input_img.width;
                output.height = input_img.height;
                output.pixels = new short [output.height][output.width][3];
                
                long startTime = System.nanoTime();
                if (numThread == 1) {
                    SingleHistogramEqualization(input_img, output);
                } else {
                    MultiThreadHistogramEqualizationSharedCols(input_img, output, numThread);
                }
                long endTime = System.nanoTime();
                
                totalTime += (endTime - startTime);
                
                // Save first iteration output
                if (i == 0) {
                    String outputFileName = "C:/Users/Salman/Desktop/ECTE 331/SharedCols_" + numThread + "threads.jpg";
                    imageReadWrite.writeJpgImage(output, outputFileName);
                    System.out.println("Saved: SharedCols_" + numThread + "threads.jpg");
                }
            }
            avgTime = (totalTime / num_loops) / 1000000.0;
            System.out.println("Threads: " + numThread + ", Average time: " + avgTime + " ms");
        }
        
        // Multi-threaded implementation - Sub-histograms
        System.out.println("\nMulti-threaded implementation - Sub-histograms:");
        for (int numThread : numOfThreads) {
            totalTime = 0;
            for (int i = 0; i < num_loops; i++) {
                colourImage output = new colourImage();
                output.width = input_img.width;
                output.height = input_img.height;
                output.pixels = new short [output.height][output.width][3];
                
                long startTime = System.nanoTime();
                if (numThread == 1) {
                    SingleHistogramEqualization(input_img, output);
                } else {
                    MultiThreadHistogramEqualizationSub(input_img, output, numThread);
                }
                long endTime = System.nanoTime();
                
                totalTime += (endTime - startTime);
                
                // Save first iteration output
                if (i == 0) {
                    String outputFileName = "C:/Users/Salman/Desktop/ECTE 331/SubHist_" + numThread + "threads.jpg";
                    imageReadWrite.writeJpgImage(output, outputFileName);
                    System.out.println("Saved: SubHist_" + numThread + "threads.jpg");
                }
            }
            avgTime = (totalTime / num_loops) / 1000000.0;
            System.out.println("Threads: " + numThread + ", Average time: " + avgTime + " ms");
        }
            
        // Demo matrix manipulation (keep as in original)
        int width=4, height=4;
        short mat [][]=new short[height][width];
        for(int i=0; i<height; i++)
            for(int j=0;j<width;j++)
                mat[i][j]=(short)(i*width+j);    
     
        short vect []=new short[height*width];
        matManipulation.mat2Vect(mat, width, height, vect);
   
        System.out.println("\nMatrix to Vector Demo for reference and to see how it works:");
        for(int i=0; i<height; i++) {        
            for(int j=0;j<width;j++)
                System.out.printf("%3d ", mat[i][j]);
            System.out.println();
        }
     
        System.out.print("Vector: ");
        for(int i=0; i<width*height; i++)         
            System.out.printf("%d ", vect[i]);
        System.out.println();
    }
    
    public static void SingleHistogramEqualization(colourImage input, colourImage output) {
        int size = input.height * input.width;
        
        for (int color = 0; color < 3; color++) {
            int [] histogram = new int[level];
            
            // Step 1: Build histogram
            for (int i = 0; i < input.height; i++) {
                for (int j = 0; j < input.width; j++) {
                    histogram[input.pixels[i][j][color]]++;
                }
            }
            
            // Step 2: Calculate cumulative histogram
            int [] cumulativeHist = new int[level];
            cumulativeHist[0] = histogram[0];
            
            for (int i = 1; i < level; i++) {
                cumulativeHist[i] = cumulativeHist[i-1] + histogram[i];
            }
            
            // Optimization: pre-calculate normalized values
            for (int i = 0; i < level; i++) {
                cumulativeHist[i] = (cumulativeHist[i] * (level-1)) / size;
            }
            
            // Step 3: Map pixels
            for (int i = 0; i < input.height; i++) {
                for (int j = 0; j < input.width; j++) {
                    output.pixels[i][j][color] = (short)cumulativeHist[input.pixels[i][j][color]];
                }
            }
        }
    }
    
    // Row-based threading (Figure 2.a pattern)
    public static void MultiThreadHistogramEqualizationSharedRows(colourImage input, colourImage output, int numOfThreads) {
        int size = input.height * input.width;
        Thread[] threads = new Thread[numOfThreads];
        
        for (int color = 0; color < 3; color++) {
            AtomicIntegerArray sharedHistogram = new AtomicIntegerArray(level);
            
            // Build histogram using multiple threads
            for (int t = 0; t < numOfThreads; t++) {
                final int threadId = t;
                final int currentColor = color;
                threads[t] = new Thread(() -> {
                    int startRow = threadId * input.height / numOfThreads;
                    int endRow = (threadId + 1) * input.height / numOfThreads;
                    
                    for (int i = startRow; i < endRow; i++) {
                        for (int j = 0; j < input.width; j++) {
                            sharedHistogram.incrementAndGet(input.pixels[i][j][currentColor]);
                        }
                    }
                });
                threads[t].start();
            }
            
            // Wait for all threads to complete
            for (int t = 0; t < numOfThreads; t++) {
                try {
                    threads[t].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            // Calculate cumulative histogram (single-threaded)
            int[] cumulativeHist = new int[level];
            cumulativeHist[0] = sharedHistogram.get(0);
            
            for (int i = 1; i < level; i++) {
                cumulativeHist[i] = cumulativeHist[i-1] + sharedHistogram.get(i);
            }
            
            for (int i = 0; i < level; i++) {
                cumulativeHist[i] = (cumulativeHist[i] * (level-1)) / size;
            }
            
            // Apply mapping using multiple threads
            for (int t = 0; t < numOfThreads; t++) {
                final int threadId = t;
                final int currentColor = color;
                final int[] finalCumulativeHist = cumulativeHist;
                threads[t] = new Thread(() -> {
                    int startRow = threadId * input.height / numOfThreads;
                    int endRow = (threadId + 1) * input.height / numOfThreads;
                    
                    for (int i = startRow; i < endRow; i++) {
                        for (int j = 0; j < input.width; j++) {
                            output.pixels[i][j][currentColor] = (short)finalCumulativeHist[input.pixels[i][j][currentColor]];
                        }
                    }
                });
                threads[t].start();
            }
            
            for (int t = 0; t < numOfThreads; t++) {
                try {
                    threads[t].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    // Column-based threading (Figure 2.b pattern)
    public static void MultiThreadHistogramEqualizationSharedCols(colourImage input, colourImage output, int numOfThreads) {
        int size = input.height * input.width;
        Thread[] threads = new Thread[numOfThreads];
        
        for (int color = 0; color < 3; color++) {
            AtomicIntegerArray sharedHistogram = new AtomicIntegerArray(level);
            
            // Build histogram using column-based division
            for (int t = 0; t < numOfThreads; t++) {
                final int threadId = t;
                final int currentColor = color;
                threads[t] = new Thread(() -> {
                    int startCol = threadId * input.width / numOfThreads;
                    int endCol = (threadId + 1) * input.width / numOfThreads;
                    
                    for (int j = startCol; j < endCol; j++) {
                        for (int i = 0; i < input.height; i++) {
                            sharedHistogram.incrementAndGet(input.pixels[i][j][currentColor]);
                        }
                    }
                });
                threads[t].start();
            }
            
            for (int t = 0; t < numOfThreads; t++) {
                try {
                    threads[t].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            // Calculate cumulative histogram
            int[] cumulativeHist = new int[level];
            cumulativeHist[0] = sharedHistogram.get(0);
            
            for (int i = 1; i < level; i++) {
                cumulativeHist[i] = cumulativeHist[i-1] + sharedHistogram.get(i);
            }
            
            for (int i = 0; i < level; i++) {
                cumulativeHist[i] = (cumulativeHist[i] * (level-1)) / size;
            }
            
            // Apply mapping using column-based division
            for (int t = 0; t < numOfThreads; t++) {
                final int threadId = t;
                final int currentColor = color;
                final int[] finalCumulativeHist = cumulativeHist;
                threads[t] = new Thread(() -> {
                    int startCol = threadId * input.width / numOfThreads;
                    int endCol = (threadId + 1) * input.width / numOfThreads;
                    
                    for (int j = startCol; j < endCol; j++) {
                        for (int i = 0; i < input.height; i++) {
                            output.pixels[i][j][currentColor] = (short)finalCumulativeHist[input.pixels[i][j][currentColor]];
                        }
                    }
                });
                threads[t].start();
            }
            
            for (int t = 0; t < numOfThreads; t++) {
                try {
                    threads[t].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static void MultiThreadHistogramEqualizationSub(colourImage input, colourImage output, int numOfThreads) {
        int size = input.height * input.width;
        Thread[] threads = new Thread[numOfThreads];
        
        for (int color = 0; color < 3; color++) {
            int[][] subHistograms = new int[numOfThreads][level];
            
            // Build sub-histograms
            for (int t = 0; t < numOfThreads; t++) {
                final int threadId = t;
                final int currentColor = color;
                threads[t] = new Thread(() -> {
                    int startRow = threadId * input.height / numOfThreads;
                    int endRow = (threadId + 1) * input.height / numOfThreads;
                    
                    for (int i = startRow; i < endRow; i++) {
                        for (int j = 0; j < input.width; j++) {
                            subHistograms[threadId][input.pixels[i][j][currentColor]]++;
                        }
                    }
                });
                threads[t].start();
            }
            
            for (int t = 0; t < numOfThreads; t++) {
                try {
                    threads[t].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            // Combine sub-histograms
            int[] histogram = new int[level];
            for (int i = 0; i < level; i++) {
                for (int t = 0; t < numOfThreads; t++) {
                    histogram[i] += subHistograms[t][i];
                }
            }
            
            // Calculate cumulative histogram
            int[] cumulativeHist = new int[level];
            cumulativeHist[0] = histogram[0];
            
            for (int i = 1; i < level; i++) {
                cumulativeHist[i] = cumulativeHist[i-1] + histogram[i];
            }
            
            for (int i = 0; i < level; i++) {
                cumulativeHist[i] = (cumulativeHist[i] * (level-1)) / size;
            }
            
            // Apply mapping
            for (int t = 0; t < numOfThreads; t++) {
                final int threadId = t;
                final int currentColor = color;
                final int[] finalCumulativeHist = cumulativeHist;
                threads[t] = new Thread(() -> {
                    int startRow = threadId * input.height / numOfThreads;
                    int endRow = (threadId + 1) * input.height / numOfThreads;
                    
                    for (int i = startRow; i < endRow; i++) {
                        for (int j = 0; j < input.width; j++) {
                            output.pixels[i][j][currentColor] = (short)finalCumulativeHist[input.pixels[i][j][currentColor]];
                        }
                    }
                });
                threads[t].start();
            }
            
            for (int t = 0; t < numOfThreads; t++) {
                try {
                    threads[t].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
