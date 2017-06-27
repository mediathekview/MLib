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
package de.mediathekview.mlib.tool;

import java.io.File;

import org.apache.commons.lang3.StringEscapeUtils;

public class Functions
{

    public static String textLaenge(int max, String text, boolean mitte, boolean addVorne)
    {
        if (text.length() > max)
        {
            if (mitte)
            {
                text = text.substring(0, 25) + " .... " + text.substring(text.length() - (max - 31));
            } else
            {
                text = text.substring(0, max - 1);
            }
        }
        while (text.length() < max)
        {
            if (addVorne)
            {
                text = ' ' + text;
            } else
            {
                text = text + ' ';
            }
        }
        return text;
    }

    public static String minTextLaenge(int max, String text)
    {
        while (text.length() < max)
        {
            text = text + ' ';
        }
        return text;
    }

    public static String unescape(String aText)
    {
        String unescapedText;
        unescapedText = StringEscapeUtils.unescapeXml(aText);
        unescapedText = StringEscapeUtils.unescapeHtml4(unescapedText);
        unescapedText = StringEscapeUtils.unescapeJava(unescapedText);
        unescapedText = unescapedText.replace("\\", "/").replace("\n"," ").replace("\"","'").trim();
        return unescapedText;
    }

    public static String addsPfad(String pfad1, String pfad2)
    {
        String ret = "";
        if (pfad1 != null && pfad2 != null)
        {
            if (pfad1.isEmpty())
            {
                ret = pfad2;
            } else if (pfad2.isEmpty())
            {
                ret = pfad1;
            } else if (!pfad1.isEmpty() && !pfad2.isEmpty())
            {
                if (pfad1.endsWith(File.separator))
                {
                    ret = pfad1.substring(0, pfad1.length() - 1);
                } else
                {
                    ret = pfad1;
                }
                if (pfad2.charAt(0) == File.separatorChar)
                {
                    ret += pfad2;
                } else
                {
                    ret += File.separator + pfad2;
                }
            }
        }
        if (ret.isEmpty())
        {
            Log.errorLog(283946015, pfad1 + " - " + pfad2);
        }
        return ret;
    }

    public static String addUrl(String u1, String u2)
    {
        if (u1.endsWith("/"))
        {
            return u1 + u2;
        } else
        {
            return u1 + '/' + u2;
        }
    }

    public static boolean istUrl(String dateiUrl)
    {
        //return dateiUrl.startsWith("http") ? true : false || dateiUrl.startsWith("www") ? true : false;
        return dateiUrl.startsWith("http") || dateiUrl.startsWith("www");
    }

    public static String getDateiName(String pfad)
    {
        //Dateinamen einer URL extrahieren
        String ret = "";
        if (pfad != null)
        {
            if (!pfad.isEmpty())
            {
                ret = pfad.substring(pfad.lastIndexOf('/') + 1);
            }
        }
        if (ret.contains("?"))
        {
            ret = ret.substring(0, ret.indexOf('?'));
        }
        if (ret.contains("&"))
        {
            ret = ret.substring(0, ret.indexOf('&'));
        }
        if (ret.isEmpty())
        {
            Log.errorLog(395019631, pfad);
        }
        return ret;
    }

    public static String removeHtml(String in)
    {
        return in.replaceAll("\\<.*?>", "");
    }
}
