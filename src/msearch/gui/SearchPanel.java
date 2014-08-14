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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import msearch.daten.ListeFilme;
import msearch.daten.MSConfig;
import msearch.gui.FilmeLaden;
import msearch.gui.PanelSenderLaden;
import msearch.io.MSFilmlisteSchreiben;

public class SearchPanel extends JPanel {
    
    private ListeFilme listeFilme;
    private FilmeLaden filmeLaden;
    
    public SearchPanel(String pfad) {
        super();
        initComponents();
        listeFilme = new ListeFilme();
        filmeLaden = new FilmeLaden(listeFilme);
        if (pfad.isEmpty()) {
            jTextFieldFilmliste.setText(System.getProperty("user.home") + File.separator + ".mediathek3" + File.separator + "filme.json");
        } else {
            jTextFieldFilmliste.setText(pfad);
        }
        jPanelSenderLaden.add(new PanelSenderLaden(filmeLaden));
        
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
                new MSFilmlisteSchreiben().filmlisteSchreibenJson(jTextFieldFilmliste.getText(), listeFilme);
            }
        });
        jButtonFilmliste.addActionListener(new BeobPfad());
        jButtonGc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.gc();
            }
        });
    }


    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JTabbedPane jTabbed1 = new javax.swing.JTabbedPane();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        jPanelSenderLaden = new javax.swing.JPanel();
        jToggleButtonSetAlles = new javax.swing.JToggleButton();
        jButtonFilmlisteLoeschen = new javax.swing.JButton();
        jButtonAlleSenderLaden = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldFilmliste = new javax.swing.JTextField();
        jButtonSpeichern = new javax.swing.JButton();
        jButtonFilmliste = new javax.swing.JButton();
        javax.swing.JPanel jPanel5 = new javax.swing.JPanel();
        jButtonCheck = new javax.swing.JButton();
        jButtonGc = new javax.swing.JButton();

        jPanelSenderLaden.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED), "Sender starten"));
        jPanelSenderLaden.setLayout(new java.awt.BorderLayout());

        jToggleButtonSetAlles.setText("[-alles] setzen");

        jButtonFilmlisteLoeschen.setText("Filmliste löschen");

        jButtonAlleSenderLaden.setText("alle Sender laden");

        jLabel1.setText("Filmliste:");

        jButtonSpeichern.setText("Speichern");

        jButtonFilmliste.setText("Auswählen");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelSenderLaden, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldFilmliste, javax.swing.GroupLayout.PREFERRED_SIZE, 458, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonFilmliste)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonSpeichern))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButtonFilmlisteLoeschen)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonAlleSenderLaden)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButtonSetAlles)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButtonFilmlisteLoeschen, jToggleButtonSetAlles});

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButtonFilmliste, jButtonSpeichern});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonFilmlisteLoeschen)
                    .addComponent(jButtonAlleSenderLaden)
                    .addComponent(jToggleButtonSetAlles))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanelSenderLaden, javax.swing.GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextFieldFilmliste, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonFilmliste)
                    .addComponent(jButtonSpeichern))
                .addContainerGap())
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jButtonFilmliste, jTextFieldFilmliste});

        jTabbed1.addTab("Start Sender", jPanel2);

        jButtonCheck.setText("Check Filmliste");

        jButtonGc.setText("Gc");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonCheck, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonGc, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(696, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonGc)
                .addContainerGap(416, Short.MAX_VALUE))
        );

        jTabbed1.addTab("Tool", jPanel5);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbed1, javax.swing.GroupLayout.DEFAULT_SIZE, 865, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbed1, javax.swing.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAlleSenderLaden;
    private javax.swing.JButton jButtonCheck;
    private javax.swing.JButton jButtonFilmliste;
    private javax.swing.JButton jButtonFilmlisteLoeschen;
    private javax.swing.JButton jButtonGc;
    private javax.swing.JButton jButtonSpeichern;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanelSenderLaden;
    private javax.swing.JTextField jTextFieldFilmliste;
    private javax.swing.JToggleButton jToggleButtonSetAlles;
    // End of variables declaration//GEN-END:variables

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
    
}
