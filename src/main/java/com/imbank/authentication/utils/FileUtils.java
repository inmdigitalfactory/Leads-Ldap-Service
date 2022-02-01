package com.imbank.authentication.utils;

import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public final class FileUtils {

	public static String readFile(String fileName) throws IOException {
		ClassPathResource cpr = new ClassPathResource(fileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(cpr.getInputStream()));
		String lines = br.lines().collect(Collectors.joining());
		br.close();
		return lines;
	}

}
