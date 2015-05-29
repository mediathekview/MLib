/*
 * MediathekView
 * Copyright (C) 2008 W. Xaver
 * W.Xaver[at]googlemail.com
 * http://zdfmediathk.sourceforge.net/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package msearch.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import msearch.daten.ListeFilme;
import msearch.filmlisten.MSFilmlisteLesen;
import msearch.filmlisten.WriteFilmlistJson;
import msearch.filmlisten.WriteFilmlistXML;
import msearch.gui.MSearchGuiLoad;
import msearch.gui.PanelSenderSearch;
import msearch.tool.MSConfig;
import msearch.tool.MSConst;
import msearch.tool.MSLog;

public final class MSearchGui extends javax.swing.JFrame {

    String pfad = "";
    public static ListeFilme listeFilme;
    private MSearchGuiLoad filmeLaden;
    private final JButton[] buttonSender;
    private final String[] sender;

    public MSearchGui(String[] ar) {
        initComponents();
        if (ar != null && ar.length > 0 && !ar[0].startsWith("-")) {
            pfad = ar[0];
        }
        MSConfig.debug = true; // ist nur zum Debug
        listeFilme = new ListeFilme();
        filmeLaden = new MSearchGuiLoad();
        if (pfad.isEmpty()) {
            jTextFieldFilmliste.setText(System.getProperty("user.home") + File.separator + ".mediathek3" + File.separator + "filme.json");
            jTextFieldFilmlisteXml.setText(System.getProperty("user.home") + File.separator + ".mediathek3" + File.separator + "filme.xml");
        } else {
            jTextFieldFilmliste.setText(pfad);
            jTextFieldFilmlisteXml.setText(pfad.replace(".json", ".xml"));
        }
        new MSFilmlisteLesen().readFilmListe(jTextFieldFilmliste.getText(), listeFilme, 0 /*all days*/);
        jLabelAnzahl.setText(MSearchGui.listeFilme.size() + "");

        jButtonFilmlisteLoeschen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listeFilme.clear();
            }
        });
        jButtonCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listeFilme.check();
            }
        });

        jToggleButtonSetAlles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MSConfig.senderAllesLaden = jToggleButtonSetAlles.isSelected();
            }
        });
        jToggleButtonUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MSConfig.updateFilmliste = jToggleButtonUpdate.isSelected();
            }
        });
        jButtonAlleSenderLaden.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        filmeLaden.filmeBeimSenderSuchen(jToggleButtonSetAlles.isSelected(), true);
                    }
                }).start();
            }
        });
        jButtonSpeichern.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new WriteFilmlistJson().filmlisteSchreibenJson(jTextFieldFilmliste.getText(), listeFilme);
            }
        });
        jButtonFilmliste.addActionListener(new BeobPfad());
        jButtonSpeichernXml.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new WriteFilmlistXML().filmlisteSchreibenXml(jTextFieldFilmlisteXml.getText(), listeFilme);
            }
        });
        jButtonFilmlisteXml.addActionListener(new BeobPfadXml());
        jButtonGc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.gc();
            }
        });

        // Tab Sender laden
        jPanelSenderLaden.setLayout(new BorderLayout());
        jPanelSenderLaden.add(new PanelSenderSearch(filmeLaden), BorderLayout.CENTER);

        // Tab Sender löschen
        sender = filmeLaden.getSenderNamen();
        buttonSender = new JButton[sender.length];
        for (int i = 0; i < filmeLaden.getSenderNamen().length; ++i) {
            buttonSender[i] = new JButton(sender[i]);
            buttonSender[i].addActionListener(new BeobSenderLoeschen(sender[i]));
        }
        addSender();
        jButtonLog.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                String datei = "/tmp/testfile"; //////////////
                Date aktTime = new Date(System.currentTimeMillis());
                String aktTimeStr = sdf.format(aktTime);
                MSLog.systemMeldung("");
                MSLog.systemMeldung("Log schreiben: " + datei);
                MSLog.systemMeldung("--> " + aktTimeStr);
                File file = new File(datei);
                File dir = new File(file.getParent());
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        MSLog.fehlerMeldung(632012165, "Kann den Pfad nicht anlegen: " + dir.toString());
                    }
                }

                try {
                    OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file, true), MSConst.KODIERUNG_UTF);
                    out.write("===============================================================");
                    out.write("===============================================================");
                    out.write("\n");
                    out.write("--> " + aktTimeStr);
                    out.write("\n");
                    ArrayList<String> ret;
                    ret = filmeLaden.msFilmeSuchen.endeMeldung();
                    for (String s : ret) {
                        out.write(s);
                        out.write("\n");
                    }
                    ret = MSLog.fehlerMeldungen();
                    for (String s : ret) {
                        out.write(s);
                        out.write("\n");
                    }
                    out.write("\n");
                    out.write("\n");
                    out.write("\n");
                    out.write("\n");
                    out.write("\n");
                    out.write("\n");
                    out.write("\n");
                    out.write("\n");
                    out.write("\n");
                    out.write("\n");
                    out.close();

                    MSLog.systemMeldung("--> geschrieben!");
                } catch (Exception ex) {
                    MSLog.fehlerMeldung(846930145, ex, "nach: " + datei);
                }

            }
        });
    }

    private void addSender() {
        jPanelSenderDelete.removeAll();
        jPanelSenderDelete.setLayout(new GridLayout(0, 5));
        int nr = 0;
        for (String aSender : sender) {
            JButton btn = buttonSender[nr];
            btn.setText(aSender);
            jPanelSenderDelete.add(btn);
            ++nr;
        }
        jPanelSenderDelete.repaint();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jTabbedPane = new javax.swing.JTabbedPane();
        jPanelSuchen = new javax.swing.JPanel();
        jButtonFilmlisteLoeschen = new javax.swing.JButton();
        jButtonAlleSenderLaden = new javax.swing.JButton();
        jPanelSenderLaden = new javax.swing.JPanel();
        jToggleButtonSetAlles = new javax.swing.JToggleButton();
        jToggleButtonUpdate = new javax.swing.JToggleButton();
        jPanelLoeschen = new javax.swing.JPanel();
        jPanelSenderDelete = new javax.swing.JPanel();
        jPanelTool = new javax.swing.JPanel();
        jButtonCheck = new javax.swing.JButton();
        jButtonGc = new javax.swing.JButton();
        jButtonLog = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabelAnzahl = new javax.swing.JLabel();
        jTextFieldFilmliste = new javax.swing.JTextField();
        jButtonFilmliste = new javax.swing.JButton();
        jButtonSpeichern = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldFilmlisteXml = new javax.swing.JTextField();
        jButtonSpeichernXml = new javax.swing.JButton();
        jButtonFilmlisteXml = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButtonFilmlisteLoeschen.setText("Filmliste löschen");

        jButtonAlleSenderLaden.setText("alle Sender laden");

        jPanelSenderLaden.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        javax.swing.GroupLayout jPanelSenderLadenLayout = new javax.swing.GroupLayout(jPanelSenderLaden);
        jPanelSenderLaden.setLayout(jPanelSenderLadenLayout);
        jPanelSenderLadenLayout.setHorizontalGroup(
            jPanelSenderLadenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanelSenderLadenLayout.setVerticalGroup(
            jPanelSenderLadenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 408, Short.MAX_VALUE)
        );

        jToggleButtonSetAlles.setText("[-alles] setzen");

        jToggleButtonUpdate.setText("[-update] setzen");

        javax.swing.GroupLayout jPanelSuchenLayout = new javax.swing.GroupLayout(jPanelSuchen);
        jPanelSuchen.setLayout(jPanelSuchenLayout);
        jPanelSuchenLayout.setHorizontalGroup(
            jPanelSuchenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSuchenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelSuchenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelSenderLaden, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanelSuchenLayout.createSequentialGroup()
                        .addComponent(jButtonFilmlisteLoeschen)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonAlleSenderLaden)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jToggleButtonSetAlles)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jToggleButtonUpdate)
                        .addGap(0, 133, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanelSuchenLayout.setVerticalGroup(
            jPanelSuchenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSuchenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelSuchenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonFilmlisteLoeschen)
                    .addComponent(jButtonAlleSenderLaden)
                    .addComponent(jToggleButtonSetAlles)
                    .addComponent(jToggleButtonUpdate))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanelSenderLaden, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane.addTab("Suchen", jPanelSuchen);

        javax.swing.GroupLayout jPanelSenderDeleteLayout = new javax.swing.GroupLayout(jPanelSenderDelete);
        jPanelSenderDelete.setLayout(jPanelSenderDeleteLayout);
        jPanelSenderDeleteLayout.setHorizontalGroup(
            jPanelSenderDeleteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanelSenderDeleteLayout.setVerticalGroup(
            jPanelSenderDeleteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanelLoeschenLayout = new javax.swing.GroupLayout(jPanelLoeschen);
        jPanelLoeschen.setLayout(jPanelLoeschenLayout);
        jPanelLoeschenLayout.setHorizontalGroup(
            jPanelLoeschenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLoeschenLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelSenderDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(681, Short.MAX_VALUE))
        );
        jPanelLoeschenLayout.setVerticalGroup(
            jPanelLoeschenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLoeschenLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelSenderDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(363, Short.MAX_VALUE))
        );

        jTabbedPane.addTab("Löschen", jPanelLoeschen);

        jButtonCheck.setText("Check Filmliste");

        jButtonGc.setText("Gcc");

        jButtonLog.setText("Log schreiben");

        javax.swing.GroupLayout jPanelToolLayout = new javax.swing.GroupLayout(jPanelTool);
        jPanelTool.setLayout(jPanelToolLayout);
        jPanelToolLayout.setHorizontalGroup(
            jPanelToolLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelToolLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelToolLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButtonCheck, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonGc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonLog, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(642, Short.MAX_VALUE))
        );

        jPanelToolLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButtonCheck, jButtonGc});

        jPanelToolLayout.setVerticalGroup(
            jPanelToolLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelToolLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonGc)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonLog)
                .addContainerGap(364, Short.MAX_VALUE))
        );

        jTabbedPane.addTab("Tool", jPanelTool);

        jLabel1.setText("Filmliste:");

        jLabel2.setText("Anzahl Filme:");

        jLabelAnzahl.setText("-1");

        jTextFieldFilmliste.setText("jTextField1");

        jButtonFilmliste.setText("Auswählen");

        jButtonSpeichern.setText("Speichern");

        jLabel3.setText("Filmliste (xml):");

        jTextFieldFilmlisteXml.setText("jTextField1");

        jButtonSpeichernXml.setText("Speichern");

        jButtonFilmlisteXml.setText("Auswählen");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelAnzahl)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldFilmliste)
                            .addComponent(jTextFieldFilmlisteXml))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButtonFilmliste, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonFilmlisteXml, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButtonSpeichern, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonSpeichernXml, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jTabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 502, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel1)
                    .addComponent(jTextFieldFilmliste, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonFilmliste)
                    .addComponent(jButtonSpeichern))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextFieldFilmlisteXml, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonSpeichernXml)
                    .addComponent(jButtonFilmlisteXml))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabelAnzahl))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jButtonFilmliste, jButtonSpeichern, jLabel1, jTextFieldFilmliste});

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jButtonFilmlisteXml, jButtonSpeichernXml, jTextFieldFilmlisteXml});

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAlleSenderLaden;
    private javax.swing.JButton jButtonCheck;
    private javax.swing.JButton jButtonFilmliste;
    private javax.swing.JButton jButtonFilmlisteLoeschen;
    private javax.swing.JButton jButtonFilmlisteXml;
    private javax.swing.JButton jButtonGc;
    private javax.swing.JButton jButtonLog;
    private javax.swing.JButton jButtonSpeichern;
    private javax.swing.JButton jButtonSpeichernXml;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    public static javax.swing.JLabel jLabelAnzahl;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelLoeschen;
    private javax.swing.JPanel jPanelSenderDelete;
    private javax.swing.JPanel jPanelSenderLaden;
    private javax.swing.JPanel jPanelSuchen;
    private javax.swing.JPanel jPanelTool;
    private javax.swing.JTabbedPane jTabbedPane;
    private javax.swing.JTextField jTextFieldFilmliste;
    private javax.swing.JTextField jTextFieldFilmlisteXml;
    private javax.swing.JToggleButton jToggleButtonSetAlles;
    private javax.swing.JToggleButton jToggleButtonUpdate;
    // End of variables declaration//GEN-END:variables

    private class BeobSenderLoeschen implements ActionListener {

        private final String sender;

        public BeobSenderLoeschen(String ssender) {
            sender = ssender;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            listeFilme.deleteAllFilms(sender);
            jLabelAnzahl.setText(MSearchGui.listeFilme.size() + "");
        }
    }

    private class BeobPfad implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            int returnVal;
            JFileChooser chooser = new JFileChooser();
            if (!jTextFieldFilmliste.getText().equals("")) {
                chooser.setCurrentDirectory(new File(jTextFieldFilmliste.getText()));
            }
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileHidingEnabled(false);
            returnVal = chooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    jTextFieldFilmliste.setText(chooser.getSelectedFile().getAbsolutePath());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private class BeobPfadXml implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            int returnVal;
            JFileChooser chooser = new JFileChooser();
            if (!jTextFieldFilmlisteXml.getText().equals("")) {
                chooser.setCurrentDirectory(new File(jTextFieldFilmlisteXml.getText()));
            }
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileHidingEnabled(false);
            returnVal = chooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    jTextFieldFilmlisteXml.setText(chooser.getSelectedFile().getAbsolutePath());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
