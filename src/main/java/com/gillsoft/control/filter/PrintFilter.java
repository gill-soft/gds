package com.gillsoft.control.filter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.springframework.core.io.DefaultResourceLoader;

import com.gillsoft.model.Document;
import com.gillsoft.model.DocumentType;
import com.gillsoft.util.StringUtil;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.layout.font.FontProvider;

public class PrintFilter implements Filter {
	
	private static DefaultResourceLoader resourceLoader = new DefaultResourceLoader();

	private static ConverterProperties properties;
	private static FontProvider CJKFontProvider;
	
	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		final PrintWriter writer = new PrintWriter(out);
		chain.doFilter(request, new HttpServletResponseWrapper((HttpServletResponse) response) {
			@Override
			public PrintWriter getWriter() {
				return writer;
			}
		});
		ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
		
		HtmlConverter.convertToPdf(new ByteArrayInputStream(out.toByteArray()), pdfOut, getDefaultConverterProperties());
		
		List<Document> documents = new ArrayList<>();
		Document document = new Document();
		document.setType(DocumentType.TICKET);
		document.setBase64(StringUtil.toBase64(pdfOut.toByteArray()));
		documents.add(document);
		response.setContentType("application/json");
		response.getOutputStream().print(StringUtil.objectToJsonString(documents));
	}
	
	private ConverterProperties getDefaultConverterProperties() {
		if (properties == null) {
			synchronized (resourceLoader) {
				if (properties == null) {
					properties = new ConverterProperties();
					properties.setFontProvider(getDefaultCJKFontProvider());
					properties.setCharset(StandardCharsets.UTF_8.name());
				}
			}
		}
		return properties;
	}

	private FontProvider getDefaultCJKFontProvider() {
		if (CJKFontProvider == null) {
			synchronized (resourceLoader) {
				if (CJKFontProvider == null) {
					CJKFontProvider = new FontProvider();
					CJKFontProvider.addFont("fonts/open-sans.ttf");
					CJKFontProvider.addFont("fonts/arialuni.ttf");
				}
			}
		}
		return CJKFontProvider;
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		
	}

}
