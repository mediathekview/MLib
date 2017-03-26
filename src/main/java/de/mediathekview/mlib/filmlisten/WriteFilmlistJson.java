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
package de.mediathekview.mlib.filmlisten;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.tool.Log;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class WriteFilmlistJson
{

    private void fastChannelCopy(final ReadableByteChannel src, final WritableByteChannel dest) throws IOException
    {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(64 * 1024);
        while (src.read(buffer) != -1)
        {
            buffer.flip();
            dest.write(buffer);
            buffer.compact();
        }

        buffer.flip();

        while (buffer.hasRemaining())
        {
            dest.write(buffer);
        }
    }

    /**
     * Write film data and compress with LZMA2.
     *
     * @param datei      file path
     * @param listeFilme film data
     */
    public void filmlisteSchreibenJsonCompressed(String datei, ListeFilme listeFilme)
    {
        final String tempFile = datei + "_temp";
        filmlisteSchreibenJson(tempFile, listeFilme);

        try
        {
            Log.sysLog("Komprimiere Datei: " + datei);
            if (datei.endsWith(Const.FORMAT_XZ))
            {
                final Path xz = testNativeXz();
                if (xz != null)
                {
                    Process p = new ProcessBuilder(xz.toString(), "-9", tempFile).start();
                    final int exitCode = p.waitFor();
                    if (exitCode == 0)
                    {
                        Files.move(Paths.get(tempFile + ".xz"), Paths.get(datei), StandardCopyOption.REPLACE_EXISTING);
                    }
                } else
                    compressFile(tempFile, datei);
            }

            Files.deleteIfExists(Paths.get(tempFile));
        } catch (IOException | InterruptedException ex)
        {
            Log.sysLog("Komprimieren fehlgeschlagen");
        }
    }

    public void filmlisteSchreibenJson(String datei, ListeFilme listeFilme)
    {
        try
        {
            Log.sysLog("Filme schreiben (" + listeFilme.size() + " Filme) :");

            Log.sysLog("   --> Start Schreiben nach: " + datei);

            try (FileOutputStream fos = new FileOutputStream(datei); BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fos)))
            {
                String fakeJson = FilmSaveLoadHelper.toFakeJson(listeFilme, listeFilme.metaDaten[0], listeFilme.metaDaten[1], listeFilme.metaDaten[2], listeFilme.metaDaten[3], listeFilme.metaDaten[4]);
                bufferedWriter.write(fakeJson);
                bufferedWriter.flush();
                Log.sysLog("   --> geschrieben!");
            }
        } catch (Exception ex)
        {
            Log.errorLog(846930145, ex, "nach: " + datei);
        }
    }

    private Path testNativeXz()
    {
        final String[] paths = {"/usr/bin/xz", "/opt/local/bin/xz", "/usr/local/bin/xz"};

        Path xz = null;

        for (String path : paths)
        {
            xz = Paths.get(path);
            if (Files.isExecutable(xz))
            {
                break;
            }
        }

        return xz;
    }

    private void compressFile(String inputName, String outputName) throws IOException
    {
        try (InputStream input = new FileInputStream(inputName);
             FileOutputStream fos = new FileOutputStream(outputName);
             final OutputStream output = new XZOutputStream(fos, new LZMA2Options());
             final ReadableByteChannel inputChannel = Channels.newChannel(input);
             final WritableByteChannel outputChannel = Channels.newChannel(output))
        {

            fastChannelCopy(inputChannel, outputChannel);
        } catch (IOException ignored)
        {
        }
    }
}
