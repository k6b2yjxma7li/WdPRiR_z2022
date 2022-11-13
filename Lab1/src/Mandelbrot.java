package Lab1.src;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
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
    public static void main(String[] args) throws IOException {

        // System.out.println(calcMandelbrot(new Complex(0, 0), new Complex(0.1, 0.1), 200, 2));
        int[] dims = {512};
        // int[] dims = {32,};

        double xLimLeft = -2.1;
        double xLimRight = 0.6;
        double yLimLeft = -1.2;
        double yLimRight = 1.2;

        BufferedWriter writer = new BufferedWriter(new FileWriter("./timings.csv", true));

        BufferedImage mandelbrotBufferedImage = null;

        for (int dim : dims) {
            double dx = (xLimRight-xLimLeft)/dim;
            double dy = (yLimRight-yLimLeft)/dim;

            mandelbrotBufferedImage = new BufferedImage(dim, dim, BufferedImage.TYPE_INT_ARGB);

            long start = System.nanoTime();

            int maxIterations = 200;

            for(int n=0; n<dim; ++n) {
                for(int m=0; m<dim; ++m) {
                    int breakpoint = calcMandelbrot(
                        new Complex(0, 0), 
                        new Complex(xLimLeft + dx*m, yLimLeft + dy*n),
                        maxIterations, 2.0
                    );
                    // double hue = Math.log10(breakpoint)/Math.log10(maxIterations);
                    // double hue = Math.sqrt(breakpoint)/Math.sqrt(maxIterations);
                    double hue = Math.pow(breakpoint, 1./3.)/Math.pow(maxIterations, 1./3.);
                    float brightness = 1.0f;
                    float saturation = 1.0f;
                    if(breakpoint == maxIterations) {brightness = 0.0f;}
                    Color color = Color.getHSBColor(1-(float)hue, saturation, brightness);
                    mandelbrotBufferedImage.setRGB(m, n, color.getRGB());
                }
                // if(n % 256 == 0){
                //     System.out.println(n+"/"+dim);
                // }
            }
            writer.write(dim+","+(System.nanoTime()-start)/1000000000.0+"\n");
        }
        writer.close();
        display(mandelbrotBufferedImage);

        // System.out.println("Mandelbrot " + dim+"x"+dim +" done");
        // File file = new File("mandelbrot_"+maxIterations+"_"+dim+"x"+dim+".png");
        // ImageIO.write(mandelbrotBufferedImage, "png", file);
    }
}
