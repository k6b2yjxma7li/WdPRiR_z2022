package Lab1.src;

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
import java.awt.BorderLayout;
import java.awt.Color;

public class Mandelbrot {

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

    public static int calcMandelbrot(Complex z0, Complex c, int iterations, double threshold) {
        Complex zn = new Complex(z0.getRe(), z0.getIm());
        int i;
        for(i=0; i<iterations; ++i) {
            zn = (zn.mul(zn)).add(c);
            if(zn.mag() > threshold) {
                return i;
            }
        }
        return i;
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
    public static double threshold = 2.0;

    //
    public static BufferedImage mandelbrotBufferedImage = null;

    public static double mandelbrotReruns(int resolution, int rerunsNumber) {
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
        double dx = (xLimRight-xLimLeft)/resolution;
        double dy = (yLimRight-yLimLeft)/resolution;

        for (int i = 0; i < rerunsNumber; i++) {
            long start = System.nanoTime();
            for(int n=0; n<resolution; ++n) {
                for(int m=0; m<resolution; ++m) {
                    int breakpoint = calcMandelbrot(
                        new Complex(0, 0), 
                        new Complex(xLimLeft + dx*m, yLimLeft + dy*n),
                        maxIterations, threshold
                    );
                    double hue = Math.pow(breakpoint, 1./3.)/Math.pow(maxIterations, 1./3.);
                    float brightness = 1.0f;
                    float saturation = 1.0f;
                    if(breakpoint == maxIterations) {brightness = 0.0f;}
                    Color color = Color.getHSBColor(1-(float)hue, saturation, brightness);
                    mandelbrotBufferedImage.setRGB(m, n, color.getRGB());
                }
            }
            long stop = System.nanoTime();
            timings[i] = (double)(stop-start)/1000000000.0;
        }
        return arrayMean(timings);
    }
    public static void main(String[] args) throws IOException {

        int[] dims = {32, 64, 128, 256, 512, 1024, 2048, 4096, 8192};

        int rerunsNumber = 10;

        BufferedWriter writer = new BufferedWriter(new FileWriter("./timings.csv", false));
        for (int dim : dims) {
            mandelbrotBufferedImage = new BufferedImage(dim, dim, BufferedImage.TYPE_INT_ARGB);
            double timeResult = mandelbrotReruns(dim, rerunsNumber);
            writer.write(dim+","+timeResult+"\n");
            writer.flush();
        }
        writer.close();
        display(mandelbrotBufferedImage);

        int dim = dims[dims.length-1];

        System.out.println("Mandelbrot generation " + dim+"x"+dim +" done");
        File file = new File("mandelbrot_"+maxIterations+"_"+dim+"x"+dim+".png");
        ImageIO.write(mandelbrotBufferedImage, "png", file);
    }
}
