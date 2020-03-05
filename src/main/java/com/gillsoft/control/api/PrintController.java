package com.gillsoft.control.api;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

import com.gillsoft.control.service.model.PrintOrderWrapper;
import com.gillsoft.model.Lang;
import com.gillsoft.model.response.OrderResponse;
import com.gillsoft.util.StringUtil;
import com.google.common.base.Charsets;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.ibm.icu.text.Transliterator;

import springfox.documentation.annotations.ApiIgnore;

@Controller
@ApiIgnore
public class PrintController {
	
	@Autowired
	private LocaleResolver localeResolver;

	@PostMapping("/order/print")
	public String printOrder(@RequestBody PrintOrderWrapper orderWrapper, Model model, HttpServletRequest request) {
		model.addAttribute("lang", getLang(request));
		model.addAttribute("order", orderWrapper.getOrder());
		try {
			model.addAttribute("translitOrder", transform(orderWrapper.getOrder()));
		} catch (IOException e) {
		}
		return orderWrapper.getTicketLayout();
	}
	
	public Lang getLang(HttpServletRequest request) {
		Locale locale = resolveLocale(request);
		Lang language = Lang.valueOf(locale.getLanguage().toUpperCase());
		if (language == null) {
			language = Lang.EN;
		}
		return language;
	}
	
	public Locale resolveLocale(HttpServletRequest request) {
		return localeResolver.resolveLocale(request);
	}
	
	private OrderResponse transform(OrderResponse order) throws IOException {
		String value = StringUtil.objectToJsonString(order);
		String[] list = { "Ukrainian-Latin/BGN", "Russian-Latin/BGN" };
		for (String s : list) {
			Transliterator trans = Transliterator.getInstance(s);
			value = trans.transform(value);
		}
		return StringUtil.jsonStringToObject(OrderResponse.class, value);
	}
	
	@GetMapping("/generate/image")
	public void generateImage(@RequestParam String base64, HttpServletResponse response) throws IOException {
		
		// отправляем файл в ответ
		response.setContentType("image/gif");
		StreamUtils.copy(new ByteArrayInputStream(StringUtil.fromBase64(base64.replace(' ', '+'))), response.getOutputStream());
		response.flushBuffer();
	}
	
	@GetMapping("/generate/qr/{value}/{width}")
	public void generateQrcode(@PathVariable String value, @PathVariable int width, HttpServletResponse response) {
	    generateCode(new QRCodeWriter(), BarcodeFormat.QR_CODE, value, width, width, response);
	}
	
	@GetMapping("/generate/code128/{value}/{width}/{height}")
	public void generateCode128(@PathVariable String value, @PathVariable int width, @PathVariable int height, HttpServletResponse response) {
	    generateCode(new Code128Writer(), BarcodeFormat.CODE_128, value, width, height, response);
	}
	
	private void generateCode(Writer writer, BarcodeFormat format, String value, int width, int height, HttpServletResponse response) {
		
		// создаем пустую картинку
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    int white = 255 << 16 | 255 << 8 | 255;
	    int black = 0;
	    try {
	    	// генерим код
	    	Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
	    	hints.put(EncodeHintType.CHARACTER_SET, Charsets.UTF_8);
	    	hints.put(EncodeHintType.MARGIN, 0);
	        BitMatrix bitMatrix = writer.encode(value, format, width, height, hints);

	        // раскрашиваем
	        for (int i = 0; i < width; i++) {
	            for (int j = 0; j < height; j++) {
	                image.setRGB(i, j, bitMatrix.get(i, j) ? black : white);
	            }
	        }
	        // отправляем
	        try {
	            ImageIO.write(image, "png", response.getOutputStream());
	        } catch (IOException e) {
	        }
	 
	    } catch (WriterException e) {
	    }
	}
	
}
