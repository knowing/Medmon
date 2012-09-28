package de.lmu.ifi.dbs.medmon.sensor.core.watcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;
import org.junit.Test;

public class WindowsWatcherTest {

	@Test
	public void testMassMediaDirectories() {
		Path c = Paths.get("C:");
		for(int i = 'D'; i <= 'Z'; i++) {
			char letter = (char)i;
			Path drive = Paths.get(Character.valueOf(letter) + ":");
			System.out.println(drive + " exists " + Files.exists(drive));
		}
		
		assertTrue("C drive should exist", Files.exists(c));
	}
}
