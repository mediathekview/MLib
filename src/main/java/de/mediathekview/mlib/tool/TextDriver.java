package de.mediathekview.mlib.tool;

import org.apache.commons.lang3.StringEscapeUtils;

public class TextDriver {
	private String text;

	public TextDriver(String aText) {
		if (aText == null) {
			text = "";
		} else {
			text = aText;
		}
	}

	public TextDriver unescape() {
		text = StringEscapeUtils.unescapeJava(StringEscapeUtils.unescapeHtml4(StringEscapeUtils.unescapeXml(text)))
				.replaceAll("\r", " ").trim().replaceAll("\n", " ").trim();
		return this;
	}
	
	public TextDriver removeHtmlTags()
	{
		text = text.replaceAll("\\<.*?>", "");
		return this;
	}
	
	public String drive()
	{
		return text;
	}

}
