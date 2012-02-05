package de.sendsor.accelerationSensor.algorithm.walonka;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Calendar;
 
import javax.swing.JFileChooser;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
 
public class Rechner {
 
        public static void rechne(File inf,File outf){
                String read;
                final String lineSeparator = System.getProperty("line.separator");
                try{
                        BufferedWriter out = new BufferedWriter(new FileWriter(outf)); 
                        BufferedWriter out2 = new BufferedWriter(new FileWriter(new File("output.csv")));      
                        BufferedReader in = new BufferedReader(new FileReader(inf));
                        out.write(in.readLine()+lineSeparator);
                        out.write(in.readLine()+lineSeparator);
                        out.write(in.readLine()+lineSeparator);
                        out.write(in.readLine()+lineSeparator);
                        out.write(in.readLine()+lineSeparator);
                        out.write(in.readLine()+lineSeparator);
                        out.write(in.readLine()+lineSeparator);
                        out.write(in.readLine()+lineSeparator);
                        int coun=0;
                        int last=0;
                        while((read=in.readLine())!=null){
                                ++coun;
                                //System.out.println(++coun);
                                String h=read.substring(12, 14);
                                String m=read.substring(15, 17);
                                String s=read.substring(18, 20);
                                String ms=read.substring(21, 24);;
                                String timestamp=h+m+s+ms;
                                int time=Integer.parseInt(timestamp);
 
                                if(last+40<time){
                                        last=time;
                                        System.out.println(coun);
                                        System.out.println(read);
                                        out.write(read+lineSeparator);
                                        out2.write(read+lineSeparator);
                                        //System.out.println(time);
                                }
                        }
                        out.flush();
                        in.close();
                        out.close();
                }
                catch(Exception e){
                        e.printStackTrace();
                }
        }
        public static void rechne(){
                String read;
                final String lineSeparator = System.getProperty("line.separator");
                try{
                        BufferedWriter out = new BufferedWriter(new FileWriter(new File("output.arff")));      
                        BufferedWriter out2 = new BufferedWriter(new FileWriter(new File("output.csv")));      
                        BufferedReader in = new BufferedReader(new FileReader(new File("input.arff")));
                        out.write(in.readLine()+lineSeparator);
                        out.write(in.readLine()+lineSeparator);
                        out.write(in.readLine()+lineSeparator);
                        out.write(in.readLine()+lineSeparator);
                        out.write(in.readLine()+lineSeparator);
                        out.write(in.readLine()+lineSeparator);
                        out.write(in.readLine()+lineSeparator);
                        out.write(in.readLine()+lineSeparator);
                        int coun=0;
                        int last=0;
                        
                        /* =========================================== */
                        while((read=in.readLine())!=null){
                                ++coun;
                                //System.out.println(++coun);
                                String h=read.substring(12, 14);
                                String m=read.substring(15, 17);
                                String s=read.substring(18, 20);
                                String ms=read.substring(21, 24);;
                                String timestamp=h+m+s+ms;
                                int time=Integer.parseInt(timestamp);
 
                                if(last+40<time){
                                        last=time;
                                        System.out.println(coun);
                                        System.out.println(read);
                                        out.write(read+lineSeparator);
                                        out2.write(read+lineSeparator);
                                        //System.out.println(time);
                                }
                        }
                        /* =========================================== */
                        out.flush();
                        in.close();
                        out.close();
                }
                catch(Exception e){
                        e.printStackTrace();
                }
        }
       
        public static void main(String[] args){
                Rechner rechner = new Rechner();
                //rechner.oeffnen();
                rechne();
                //rechne(rechner.oeffnen(), rechner.schreiben());
               
        }
       
    private File oeffnen() {
        final JFileChooser chooser = new JFileChooser("Verzeichnis wählen");
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        final File file = new File("/home");
 
        chooser.setCurrentDirectory(file);
 
        chooser.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)
                        || e.getPropertyName().equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
                    final File f = (File) e.getNewValue();
                }
            }
        });
 
        chooser.setVisible(true);
        final int result = chooser.showOpenDialog(null);
 
        if (result == JFileChooser.APPROVE_OPTION) {
            File inputVerzFile = chooser.getSelectedFile();
            return inputVerzFile;
        }
        chooser.setVisible(false);
        return null;
    }  
   
    private File schreiben() {
        final JFileChooser chooser = new JFileChooser("Verzeichnis wählen");
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.SAVE_DIALOG);
        final File file = new File("/home");
 
        chooser.setCurrentDirectory(file);
 
        chooser.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)
                        || e.getPropertyName().equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
                    final File f = (File) e.getNewValue();
                }
            }
        });
 
        chooser.setVisible(true);
        final int result = chooser.showOpenDialog(null);
 
        if (result == JFileChooser.APPROVE_OPTION) {
            File inputVerzFile = chooser.getSelectedFile();
            return inputVerzFile;
        }
        chooser.setVisible(false);
        return null;
    }
 
}
