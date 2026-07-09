package com.ca.ui;

import com.ca.core.MemoryRepository;
import com.ca.db.model.ApplicationLog;
import com.gt.common.AppStarter;
import com.gt.uilib.components.AppFrame;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.Date;

public class Main {

    private static AppFrame gui;

    private static void setUpAndShowGui() {
        gui = AppFrame.getInstance();
        gui.setVisible(true);
        gui.setLocationRelativeTo(null);
        gui.addComponentListener(new java.awt.event.ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent event) {
                Dimension dGUI = new Dimension(Math.max(780, gui.getWidth()), Math.max(580, gui.getHeight()));
                Dimension mindGUI = new Dimension(780, 580);
                gui.setMinimumSize(mindGUI);
                gui.setPreferredSize(mindGUI);
                gui.setSize(dGUI);
            }
        });
    }

    public static void showMaximized() {
        gui.setState(java.awt.Frame.NORMAL);
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) throws Exception {
        if (SystemUtils.IS_OS_WINDOWS) {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }

        // Logs
        File f = new File("log");
        f.mkdir();

        startApp();
    }

    private static void startApp() {

        // En Java Swing, toda la interfaz gráfica debe manejarse en un hilo especial 
        // llamado Event Dispatch Thread (EDT).
        EventQueue.invokeLater(() -> {

            if (new AppStarter().alreadyRunning) {
                setApplicationStartLog();
                setUpAndShowGui(); // Inicializa la interfaz gráfica
            } else {
                System.exit(0);
            }
        });
    }

    // Lanza / Inicializa la estructura en RAM
    private static void setApplicationStartLog() {
        try {
            ApplicationLog log = new ApplicationLog();
            log.setDateTime(new Date());
            log.setMessage("Application Started");
            MemoryRepository.getInstance().saveOrUpdate(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
