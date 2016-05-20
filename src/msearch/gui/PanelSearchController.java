/*
 * MediathekView
 * Copyright (C) 2016 W. Xaver
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import msearch.filmeSuchen.MSGetUrl;
import msearch.filmeSuchen.MSListenerFilmeLaden;
import msearch.filmeSuchen.MSListenerFilmeLadenEvent;
import msearch.tool.MSConfig;
import msearch.tool.MSConst;
import msearch.tool.MSLog;

public class PanelSearchController implements Initializable {

    @FXML
    private Label lblSum;
    @FXML
    private Label lblProgress;
    @FXML
    private Label lblPercent;
    @FXML
    private Button btnStop;
    @FXML
    private Button btnAllSender;
    @FXML
    private Button btnLog;
    @FXML
    private ProgressBar pBar;
    @FXML
    private GridPane pSender;
    @FXML
    private RadioButton rbShort;
    @FXML
    private RadioButton rbLong;
    @FXML
    private RadioButton rbMax;
    @FXML
    private CheckBox cbLoadTime;
    @FXML
    private CheckBox cbDebug;
    @FXML
    private CheckBox cbUpdate;

    private int i = 0;
    private Button[] buttonSender;
    private String[] senderArray;
    private MSearchGuiLoad mSearchGuiLoad;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnStop.setOnAction(e -> System.out.println("Test"));

        initPanelSearch();

    }

    private void initPanelSearch() {
        lblSum.setText("");
        btnStop.setOnAction(e -> MSConfig.setStop(true));
        mSearchGuiLoad = new MSearchGuiLoad();

        rbShort.setSelected(true);
        MSConfig.senderLoadHow = MSConfig.LOAD_SHORT;
        rbShort.setOnAction(e -> MSConfig.senderLoadHow = MSConfig.LOAD_SHORT);
        rbLong.setOnAction(e -> MSConfig.senderLoadHow = MSConfig.LOAD_LONG);
        rbMax.setOnAction(e -> MSConfig.senderLoadHow = MSConfig.LOAD_MAX);

        MSGetUrl.showLoadTime = cbLoadTime.isSelected();
        cbLoadTime.setOnAction(e -> MSGetUrl.showLoadTime = cbLoadTime.isSelected());

        MSConfig.debug = cbDebug.isSelected();
        cbDebug.setOnAction(e -> MSConfig.debug = cbDebug.isSelected());

        cbUpdate.setOnAction(e -> MSConfig.updateFilmliste = cbUpdate.isSelected());

        btnAllSender.setOnAction(e -> new Thread(() -> {
            disableButton(true);
            mSearchGuiLoad.filmeBeimSenderSuchen(true);
        }).start());

        btnLog.setOnAction(e -> {
            writeLog();
        });

        senderArray = MSearchGuiLoad.getSenderNamen();
        buttonSender = new Button[senderArray.length];
        for (int i = 0; i < MSearchGuiLoad.getSenderNamen().length; ++i) {
            buttonSender[i] = new Button(senderArray[i]);
            buttonSender[i].setOnAction(new ActionLoadSender(senderArray[i]));
        }
        addSender();

        mSearchGuiLoad.addAdListener(new MSListenerFilmeLaden() {
            @Override
            public void progress(MSListenerFilmeLadenEvent event) {
                Platform.runLater(() -> {
                    if (event.max == 0) {
                        pBar.setProgress(0);
                        lblPercent.setText("");
                    } else {
                        if (event.progress == 0) {
                            pBar.setProgress(0);
                            lblPercent.setText("0%");
                        } else {
                            double prog = 1.0 * event.progress / event.max;
                            if (prog < 0) {
                                prog = 0;
                            } else if (prog > 1) {
                                prog = 0.99;
                            }
                            pBar.setProgress(prog);
                            int i = (int) (100 * prog);
                            lblPercent.setText(i + "%");
                        }
                    }
                    lblProgress.setText(textLaenge(80, event.text, true /* mitte */, false /*addVorne*/));
                    lblSum.setText(event.count + "");
                });
            }

            @Override
            public void fertig(MSListenerFilmeLadenEvent event) {
                Platform.runLater(() -> {
                    pBar.setProgress(0);
                    lblPercent.setText("");
                    lblSum.setText(Data.listeFilme.size() + "");
                    Data.mSearchGuiController.lblSum.setText(Data.listeFilme.size() + "");

                    disableButton(false);
                });
            }
        });
    }

    private void writeLog() {
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
            ret = mSearchGuiLoad.msFilmeSuchen.endeMeldung();
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

    private void addSender() {
        pSender.setHgap(10);
        pSender.setVgap(10);
        pSender.setPadding(new Insets(10));
        int zeile = 0, spalte = 0, count = 0;
        for (String aSender : senderArray) {
            Button btn = buttonSender[count];
            btn.setText(aSender);
            btn.setMaxWidth(Double.MAX_VALUE);
            pSender.add(btn, spalte, zeile);

            ++spalte;
            if (spalte >= 5) {
                ++zeile;
                spalte = 0;
            }
            ++count;
        }

    }

    private static String textLaenge(int max, String text, boolean mitte, boolean addVorne) {
        if (text.length() > max) {
            if (mitte) {
                text = text.substring(0, 25) + " .... " + text.substring(text.length() - (max - 31));
            } else {
                text = text.substring(0, max - 1);
            }
        }
        while (text.length() < max) {
            if (addVorne) {
                text = " " + text;
            } else {
                text = text + " ";
            }
        }
        return text;
    }

    private void disableButton(boolean disable) {
        for (Button aButtonSender : buttonSender) {
            aButtonSender.setDisable(disable);
        }
        btnAllSender.setDisable(disable);
    }

    private class ActionLoadSender implements EventHandler<ActionEvent> {

        private final String sender;

        public ActionLoadSender(String ssender) {
            sender = ssender;
        }

        @Override
        public void handle(ActionEvent t) {
            lblProgress.setText("");
            lblSum.setText("");
            disableButton(true);
            mSearchGuiLoad.updateSender(new String[]{sender} /* senderAllesLaden */);
        }
    }

}
