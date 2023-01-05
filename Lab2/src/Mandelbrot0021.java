package Lab2.src;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.awt.BorderLayout;
import java.awt.Color;

public class Mandelbrot0021 {

    public static class Complex {
        private double real = 0.0;
        private double imag = 0.0;

        public void setRe(double re) {
            real = re;
        }

        public void setIm(double im) {
            imag = im;
        }

        public double getRe() {
            return real;
        }

        public double getIm() {
            return imag;
        }

        public Complex(double re, double im) {
            setIm(im);
            setRe(re);
        }

        public Complex conj() {
            return new Complex(real, -imag);
        }

        public Complex mul(Complex c) {
            return new Complex(
                getRe()*c.getRe()-getIm()*c.getIm(),
                getRe()*c.getIm()+getIm()*c.getRe()
            );
        }

        public Complex add(Complex c) {
            return new Complex(getRe()+c.getRe(), getIm()+c.getIm());
        }

        public double mag() {
            return Math.sqrt(getRe()*getRe() + getIm()*getIm());
        }
    }

    private static JFrame frame;
    private static JLabel label;

    public static void display(BufferedImage image){
        if(frame==null){
            frame=new JFrame();
            frame.setTitle("Mandelbrot");
            frame.setSize(480, 640);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            label=new JLabel();
            label.setIcon(new ImageIcon(image));
            frame.getContentPane().add(label,BorderLayout.CENTER);
            frame.setLocationRelativeTo(null);
            frame.pack();
            frame.setVisible(true);
        } else { 
            label.setIcon(new ImageIcon(image));
        }
    }

    public static int calcMandelbrotPixel(Complex z0, Complex c, int iterations, double magThreshold) {
        Complex zn = new Complex(z0.getRe(), z0.getIm());
        int i;
        for(i=0; i<iterations; ++i) {
            zn = (zn.mul(zn)).add(c);
            if(zn.mag() > magThreshold) {
                return i;
            }
        }
        return i;
    }

    public static void calcMandelbrotImage(int[] xResolution, int[] yResolution, double[] xParams, double[] yParams) {
        double dx = xParams[0];
        double dy = yParams[0];

        double cReal = xParams[1];
        double cImag = yParams[1];

        for (int xPxNo = xResolution[0]; xPxNo < xResolution[1]; xPxNo++) {
            // pixel index should be relative here, hence xPxNo-xResolution[0]
            cReal = xParams[1] + (double)xPxNo*dx;
            for (int yPxNo = yResolution[0]; yPxNo < yResolution[1]; yPxNo++) {
                cImag = yParams[1] + (double)yPxNo*dy;
                int stopIteration = calcMandelbrotPixel(
                    new Complex(0.0, 0.0), 
                    new Complex(cReal, cImag),
                    maxIterations, magThreshold
                );
                double hue = Math.pow(stopIteration, 1./3.)/Math.pow(maxIterations, 1./3.);
                float brightness = 1.0f;
                float saturation = 1.0f;
                if(stopIteration == maxIterations) {brightness = 0.0f;}
                Color color = Color.getHSBColor(1-(float)hue, saturation, brightness);
                mandelbrotBufferedImage.setRGB(xPxNo, yPxNo, color.getRGB());
            }
        }
    }

    public static double arrayMean(double[] doubleArray) {
        double sum = 0.0;
        for (double d : doubleArray) {
            sum += d;
        }
        return sum/(double)doubleArray.length;
    }

    // domain limits
    public static double xLimLeft = -2.1;
    public static double xLimRight = 0.6;
    public static double yLimLeft = -1.2;
    public static double yLimRight = 1.2;

    // rendering params
    public static int maxIterations = 200;
    public static double magThreshold = 2.0;

    public static BufferedImage mandelbrotBufferedImage = null;

    public static double mandelbrotReruns(int resolution, int subresolution, int rerunsNumber) throws InterruptedException {
        double[] timings = new double[rerunsNumber];
        if(mandelbrotBufferedImage == null) {
            mandelbrotBufferedImage = new BufferedImage(
                resolution, 
                resolution, 
                BufferedImage.TYPE_INT_ARGB
            );
        } else {
            resolution = mandelbrotBufferedImage.getWidth();
        }

        double xSpan = xLimRight-xLimLeft;
        double ySpan = yLimRight-yLimLeft;

        final int pxResolution = resolution;

        double dx = xSpan/(double)pxResolution;
        double dy = ySpan/(double)pxResolution;

        int xSubresolution = subresolution;
        int ySubresolution = 1;
        
        for (int i = 0; i < rerunsNumber; i++) {
            // timing
            long start = System.nanoTime();
            // creating pool of threads that correspond with number of processors
            ExecutorService exPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            // outsourcing parts of image to threads in the pool
            for (int ix = 0; ix < pxResolution; ix+=xSubresolution) {
                for (int iy = 0; iy < pxResolution; iy+=ySubresolution) {
                    final int jx = ix;
                    final int jy = iy;
                    exPool.submit(()->{
                        calcMandelbrotImage(
                            new int[]{jx, jx+subresolution},
                            new int[]{jy, jy+subresolution}, 
                            new double[]{dx, xLimLeft},
                            new double[]{dy, yLimLeft}
                        );
                    });
                }
            }
            // for (int j = 0; j < threadCount; j++) {
            //     // calculating Mandelbrot's set image
            //     final int k = j;
            //     threads[k] = new Thread(()->{calcMandelbrotImage(
            //         new int[]{k * (int)((float)pxResolution/(float)threadCount), (k+1) * (int)((float)pxResolution/(float)threadCount)},
            //         new int[]{0, pxResolution}, 
            //         new double[]{xLimLeft + (double)k * (double)xSpan/(double)threadCount, xLimLeft + (double)(k+1) * (double)xSpan/(double)threadCount},
            //         new double[]{yLimLeft, yLimRight}
            //     );});
            // }
            // for (int j = 0; j < threadCount; j++) {
            //     threads[j].start();
            // }
            // for (int j = 0; j < threadCount; j++) {
            //     threads[j].join();
            // }
            long stop = System.nanoTime();
            timings[i] = (double)(stop-start)/1.0e+9;
            System.out.printf("%d/%d\r", i+1, rerunsNumber);
        }
        System.out.println();
        return arrayMean(timings);
    }
    public static void main(String[] args) throws IOException, InterruptedException {

        int[] dims = {32, 64, 128, 256, 512, 1024, 2048, 4096, 8192};
        // int[] dims = {32};

        int rerunsNumber = 100;

        BufferedWriter writer = new BufferedWriter(new FileWriter("WdPRiR_z2022/Lab2/src/timings.csv", false));
        for (int dim : dims) {
            mandelbrotBufferedImage = new BufferedImage(dim, dim, BufferedImage.TYPE_INT_ARGB);
            double timeResult = mandelbrotReruns(dim, 128, rerunsNumber);
            writer.write(dim+","+timeResult+"\n");
            writer.flush();
        }

        int dim = dims[dims.length-1];
        writer.close();
        display(mandelbrotBufferedImage);


        System.out.println("Mandelbrot generation " + dim+"x"+dim +" done");
        File file = new File("mandelbrot_"+maxIterations+"_"+dim+"x"+dim+".png");
        ImageIO.write(mandelbrotBufferedImage, "png", file);
    }
}
