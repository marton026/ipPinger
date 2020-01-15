import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class FormPing {
    private final String configFile="config.dat";
    private String s, p = "";
    private File path, soundPath;
    private String czasPing = "5"; //sekund
    private String czasPomiedzy = "5"; //minut
    private JButton buttonWybierzIp;
    private JPanel mPanel;
    private JButton buttonZmienDzwiek;
    private JTextField textFieldCzasPing;
    private JButton buttonZapiszCzasPingowania;
    private JTextField textFieldCzasPomiedzy;
    private JButton buttonZapiszCzasPomiedzy;
    private JButton buttonStart;
    private JButton buttonZamknij;
    private JButton buttonLog;
    public JTextArea textAreaLogi;
    private JProgressBar progressBar1;
    private JScrollPane scroll;
    private boolean zmieniacz;

    public FormPing() {
        buttonZamknij.addActionListener(e -> {
            Object z = e.getSource();
            if (z == buttonZamknij) {
                try {
                    System.exit(0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        buttonWybierzIp.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            FileFilter txtFilter = new FileTypeFilter(".txt", "Dokument tekstowy");
            fileChooser.addChoosableFileFilter(txtFilter);
            fileChooser.setAcceptAllFileFilterUsed(false);
            int result = fileChooser.showSaveDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                path = fileChooser.getSelectedFile();
                try {
                    s = pobierzLiniezPliku(2, configFile);
                    PrintWriter zapis = new PrintWriter(configFile);
                    zapis.println(path);
                    zapis.println(s);
                    zapis.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }
        });
        buttonZmienDzwiek.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            FileFilter wavFilter = new FileTypeFilter(".wav", "Plik audio");
            fileChooser.addChoosableFileFilter(wavFilter);
            fileChooser.setAcceptAllFileFilterUsed(false);
            int result = fileChooser.showSaveDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                soundPath = fileChooser.getSelectedFile();
                try {
                    p = pobierzLiniezPliku(1, configFile);
                    PrintWriter zapis = new PrintWriter(configFile);
                    zapis.println(p);
                    zapis.println(soundPath);
                    zapis.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        buttonZapiszCzasPingowania.addActionListener(e -> {
            int czasInt = 0;
            if (czyLiczba(textFieldCzasPing.getText()) == true) {
                czasInt = Integer.parseInt(textFieldCzasPing.getText());
            }
            if (czasInt > 0 && czasInt < 100) {
                czasPing = textFieldCzasPing.getText();
            } else {
                JOptionPane.showMessageDialog(null, "Niepoprawna wartość lub zakres czasu. Prawidłowa wartość: 1-99 s");
                textFieldCzasPing.setText("5");
            }
        });
        buttonZapiszCzasPomiedzy.addActionListener(e -> {
            int pomiedzyInt = 0;
            if (czyLiczba(textFieldCzasPomiedzy.getText())) {
                pomiedzyInt = Integer.parseInt(textFieldCzasPomiedzy.getText());
            }
            if (pomiedzyInt > 0 && pomiedzyInt < 1441) {
                czasPomiedzy = textFieldCzasPomiedzy.getText();
            } else {
                JOptionPane.showMessageDialog(null, "Niepoprawna wartość lub zakres czasu. Prawidłowa wartość: 1-1440 min");
                textFieldCzasPomiedzy.setText("5");
            }
        });
        buttonStart.addActionListener(e -> {
            try {
                path = new File(pobierzLiniezPliku(1, configFile));
                soundPath = new File(pobierzLiniezPliku(2, configFile));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (zmieniacz == false) {
                progressBar1.setIndeterminate(true);
                stopStart();
                zmieniacz = true;
                buttonStart.setText("Stop");
            } else {
                zmieniacz = false;
                progressBar1.setIndeterminate(false);
                buttonStart.setText("Start");
            }

        });
        buttonLog.addActionListener(e -> {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().edit(new File("log.txt"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private String teraz() {
        LocalDateTime teraz = LocalDateTime.now();
        DateTimeFormatter formater = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return teraz.format(formater);
    }

    private boolean czyLiczba(String znaki) {
        if (znaki.chars().allMatch(Character::isDigit) == true) {
            return true;
        } else {
            return false;
        }
    }

    private String pobierzLiniezPliku(int lineNumber, String plik) throws IOException {
        return Files.lines(Paths.get(plik))
                .skip(lineNumber - 1).findFirst().get();
    }

    private void dzwiek() {
        try {
            URL url = new URL("file:" + soundPath);
            Clip clip = AudioSystem.getClip();
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            clip.open(ais);
            clip.start();
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopStart() {
        new Thread(() -> {
            while (true) {
                File file = new File(String.valueOf(path));
                Scanner scanner = null;
                if (zmieniacz) {
                    try {
                        scanner = new Scanner(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    while (scanner.hasNextLine()) {
                        String ip = scanner.nextLine();
                        boolean reachable = false;
                        try {
                            reachable = InetAddress.getByName(ip).isReachable(Integer.parseInt(czasPing) * 1000);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (reachable == false) {
                            String ipFormat = String.format("%-11s%15s rozłączone: %-17s", "Urządzenie:", ip, teraz());
                            textAreaLogi.append(ipFormat + "\n");
                            try {
                                FileWriter fileWriter = new FileWriter("log.txt", true);
                                PrintWriter printWriter = new PrintWriter(fileWriter);
                                printWriter.println(ipFormat);
                                printWriter.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            dzwiek();
                        }
                    }
                }
                try {
                    Thread.sleep(Long.parseLong(czasPomiedzy) * 60000);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }

        }).start();
    }

    public static void main(String[] args) throws FileNotFoundException {
        JFrame frame = new JFrame("IP Pinger");
        frame.setContentPane(new FormPing().mPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(new ImageIcon("ping.png").getImage());
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
        File fileConf = new File("config.dat");
        if (!fileConf.exists()) {
            try {
                fileConf.createNewFile();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (fileConf.length() == 0) {
            PrintWriter zapis = new PrintWriter(fileConf);
            zapis.println("listaip.txt");
            zapis.println("sound.wav");
            zapis.close();
        }
        File fileLog = new File("log.txt");
        if (!fileLog.exists()) {
            try {
                fileLog.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
